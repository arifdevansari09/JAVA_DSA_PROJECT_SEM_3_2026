# Smart Inventory and Order Fulfillment System

A full-stack inventory and order fulfillment management application built with **Java (Spring Boot)** and modern web technologies. This project applies foundational **Data Structures and Algorithms (DSA)** to solve real-world warehouse logistics challenges including constant-time catalog lookups, automated restock prioritization, FIFO customer order processing, and transactional rollback.

---

## ⚡ Core Data Structures & Architecture

| Data Structure | Role in System | Algorithmic Complexity |
| :--- | :--- | :--- |
| **`HashMap<String, Product>`** | Maps unique SKU/Product IDs to product entities for instant lookups, stock updates, and removals. | **$O(1)$** Average Lookup / Update |
| **`PriorityQueue<Product>` (Min-Heap)** | Priority queue sorting items based on remaining stock quantity. The root node always holds the most critical item near depletion. | **$O(1)$** Root Peek, **$O(\log N)$** Re-sift |
| **`Queue<Order>` (FIFO)** | Buffers incoming customer orders and dispatches them in strict arrival order, automatically decrementing available stock. | **$O(1)$** Enqueue / Dequeue |
| **`Stack<StockTransaction>` (LIFO)** | Logs stock modification states, enabling 1-click **Undo** operations to restore previous stock levels upon mistake. | **$O(1)$** Push / Pop |

---

## 🛠️ Tech Stack

- **Backend:** Java 21, Spring Boot 3.2, Maven, Spring Web REST Controllers
- **Frontend:** HTML5, CSS3, JavaScript (ES6+), Responsive Dashboard
- **Design Pattern:** Layered Architecture (Controller &rarr; Service &rarr; Model / Data Structures)

---

## 🚀 Getting Started

### Console Product Inventory Search (PS 45)
This standalone Java console program implements the assignment's add, display, ID lookup, and binary name-search cases. It stores records in an `ArrayList`, uses a `HashMap` for ID lookup, keeps the list sorted by name with manual insertion logic, and saves data to `inventory.txt` when exiting.

Run it from the repository root with Java 21 in PowerShell:
```powershell
New-Item -ItemType Directory -Force .\out | Out-Null
javac -d .\out .\src\main\java\com\rungta\inventory\ProductInventorySearch.java
java -cp .\out com.rungta.inventory.ProductInventorySearch
```

For one-click run in VS Code, install the recommended Extension Pack for Java, open `ProductInventorySearch.java`, and click **Run** above `main`. The **Run Product Inventory Search** configuration is also available under **Run and Debug**.

The first added product receives ID `PR101`. Choose option 5 to save and exit. An optional file path can be supplied as the first argument when running the program.

### 1. Direct Web Dashboard (Instant Run)
To run the interactive frontend demo without any build setup:
- Navigate to `src/main/resources/static/` and open `index.html` in Google Chrome, Microsoft Edge, or Mozilla Firefox.
- Test placing orders into the FIFO Queue, dispatching them, monitoring Min-Heap low-stock alerts, and triggering the LIFO Undo rollback.

### 2. Spring Boot Backend
To run the backend server with Maven:
```bash
# Clone the repository
git clone https://github.com/<your-username>/inventory-management-system.git
cd inventory-management-system

# Build and run with Maven
mvn clean spring-boot:run
```

Once running, access the application at:
```
http://localhost:8080
```

---

## 📡 REST API Endpoints

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/inventory/products` | Retrieve complete inventory catalog |
| `GET` | `/api/inventory/product/{id}` | $O(1)$ product retrieval by SKU ID |
| `GET` | `/api/inventory/alerts` | Get low-stock items prioritized by Min-Heap |
| `GET` | `/api/inventory/orders` | View pending orders in the FIFO Queue |
| `POST` | `/api/inventory/orders/add` | Enqueue a new customer order |
| `POST` | `/api/inventory/orders/process` | Dispatch next order and decrement stock |
| `POST` | `/api/inventory/undo` | Pop from LIFO Stack to revert last stock adjustment |
| `PUT` | `/api/inventory/update-stock` | Manually adjust item stock quantity |
| `POST` | `/api/inventory/add` | Add a new product to the catalog |
| `DELETE` | `/api/inventory/{id}` | Remove a product from the catalog |

---

## 📁 Repository Structure

```
├── src/
│   └── main/
│       ├── java/com/rungta/inventory/
│       │   ├── InventoryManagementApplication.java
│       │   ├── controller/
│       │   │   └── InventoryController.java
│       │   ├── service/
│       │   │   └── InventoryService.java
│       │   └── model/
│       │       ├── Product.java
│       │       ├── Order.java
│       │       └── StockTransaction.java
│       └── resources/
│           ├── application.properties
│           └── static/
│               ├── index.html
│               ├── style.css
│               └── script.js
├── pom.xml
├── .gitignore
└── README.md
```

---

## 👤 Author
- **Arif Ansari** (RU-26-02883)  
  *B.Tech Computer Science & Engineering (AI & ML)*  
  *School of Computer Science & Engineering, RISU*
