package com.marketplace.ms_productos.config;
import com.marketplace.ms_productos.model.Producto;
import com.marketplace.ms_productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

// IMPORTANTE: Arrancar PRIMERO ms-categorias (puerto 8084)
@Slf4j @Component @RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final ProductoRepository repository;
    @Override
    public void run(String... args) {
        if (repository.count() > 0) { log.info(">>> Productos ya cargados."); return; }
        log.info(">>> Cargando productos iniciales...");
        repository.save(new Producto(null,"Laptop Lenovo IdeaPad","Intel Core i5 8GB RAM",new BigDecimal("599990"),10,"LAP-001",2L,1L,true,null,null));
        repository.save(new Producto(null,"Mouse Logitech MX","Mouse inalambrico ergonomico",new BigDecimal("29990"),50,"MOU-001",2L,1L,true,null,null));
        repository.save(new Producto(null,"Poleron Adidas","Poleron deportivo talla M",new BigDecimal("19990"),30,"POL-001",3L,2L,true,null,null));
        log.info(">>> 3 productos cargados OK.");
    }
}
