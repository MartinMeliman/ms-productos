package com.marketplace.ms_productos.service;
import com.marketplace.ms_productos.client.CategoriaClient;
import com.marketplace.ms_productos.client.UsuarioClient;
import com.marketplace.ms_productos.dto.ProductoResponseDTO;
import com.marketplace.ms_productos.model.Producto;
import com.marketplace.ms_productos.repository.ProductoRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service @RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;
    // FeignClient inyectado igual que un @Repository
    private final CategoriaClient categoriaClient;
    private final UsuarioClient usuarioClient;

    public List<ProductoResponseDTO> obtenerTodos() { return productoRepository.findAllActivos().stream().map(this::mapToDTO).collect(Collectors.toList()); }

    public Optional<ProductoResponseDTO> obtenerPorId(Long id) {
        return productoRepository.findById(id).map(p -> {
            ProductoResponseDTO dto = mapToDTO(p);
            dto.setCategoriaNombre(obtenerNombreCategoria(p.getCategoriaId()));
            return dto;
        });
    }

    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return productoRepository.buscarPorNombre(nombre).stream().map(this::mapToDTO).collect(Collectors.toList()); }
        
    public List<ProductoResponseDTO> obtenerPorCategoria(Long cid)  {
        return productoRepository.findByCategoriaIdAndActivoTrue(cid).stream().map(this::mapToDTO).collect(Collectors.toList()); }

    public ProductoResponseDTO guardar(Producto p) {
        if (productoRepository.existsBySku(p.getSku())) throw new RuntimeException("Ya existe un producto con el SKU: " + p.getSku());
        validarCategoria(p.getCategoriaId());
        validarUsuario(p.getVendedorId()); 
        log.info("Guardando producto SKU: {}", p.getSku());
        return mapToDTO(productoRepository.save(p));
    }

    public Optional<ProductoResponseDTO> actualizar(Long id, Producto datos) {
        return productoRepository.findById(id).map(p -> {
            if (!p.getSku().equals(datos.getSku()) && productoRepository.existsBySku(datos.getSku()))
                throw new RuntimeException("El SKU " + datos.getSku() + " ya esta en uso");

            if (datos.getPrecio() == null || datos.getPrecio().doubleValue() <= 0)
                throw new RuntimeException("El precio debe ser mayor a 0");
            if (datos.getStock() == null || datos.getStock() < 0)
                throw new RuntimeException("El stock no puede ser negativo");
            
            validarCategoria(datos.getCategoriaId());
            validarUsuario(datos.getVendedorId()); 

            p.setNombre(datos.getNombre());
            p.setDescripcion(datos.getDescripcion());
            p.setPrecio(datos.getPrecio());
            p.setStock(datos.getStock());
            p.setSku(datos.getSku());
            p.setCategoriaId(datos.getCategoriaId());
            return mapToDTO(productoRepository.save(p));
        });
    }

    public void descontarStock(Long id, Integer cantidad) {
        productoRepository.findById(id).ifPresent(p -> {
            if (!p.getActivo())
                throw new RuntimeException("No se puede descontar stock de un producto inactivo");

            int nuevo = p.getStock() - cantidad;
            if (nuevo < 0) throw new RuntimeException("Stock insuficiente. Disponible: " + p.getStock());
            p.setStock(nuevo); productoRepository.save(p);
            log.info("Stock descontado. Producto ID: {}, nuevo stock: {}", id, nuevo);
        });
    }

    public void desactivar(Long id) { productoRepository.findById(id).ifPresent(p -> { p.setActivo(false); productoRepository.save(p); }); }

    // Llama a ms-categorias via FeignClient. Si falla retorna null sin lanzar error.
    private String obtenerNombreCategoria(Long categoriaId) {
        if (categoriaId == null) return null;
        try {
            Map<String,Object> cat = categoriaClient.obtenerPorId(categoriaId);
            return (String) cat.get("nombre");
        } catch (FeignException e) {
            log.warn("No se pudo obtener categoria ID {}: {}", categoriaId, e.getMessage());
            return null;
        }
    }

    private void validarCategoria(Long categoriaId) {
        if (categoriaId == null) return;
        try {
            categoriaClient.obtenerPorId(categoriaId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Categoria ID " + categoriaId + " no existe");
        } catch (FeignException e) {
            throw new RuntimeException("No se puede conectar con ms-categorias");
        }
    }

private void validarUsuario(Long vendedorId) {
        if (vendedorId == null) return;
        try {
            usuarioClient.obtenerPorId(vendedorId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Vendedor ID " + vendedorId + " no existe");
        } catch (FeignException e) {
            throw new RuntimeException("No se puede conectar con ms-usuarios");
        }
    }

    private ProductoResponseDTO mapToDTO(Producto p) {
        return new ProductoResponseDTO(p.getId(),p.getNombre(),p.getDescripcion(),p.getPrecio(),
            p.getStock(),p.getSku(),p.getCategoriaId(),null,p.getVendedorId(),p.getActivo(),
            p.getActivo()&&p.getStock()>0,p.getCreadoEn());
    }
}
