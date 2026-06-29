package com.marketplace.ms_productos.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// categoriaId y vendedorId son referencias LOGICAS (no FK real entre microservicios)
@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacio") @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // BigDecimal para precios: evita errores de precision de float/double
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull @Min(0) @Column(nullable = false)
    private Integer stock;

    @NotBlank(message = "El SKU es obligatorio")
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "vendedor_id")
    private Long vendedorId;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
    
    @PrePersist public void prePersist() {
        if (this.activo == null) this.activo = true;
        creadoEn = actualizadoEn = LocalDateTime.now();
    }
    @PreUpdate public void preUpdate() { actualizadoEn = LocalDateTime.now(); }
}
