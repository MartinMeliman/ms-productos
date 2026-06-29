package com.marketplace.ms_productos.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// ═══════════════════════════════════════════════════
// @FeignClient genera implementacion automatica.
// Llama a GET http://localhost:8084/api/categorias/{id}
// Si ms-categorias responde 404 → FeignException.NotFound
// ═══════════════════════════════════════════════════
@FeignClient(name = "ms-categorias", url = "${ms.categorias.url}")
public interface CategoriaClient {
    @GetMapping("/api/categorias/{id}")
    Map<String, Object> obtenerPorId(@PathVariable Long id);
}
