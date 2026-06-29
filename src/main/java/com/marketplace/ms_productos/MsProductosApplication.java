package com.marketplace.ms_productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @EnableFeignClients → escanea el paquete buscando interfaces @FeignClient
// SIN esta anotacion → NoSuchBeanDefinitionException al inyectar el client
@SpringBootApplication
@EnableFeignClients
public class MsProductosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsProductosApplication.class, args);
    }
}
