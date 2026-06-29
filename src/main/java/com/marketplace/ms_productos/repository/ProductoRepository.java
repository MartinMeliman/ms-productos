package com.marketplace.ms_productos.repository;
import com.marketplace.ms_productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);
    boolean existsBySku(String sku);
    @Query("SELECT p FROM Producto p WHERE p.activo = true") List<Producto> findAllActivos();
    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.activo = true")
    List<Producto> buscarPorNombre(@Param("nombre") String nombre);
}
