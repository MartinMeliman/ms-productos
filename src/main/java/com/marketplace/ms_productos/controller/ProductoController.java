package com.marketplace.ms_productos.controller;

import com.marketplace.ms_productos.dto.ProductoResponseDTO;
import com.marketplace.ms_productos.model.Producto;
import com.marketplace.ms_productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Productos", description = "Catálogo de productos del marketplace EcoTrade")
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Listar todos los productos activos",
               description = "Retorna el catálogo completo de productos activos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    })
    @GetMapping
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoService.obtenerTodos();
    }

    @Operation(summary = "Obtener producto por ID",
               description = "Busca un producto activo por su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar productos por nombre",
               description = "Busca productos cuyo nombre contenga el texto indicado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de productos encontrados")
    })
    @GetMapping("/buscar")
    public List<ProductoResponseDTO> buscar(
            @Parameter(description = "Nombre o parte del nombre a buscar") @RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    @Operation(summary = "Listar productos por categoría",
               description = "Retorna todos los productos activos de una categoría específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de productos de la categoría")
    })
    @GetMapping("/categoria/{cid}")
    public List<ProductoResponseDTO> porCategoria(
            @Parameter(description = "ID de la categoría") @PathVariable Long cid) {
        return productoService.obtenerPorCategoria(cid);
    }

    @Operation(summary = "Crear nuevo producto",
               description = "Registra un nuevo producto. El SKU debe ser único en el sistema.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "SKU duplicado o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Categoría o vendedor no existe")
    })
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody Producto p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(p));
    }

    @Operation(summary = "Actualizar producto",
               description = "Actualiza los datos de un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody Producto datos) {
        return productoService.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Descontar stock del producto",
               description = "Reduce el stock de un producto. Llamado internamente por ms-pedidos al crear un pedido.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock descontado correctamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> descontarStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Parameter(description = "Cantidad a descontar") @RequestParam Integer cantidad) {
        productoService.descontarStock(id, cantidad);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Desactivar producto (soft delete)",
               description = "Marca el producto como inactivo sin eliminarlo de la base de datos")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Producto desactivado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @Parameter(description = "ID del producto a desactivar") @PathVariable Long id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}