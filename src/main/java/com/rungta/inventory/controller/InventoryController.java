package com.rungta.inventory.controller;

import com.rungta.inventory.model.Order;
import com.rungta.inventory.model.Product;
import com.rungta.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing inventory, order fulfillment (FIFO queue),
 * and transaction undo (LIFO stack) endpoints.
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        Product product = inventoryService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Product>> getLowStockAlerts(@RequestParam(required = false) Integer threshold) {
        return ResponseEntity.ok(inventoryService.getLowStockAlerts(threshold));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(inventoryService.getPendingOrders());
    }

    @PostMapping("/orders/add")
    public ResponseEntity<Order> enqueueOrder(@RequestBody Order order) {
        return ResponseEntity.ok(inventoryService.enqueueOrder(order));
    }

    @PostMapping("/orders/process")
    public ResponseEntity<Map<String, Object>> processNextOrder() {
        return ResponseEntity.ok(inventoryService.processNextOrder());
    }

    @PostMapping("/undo")
    public ResponseEntity<Map<String, Object>> undoLastTransaction() {
        return ResponseEntity.ok(inventoryService.undoLastTransaction());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(inventoryService.getInventorySummary());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Product product) {
        try {
            Product created = inventoryService.addProduct(product);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update-stock")
    public ResponseEntity<?> updateStock(@RequestParam String productId, 
                                        @RequestParam int quantity,
                                        @RequestParam(defaultValue = "MANUAL_ADJUSTMENT") String reason) {
        Product updated = inventoryService.updateStock(productId, quantity, reason);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Product not found: " + productId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        boolean deleted = inventoryService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
