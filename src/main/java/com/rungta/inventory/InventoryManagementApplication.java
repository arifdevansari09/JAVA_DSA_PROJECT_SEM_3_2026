package com.rungta.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Entry Point for Smart Inventory Management System.
 * Rungta International Skills University - DSA Project
 */
@SpringBootApplication
public class InventoryManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
        System.out.println("==================================================");
        System.out.println(" Smart Inventory Management System Started!       ");
        System.out.println(" Access Dashboard: http://localhost:8080         ");
        System.out.println("==================================================");
    }
}
