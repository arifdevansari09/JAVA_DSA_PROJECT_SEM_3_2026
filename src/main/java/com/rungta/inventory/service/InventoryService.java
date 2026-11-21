package com.rungta.inventory.service;

import com.rungta.inventory.model.Order;
import com.rungta.inventory.model.Product;
import com.rungta.inventory.model.StockTransaction;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core Inventory Management Service applying foundational Data Structures:
 * 1. HashMap: O(1) Product lookup, insert, and quantity adjustments by SKU
 * 2. Min-Heap PriorityQueue: O(1) peek for critical stock depletion alerts
 * 3. FIFO Queue: O(1) order arrival and dispatch fulfillment processing
 * 4. LIFO Stack: O(1) transaction audit trail with 1-click Undo capability
 */
@Service
public class InventoryService {

    // 1. HashMap for O(1) product catalog indexing by Product ID
    private final Map<String, Product> productMap = new HashMap<>();

    // 2. Min-Heap PriorityQueue for low-stock alerts (ordered by quantity ascending)
    private final PriorityQueue<Product> lowStockHeap = new PriorityQueue<>();

    // 3. FIFO Queue for customer order processing (First-In, First-Out)
    private final Queue<Order> orderQueue = new LinkedList<>();

    // 4. LIFO Stack for transaction history and undo operations (Last-In, First-Out)
    private final Stack<StockTransaction> transactionStack = new Stack<>();

    @PostConstruct
    public void init() {
        loadPredefinedInventory();
    }

    /**
     * Initializes a standard warehouse catalog for demonstration.
     */
    public void loadPredefinedInventory() {
        addProduct(new Product("PRD101", "Mechanical Keyboard", "Electronics", 4, 10, 2499.0));
        addProduct(new Product("PRD102", "Wireless Mouse", "Electronics", 18, 10, 899.0));
        addProduct(new Product("PRD103", "Mouse Pad RGB", "Accessories", 3, 8, 499.0));
        addProduct(new Product("PRD104", "USB-C Hub Multiport", "Electronics", 12, 5, 1599.0));
        addProduct(new Product("PRD105", "Ergonomic Desk Chair", "Furniture", 2, 5, 7999.0));
        addProduct(new Product("PRD106", "Monitor 24 inch IPS", "Electronics", 7, 5, 11499.0));
        addProduct(new Product("PRD107", "Laptop Stand Aluminum", "Accessories", 15, 10, 1299.0));
        addProduct(new Product("PRD108", "Noise Cancelling Headphone", "Audio", 1, 6, 4999.0));
        addProduct(new Product("PRD109", "Webcam 1080p HD", "Electronics", 9, 8, 2199.0));
        addProduct(new Product("PRD110", "Desk Cable Organizer", "Accessories", 25, 12, 299.0));

        // Sample initial pending order in FIFO Queue
        enqueueOrder(new Order("ORD-1001", "PRD102", 2, "Rahul Sharma"));
        enqueueOrder(new Order("ORD-1002", "PRD104", 1, "Priya Verma"));
    }

    // --- HashMap Operations (O(1)) ---

    public synchronized Product addProduct(Product product) {
        if (product == null || product.getProductId() == null) {
            throw new IllegalArgumentException("Invalid product information");
        }
        String id = product.getProductId().trim().toUpperCase();
        product.setProductId(id);
        productMap.put(id, product);
        refreshHeap();
        return product;
    }

    public Product getProductById(String productId) {
        if (productId == null) return null;
        return productMap.get(productId.trim().toUpperCase());
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(productMap.values());
    }

    /**
     * Updates product quantity and logs to LIFO Stack for Undo capability.
     * Time Complexity: O(1) HashMap update + O(log N) Heap re-sift + O(1) Stack push
     */
    public synchronized Product updateStock(String productId, int newQuantity, String reason) {
        if (productId == null) return null;
        String id = productId.trim().toUpperCase();
        Product product = productMap.get(id);

        if (product != null) {
            int oldQuantity = product.getQuantity();
            product.setQuantity(newQuantity);

            // Record transaction onto LIFO Stack
            String txId = "TX-" + UUID.randomUUID().toString().substring(0, 8);
            transactionStack.push(new StockTransaction(txId, id, oldQuantity, newQuantity, reason));

            refreshHeap();
            return product;
        }
        return null;
    }

    public synchronized boolean deleteProduct(String productId) {
        if (productId == null) return false;
        String id = productId.trim().toUpperCase();
        Product removed = productMap.remove(id);
        if (removed != null) {
            refreshHeap();
            return true;
        }
        return false;
    }

    // --- Min-Heap Low-Stock Operations ---

    private void refreshHeap() {
        lowStockHeap.clear();
        lowStockHeap.addAll(productMap.values());
    }

    public List<Product> getLowStockAlerts(Integer customThreshold) {
        List<Product> alerts = new ArrayList<>();
        PriorityQueue<Product> copyHeap = new PriorityQueue<>(lowStockHeap);

        while (!copyHeap.isEmpty()) {
            Product item = copyHeap.poll();
            int threshold = (customThreshold != null) ? customThreshold : item.getMinThreshold();
            if (item.getQuantity() <= threshold) {
                alerts.add(item);
            }
        }
        return alerts;
    }

    public Product peekMostCriticalLowStock() {
        return lowStockHeap.peek();
    }

    // --- FIFO Queue Operations (Order Processing) ---

    public synchronized Order enqueueOrder(Order order) {
        if (order == null || order.getProductId() == null) {
            throw new IllegalArgumentException("Invalid order data");
        }
        orderQueue.offer(order);
        return order;
    }

    public List<Order> getPendingOrders() {
        return new ArrayList<>(orderQueue);
    }

    /**
     * Processes next order from FIFO Queue.
     * Dequeues order, decrements stock in HashMap, logs to Stack, and reorganizes Min-Heap.
     */
    public synchronized Map<String, Object> processNextOrder() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (orderQueue.isEmpty()) {
            result.put("status", "EMPTY");
            result.put("message", "No pending orders in FIFO queue.");
            return result;
        }

        Order nextOrder = orderQueue.peek();
        Product product = productMap.get(nextOrder.getProductId());

        if (product == null) {
            orderQueue.poll();
            nextOrder.setStatus("FAILED_NOT_FOUND");
            result.put("status", "FAILED");
            result.put("message", "Product ID " + nextOrder.getProductId() + " not found.");
            result.put("order", nextOrder);
            return result;
        }

        if (product.getQuantity() < nextOrder.getQuantity()) {
            result.put("status", "INSUFFICIENT_STOCK");
            result.put("message", "Insufficient stock for " + product.getName() + " (Available: " + product.getQuantity() + ", Required: " + nextOrder.getQuantity() + ")");
            result.put("order", nextOrder);
            return result;
        }

        // Successfully fulfill order
        orderQueue.poll();
        int prevQty = product.getQuantity();
        int newQty = prevQty - nextOrder.getQuantity();
        product.setQuantity(newQty);
        nextOrder.setStatus("FULFILLED");

        // Push transaction to Undo Stack
        String txId = "TX-ORD-" + UUID.randomUUID().toString().substring(0, 8);
        transactionStack.push(new StockTransaction(txId, product.getProductId(), prevQty, newQty, "ORDER_FULFILLMENT (" + nextOrder.getOrderId() + ")"));

        refreshHeap();

        result.put("status", "SUCCESS");
        result.put("message", "Order " + nextOrder.getOrderId() + " fulfilled successfully.");
        result.put("order", nextOrder);
        result.put("updatedProduct", product);
        return result;
    }

    // --- LIFO Stack Operations (Undo Last Transaction) ---

    public synchronized Map<String, Object> undoLastTransaction() {
        Map<String, Object> response = new LinkedHashMap<>();
        if (transactionStack.isEmpty()) {
            response.put("status", "EMPTY");
            response.put("message", "No transactions in stack to undo.");
            return response;
        }

        StockTransaction lastTx = transactionStack.pop();
        Product product = productMap.get(lastTx.getProductId());

        if (product != null) {
            int revertedFrom = product.getQuantity();
            product.setQuantity(lastTx.getPreviousQuantity());
            refreshHeap();

            response.put("status", "SUCCESS");
            response.put("message", "Reverted " + product.getName() + " stock from " + revertedFrom + " to " + lastTx.getPreviousQuantity());
            response.put("revertedTransaction", lastTx);
            response.put("product", product);
        } else {
            response.put("status", "ERROR");
            response.put("message", "Product associated with transaction no longer exists.");
        }
        return response;
    }

    public List<StockTransaction> getRecentTransactions() {
        List<StockTransaction> list = new ArrayList<>(transactionStack);
        Collections.reverse(list);
        return list;
    }

    public Map<String, Object> getInventorySummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        int totalItems = productMap.size();
        int totalUnits = 0;
        double totalValuation = 0.0;
        int lowStockCount = 0;

        for (Product p : productMap.values()) {
            totalUnits += p.getQuantity();
            totalValuation += (p.getQuantity() * p.getPrice());
            if (p.isLowStock()) {
                lowStockCount++;
            }
        }

        summary.put("totalUniqueProducts", totalItems);
        summary.put("totalUnitsInStock", totalUnits);
        summary.put("totalInventoryValue", totalValuation);
        summary.put("lowStockCount", lowStockCount);
        summary.put("pendingOrdersCount", orderQueue.size());
        summary.put("stackTransactionsCount", transactionStack.size());
        return summary;
    }
}
