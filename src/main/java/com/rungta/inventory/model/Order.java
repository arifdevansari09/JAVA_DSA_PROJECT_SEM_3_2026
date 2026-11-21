package com.rungta.inventory.model;

import java.time.LocalDateTime;

/**
 * Model representing a customer order for FIFO queue processing.
 */
public class Order {
    private String orderId;
    private String productId;
    private int quantity;
    private String customerName;
    private String status; // PENDING, FULFILLED, FAILED
    private LocalDateTime orderTime;

    public Order() {
        this.orderTime = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Order(String orderId, String productId, int quantity, String customerName) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.customerName = customerName;
        this.status = "PENDING";
        this.orderTime = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
}
