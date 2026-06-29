package com.marketplace.ms_productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marketplace.ms_productos.client.CategoriaClient;
import com.marketplace.ms_productos.client.UsuarioClient;
import com.marketplace.ms_productos.dto.ProductoResponseDTO;
import com.marketplace.ms_productos.model.Producto;
import com.marketplace.ms_productos.repository.ProductoRepository;
import com.marketplace.ms_productos.service.ProductoService;

import feign.FeignException;

/**
 * Pruebas unitarias para ProductoService.
 * Los FeignClients (CategoriaClient, UsuarioClient) se simulan con @Mock.
 * Patrón Given/When/Then con Mockito (sin BD ni red real).
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaClient categoriaClient;
    @Mock private UsuarioClient usuarioClient;
    @InjectMocks private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setDescripcion("Laptop gamer");
        producto.setPrecio(new BigDecimal("500000"));
        producto.setStock(10);
        producto.setSku("SKU-001");
        producto.setCategoriaId(1L);
        producto.setVendedorId(1L);
        producto.setActivo(true);
    }

    @Test
    @DisplayName("obtenerTodos: debería retornar productos activos")
    void shouldReturnAllActiveProducts() {
        // GIVEN
        when(productoRepository.findAllActivos()).thenReturn(List.of(producto));
        // WHEN
        List<ProductoResponseDTO> resultado = productoService.obtenerTodos();
        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Laptop", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("obtenerPorId: debería retornar el producto con nombre de categoría")
    void shouldReturnProductWithCategory() {
        // GIVEN — producto existe y categoría responde via Feign
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaClient.obtenerPorId(1L)).thenReturn(Map.of("nombre", "Tecnologia"));
        // WHEN
        Optional<ProductoResponseDTO> resultado = productoService.obtenerPorId(1L);
        // THEN
        assertTrue(resultado.isPresent());
        assertEquals("Tecnologia", resultado.get().getCategoriaNombre());
    }

    @Test
    @DisplayName("guardar: debería crear el producto cuando SKU, categoría y vendedor son válidos")
    void shouldSaveProductSuccessfully() {
        // GIVEN — SKU único, categoría existe, usuario existe
        when(productoRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoriaClient.obtenerPorId(1L)).thenReturn(Map.of("nombre", "Tecnologia"));
        when(usuarioClient.obtenerPorId(1L)).thenReturn(Map.of("id", 1L));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        // WHEN
        ProductoResponseDTO resultado = productoService.guardar(producto);
        // THEN
        assertNotNull(resultado);
        assertEquals("Laptop", resultado.getNombre());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("guardar: debería lanzar excepción cuando el SKU ya existe")
    void shouldThrowWhenSkuDuplicated() {
        // GIVEN — regla de negocio: SKU único
        when(productoRepository.existsBySku("SKU-001")).thenReturn(true);
        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> productoService.guardar(producto));
        assertTrue(ex.getMessage().contains("SKU"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardar: debería lanzar excepción cuando la categoría no existe")
    void shouldThrowWhenCategoryNotFound() {
        // GIVEN — SKU OK pero categoría lanza 404 via Feign
        when(productoRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoriaClient.obtenerPorId(1L)).thenThrow(FeignException.NotFound.class);
        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> productoService.guardar(producto));
        assertTrue(ex.getMessage().contains("Categoria"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("descontarStock: debería descontar correctamente cuando hay stock")
    void shouldDiscountStockSuccessfully() {
        // GIVEN — producto activo con stock 10
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        // WHEN — descontamos 3
        productoService.descontarStock(1L, 3);
        // THEN — stock pasa de 10 a 7
        assertEquals(7, producto.getStock());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("descontarStock: debería lanzar excepción cuando el stock es insuficiente")
    void shouldThrowWhenStockInsufficient() {
        // GIVEN — producto con stock 10, intentamos descontar 20
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        // WHEN + THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> productoService.descontarStock(1L, 20));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("desactivar: debería marcar el producto como inactivo (soft delete)")
    void shouldDeactivateProduct() {
        // GIVEN
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        // WHEN
        productoService.desactivar(1L);
        // THEN
        assertFalse(producto.getActivo());
        verify(productoRepository, times(1)).save(producto);
    }
}
