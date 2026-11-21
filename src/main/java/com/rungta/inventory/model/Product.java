package com.rungta.inventory.model;

/**
 * Domain model representing a warehouse inventory product.
 * Implements Comparable to enable Min-Heap ordering based on stock quantity.
 */
public class Product implements Comparable<Product> {
    private String productId;
    private String name;
    private String category;
    private int quantity;
    private int minThreshold;
    private double price;

    public Product() {
    }

    public Product(String productId, String name, String category, int quantity, int minThreshold, double price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.minThreshold = minThreshold;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(int minThreshold) {
        this.minThreshold = minThreshold;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isLowStock() {
        return this.quantity <= this.minThreshold;
    }

    /**
     * Min-Heap ordering logic: Items with lowest stock get highest priority (root of heap).
     */
    @Override
    public int compareTo(Product other) {
        if (other == null) return -1;
        int qtyCompare = Integer.compare(this.quantity, other.quantity);
        if (qtyCompare != 0) {
            return qtyCompare;
        }
        return this.productId.compareTo(other.productId);
    }
}
