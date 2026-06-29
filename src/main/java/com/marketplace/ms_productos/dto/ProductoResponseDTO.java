package com.marketplace.ms_productos.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO de SALIDA: incluye categoriaNombre enriquecido desde ms-categorias via Feign
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String sku;
    private Long categoriaId;
    private String categoriaNombre; // obtenido de ms-categorias via FeignClient
    private Long vendedorId;
    private boolean activo;
    private boolean disponible;     // calculado: activo && stock > 0
    private LocalDateTime creadoEn;
}
