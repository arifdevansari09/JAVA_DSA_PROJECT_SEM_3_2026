package com.rungta.inventory.model;

import java.time.LocalDateTime;

/**
 * Model representing an inventory stock adjustment transaction.
 * Stored on a LIFO Stack to enable 1-click Undo operations.
 */
public class StockTransaction {
    private String transactionId;
    private String productId;
    private int previousQuantity;
    private int newQuantity;
    private String transactionType; // RESTOCK, ORDER_FULFILLMENT, MANUAL_UPDATE
    private LocalDateTime timestamp;

    public StockTransaction() {
        this.timestamp = LocalDateTime.now();
    }

    public StockTransaction(String transactionId, String productId, int previousQuantity, int newQuantity, String transactionType) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.transactionType = transactionType;
        this.timestamp = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(int previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
