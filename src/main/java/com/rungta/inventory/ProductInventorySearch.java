package com.rungta.inventory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public final class ProductInventorySearch {
    private final List<Product> products = new ArrayList<>();
    private final Map<String, Product> productsById = new HashMap<>();
    private final Path inventoryFile;
    private long nextProductNumber = 101;

    public ProductInventorySearch(Path inventoryFile) throws IOException {
        this.inventoryFile = inventoryFile;
        loadProducts();
    }

    public static void main(String[] args) throws IOException {
        Path inventoryFile = args.length == 0 ? Path.of("inventory.txt") : Path.of(args[0]);
        ProductInventorySearch app = new ProductInventorySearch(inventoryFile);
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            app.run(input);
        }
    }

    public Product addProduct(String name, int quantity, double price) {
        String validName = validateName(name);
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }

        String productId = String.format(Locale.ROOT, "PR%03d", nextProductNumber++);
        Product product = new Product(productId, validName, quantity, price);
        productsById.put(productId, product);
        insertByName(product);
        return product;
    }

    public Product findById(String productId) {
        if (productId == null) {
            return null;
        }
        return productsById.get(productId.trim().toUpperCase(Locale.ROOT));
    }

    public int binarySearchByName(String name) {
        if (name == null || name.isBlank()) {
            return -1;
        }

        String target = name.trim();
        int low = 0;
        int high = products.size() - 1;
        while (low <= high) {
            int middle = low + (high - low) / 2;
            int comparison = products.get(middle).name().compareToIgnoreCase(target);
            if (comparison == 0) {
                return middle;
            }
            if (comparison < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return -1;
    }

    public List<Product> getProducts() {
        return List.copyOf(products);
    }

    public void run(Scanner input) throws IOException {
        boolean running = true;
        while (running) {
            printMenu();
            if (!input.hasNextLine()) {
                break;
            }

            String choice = input.nextLine().trim();
            switch (choice) {
                case "1" -> addProductFromInput(input);
                case "2" -> displayProducts();
                case "3" -> searchByIdFromInput(input);
                case "4" -> searchByNameFromInput(input);
                case "5" -> {
                    running = false;
                    System.out.println("Saving inventory and exiting.");
                }
                default -> System.out.println("Invalid option. Choose 1, 2, 3, 4, or 5.");
            }
        }
        saveProducts();
    }

    private void loadProducts() throws IOException {
        if (!Files.exists(inventoryFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(inventoryFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split("\t", -1);
                if (fields.length != 4) {
                    throw new IOException("Invalid inventory record at line " + lineNumber + ".");
                }

                String productId = fields[0].trim().toUpperCase(Locale.ROOT);
                String name = fields[1];
                int quantity;
                double price;
                long productNumber;
                try {
                    quantity = Integer.parseInt(fields[2]);
                    price = Double.parseDouble(fields[3]);
                    if (!productId.startsWith("PR") || productId.length() <= 2) {
                        throw new NumberFormatException();
                    }
                    productNumber = Long.parseLong(productId.substring(2));
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid inventory record at line " + lineNumber + ".", e);
                }

                if (name.isBlank() || name.indexOf('\t') >= 0 || quantity < 0
                        || !Double.isFinite(price) || price <= 0) {
                    throw new IOException("Invalid inventory record at line " + lineNumber + ".");
                }
                if (productsById.containsKey(productId)) {
                    throw new IOException("Duplicate product ID at line " + lineNumber + ": " + productId);
                }

                Product product = new Product(productId, name, quantity, price);
                productsById.put(productId, product);
                insertByName(product);
                nextProductNumber = Math.max(nextProductNumber, productNumber + 1);
            }
        }
    }

    private void saveProducts() throws IOException {
        Path parent = inventoryFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                inventoryFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            for (Product product : products) {
                writer.write(product.productId());
                writer.write('\t');
                writer.write(product.name());
                writer.write('\t');
                writer.write(Integer.toString(product.quantity()));
                writer.write('\t');
                writer.write(Double.toString(product.price()));
                writer.newLine();
            }
        }
    }

    private void insertByName(Product product) {
        int low = 0;
        int high = products.size();
        while (low < high) {
            int middle = low + (high - low) / 2;
            if (compareProducts(products.get(middle), product) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        products.add(low, product);
    }

    private static int compareProducts(Product left, Product right) {
        int nameComparison = left.name().compareToIgnoreCase(right.name());
        return nameComparison != 0 ? nameComparison : left.productId().compareTo(right.productId());
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        String trimmedName = name.trim();
        if (trimmedName.indexOf('\t') >= 0 || trimmedName.indexOf('\n') >= 0 || trimmedName.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Product name cannot contain tabs or line breaks.");
        }
        return trimmedName;
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("Product Inventory Search");
        System.out.println("1. Add Product");
        System.out.println("2. Display All Products");
        System.out.println("3. Search by Product ID");
        System.out.println("4. Binary Search by Product Name");
        System.out.println("5. Save and Exit");
        System.out.print("Choose option: ");
    }

    private void addProductFromInput(Scanner input) {
        System.out.print("Name: ");
        if (!input.hasNextLine()) {
            return;
        }
        String name = input.nextLine();

        System.out.print("Quantity: ");
        if (!input.hasNextLine()) {
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(input.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity. Enter a non-negative integer.");
            return;
        }

        System.out.print("Price: ");
        if (!input.hasNextLine()) {
            return;
        }
        double price;
        try {
            price = Double.parseDouble(input.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid price. Enter a number greater than 0.");
            return;
        }

        try {
            Product product = addProduct(name, quantity, price);
            System.out.println("Product added. ID: " + product);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid product: " + e.getMessage());
        }
    }

    private void displayProducts() {
        if (products.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        for (Product product : products) {
            System.out.println(product);
        }
    }

    private void searchByIdFromInput(Scanner input) {
        System.out.print("Product ID: ");
        if (!input.hasNextLine()) {
            return;
        }
        String productId = input.nextLine().trim();
        Product product = findById(productId);
        if (product == null) {
            System.out.println("Product ID " + productId.toUpperCase(Locale.ROOT) + " not found.");
        } else {
            System.out.println("Found: " + product);
        }
    }

    private void searchByNameFromInput(Scanner input) {
        System.out.print("Product name: ");
        if (!input.hasNextLine()) {
            return;
        }
        String name = input.nextLine().trim();
        int index = binarySearchByName(name);
        if (index < 0) {
            System.out.println("Product name " + name + " not found.");
        } else {
            System.out.println("Found at index " + index + ": " + products.get(index));
        }
    }

    public record Product(String productId, String name, int quantity, double price) {
        @Override
        public String toString() {
            return productId + " | " + name + " | Qty: " + quantity + " | Price: "
                    + BigDecimal.valueOf(price).stripTrailingZeros().toPlainString();
        }
    }
}
