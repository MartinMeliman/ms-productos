package com.marketplace.ms_productos.controller;
import com.marketplace.ms_productos.dto.ProductoResponseDTO;
import com.marketplace.ms_productos.model.Producto;
import com.marketplace.ms_productos.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/productos") @RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @GetMapping public List<ProductoResponseDTO> obtenerTodos() { return productoService.obtenerTodos(); }
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/buscar") public List<ProductoResponseDTO> buscar(@RequestParam String nombre) { return productoService.buscarPorNombre(nombre); }
    @GetMapping("/categoria/{cid}") public List<ProductoResponseDTO> porCategoria(@PathVariable Long cid) { return productoService.obtenerPorCategoria(cid); }
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody Producto p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(p));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody Producto datos) {
        return productoService.actualizar(id,datos).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    // PATCH: actualizacion parcial del stock, llamado por ms-pedidos
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> descontarStock(@PathVariable Long id, @RequestParam Integer cantidad) {
        productoService.descontarStock(id, cantidad); return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) { productoService.desactivar(id); return ResponseEntity.noContent().build(); }
}
