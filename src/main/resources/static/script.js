/**
 * Smart Inventory & Order Fulfillment System - Frontend DSA Engine
 * Student: Arif Ansari (RU-26-02883)
 * Course: Data Structures and Algorithms (DSA) - RISU Bhilai
 */

// --- 1. Min-Heap Priority Queue Implementation (For Low-Stock Tracking) ---
class MinHeap {
    constructor() {
        this.heap = [];
    }

    push(product) {
        this.heap.push(product);
        this._siftUp(this.heap.length - 1);
    }

    pop() {
        if (this.heap.length === 0) return null;
        if (this.heap.length === 1) return this.heap.pop();
        const min = this.heap[0];
        this.heap[0] = this.heap.pop();
        this._siftDown(0);
        return min;
    }

    size() {
        return this.heap.length;
    }

    _siftUp(index) {
        let parent = Math.floor((index - 1) / 2);
        while (index > 0 && this.heap[index].quantity < this.heap[parent].quantity) {
            [this.heap[index], this.heap[parent]] = [this.heap[parent], this.heap[index]];
            index = parent;
            parent = Math.floor((index - 1) / 2);
        }
    }

    _siftDown(index) {
        let smallest = index;
        const left = 2 * index + 1;
        const right = 2 * index + 2;
        const len = this.heap.length;

        if (left < len && this.heap[left].quantity < this.heap[smallest].quantity) {
            smallest = left;
        }
        if (right < len && this.heap[right].quantity < this.heap[smallest].quantity) {
            smallest = right;
        }
        if (smallest !== index) {
            [this.heap[index], this.heap[smallest]] = [this.heap[smallest], this.heap[index]];
            this._siftDown(smallest);
        }
    }
}

// --- 2. Initial Data & State ---
const initialProducts = [
    { productId: "PRD101", name: "Mechanical Keyboard", category: "Electronics", quantity: 4, minThreshold: 10, price: 2499 },
    { productId: "PRD102", name: "Wireless Mouse", category: "Electronics", quantity: 18, minThreshold: 10, price: 899 },
    { productId: "PRD103", name: "Mouse Pad RGB", category: "Accessories", quantity: 3, minThreshold: 8, price: 499 },
    { productId: "PRD104", name: "USB-C Hub Multiport", category: "Electronics", quantity: 12, minThreshold: 5, price: 1599 },
    { productId: "PRD105", name: "Ergonomic Desk Chair", category: "Furniture", quantity: 2, minThreshold: 5, price: 7999 },
    { productId: "PRD106", name: "Monitor 24 inch IPS", category: "Electronics", quantity: 7, minThreshold: 5, price: 11499 },
    { productId: "PRD107", name: "Laptop Stand Aluminum", category: "Accessories", quantity: 15, minThreshold: 10, price: 1299 },
    { productId: "PRD108", name: "Noise Cancelling Headphone", category: "Audio", quantity: 1, minThreshold: 6, price: 4999 },
    { productId: "PRD109", name: "Webcam 1080p HD", category: "Electronics", quantity: 9, minThreshold: 8, price: 2199 },
    { productId: "PRD110", name: "Desk Cable Organizer", category: "Accessories", quantity: 25, minThreshold: 12, price: 299 }
];

// Data Structures
const productHashMap = new Map(); // HashMap<String, Product>
const orderQueue = [];            // Queue<Order> (FIFO)
const transactionStack = [];      // Stack<StockTransaction> (LIFO)
let activeFilter = 'all';

// Populate HashMap
initialProducts.forEach(prod => productHashMap.set(prod.productId, { ...prod }));

// Populate Initial FIFO Queue
orderQueue.push({ orderId: "ORD-1001", productId: "PRD102", quantity: 2, customerName: "Rahul Sharma", timestamp: "Just now" });
orderQueue.push({ orderId: "ORD-1002", productId: "PRD104", quantity: 1, customerName: "Priya Verma", timestamp: "2m ago" });

// --- 3. DOM Elements ---
const tableBody = document.getElementById('inventoryTableBody');
const tableCountLabel = document.getElementById('tableCountLabel');
const searchInput = document.getElementById('searchInput');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const categoryFilter = document.getElementById('categoryFilter');
const filterLowStockBtn = document.getElementById('filterLowStockBtn');
const resetFilterBtn = document.getElementById('resetFilterBtn');

const kpiTotalItems = document.getElementById('kpi-total-items');
const kpiLowStock = document.getElementById('kpi-low-stock');
const kpiPendingOrders = document.getElementById('kpi-pending-orders');
const kpiTotalVal = document.getElementById('kpi-total-val');

const alertChipsContainer = document.getElementById('alertChips');
const alertCountBadge = document.getElementById('alertCountBadge');
const orderQueueContainer = document.getElementById('orderQueueContainer');
const transactionStackContainer = document.getElementById('transactionStackContainer');

const openAddModalBtn = document.getElementById('openAddModalBtn');
const closeAddModalBtn = document.getElementById('closeAddModalBtn');
const cancelAddBtn = document.getElementById('cancelAddBtn');
const addProductModal = document.getElementById('addProductModal');
const addProductForm = document.getElementById('addProductForm');

const openOrderModalBtn = document.getElementById('openOrderModalBtn');
const closeOrderModalBtn = document.getElementById('closeOrderModalBtn');
const cancelOrderBtn = document.getElementById('cancelOrderBtn');
const placeOrderModal = document.getElementById('placeOrderModal');
const placeOrderForm = document.getElementById('placeOrderForm');
const orderProductSelect = document.getElementById('orderProductSelect');
const processOrderBtn = document.getElementById('processOrderBtn');
const undoBtn = document.getElementById('undoBtn');

// --- 4. Render Cycle ---
function render() {
    updateKPIs();
    renderAlerts();
    renderOrderQueue();
    renderTransactionStack();
    renderTable();
    populateProductSelect();
}

function updateKPIs() {
    const products = Array.from(productHashMap.values());
    let lowStockCount = 0;
    let totalValue = 0;

    products.forEach(p => {
        totalValue += (p.quantity * p.price);
        if (p.quantity <= p.minThreshold) {
            lowStockCount++;
        }
    });

    kpiTotalItems.textContent = products.length;
    kpiLowStock.textContent = lowStockCount;
    kpiPendingOrders.textContent = orderQueue.length;
    kpiTotalVal.innerHTML = `&#8377;${totalValue.toLocaleString('en-IN')}`;
}

function renderAlerts() {
    // Build Min-Heap
    const heap = new MinHeap();
    for (let p of productHashMap.values()) {
        if (p.quantity <= p.minThreshold) {
            heap.push(p);
        }
    }

    const sortedAlerts = [];
    while (heap.size() > 0) {
        sortedAlerts.push(heap.pop());
    }

    alertChipsContainer.innerHTML = '';
    alertCountBadge.textContent = `${sortedAlerts.length} Critical Items`;

    if (sortedAlerts.length === 0) {
        alertChipsContainer.innerHTML = '<span style="font-size: 0.86rem; color: #16a34a;">All items are well-stocked. No low-stock alerts!</span>';
        return;
    }

    sortedAlerts.forEach((item, idx) => {
        const chip = document.createElement('div');
        chip.className = 'alert-chip';
        chip.innerHTML = `
            <span class="chip-name"><strong>#${idx + 1}</strong> ${escapeHtml(item.name)}</span>
            <span class="chip-qty">Qty: ${item.quantity} (Limit: ${item.minThreshold})</span>
        `;
        alertChipsContainer.appendChild(chip);
    });
}

function renderOrderQueue() {
    orderQueueContainer.innerHTML = '';
    if (orderQueue.length === 0) {
        orderQueueContainer.innerHTML = '<span style="font-size: 0.82rem; color: #64748b; margin: auto;">FIFO Queue is empty. No pending orders.</span>';
        return;
    }

    orderQueue.forEach((order, index) => {
        const prod = productHashMap.get(order.productId);
        const prodName = prod ? prod.name : order.productId;
        const item = document.createElement('div');
        item.className = 'queue-item';
        item.innerHTML = `
            <div>
                <span class="queue-pos">#${index + 1}</span>
                <strong>${escapeHtml(order.orderId)}</strong> &bull; ${escapeHtml(order.customerName)}
                <div style="font-size: 0.76rem; color: #475569;">${escapeHtml(prodName)} &times; ${order.quantity} units</div>
            </div>
            <span style="font-size: 0.74rem; background: #dcfce7; color: #15803d; padding: 2px 6px; border-radius: 4px; font-weight: 600;">FIFO HEAD</span>
        `;
        if (index > 0) {
            item.querySelector('span:last-child').style.display = 'none';
        }
        orderQueueContainer.appendChild(item);
    });
}

function renderTransactionStack() {
    transactionStackContainer.innerHTML = '';
    if (transactionStack.length === 0) {
        transactionStackContainer.innerHTML = '<span style="font-size: 0.82rem; color: #64748b; margin: auto;">LIFO Stack is empty. No actions to undo.</span>';
        return;
    }

    // Display top 5 items from stack top (reverse view)
    for (let i = transactionStack.length - 1; i >= Math.max(0, transactionStack.length - 5); i--) {
        const tx = transactionStack[i];
        const prod = productHashMap.get(tx.productId);
        const name = prod ? prod.name : tx.productId;
        const item = document.createElement('div');
        item.className = 'stack-item';
        item.innerHTML = `
            <div>
                <strong>${escapeHtml(name)}</strong>
                <div style="font-size: 0.74rem; color: #475569;">${escapeHtml(tx.reason)}: Qty ${tx.oldQty} &rarr; ${tx.newQty}</div>
            </div>
            <span style="font-size: 0.72rem; color: #b45309; font-weight: 600;">${i === transactionStack.length - 1 ? 'STACK TOP' : ''}</span>
        `;
        transactionStackContainer.appendChild(item);
    }
}

function renderTable() {
    let list = Array.from(productHashMap.values());

    // Search filter
    const searchVal = searchInput.value.trim().toLowerCase();
    if (searchVal) {
        list = list.filter(p => p.productId.toLowerCase().includes(searchVal) || p.name.toLowerCase().includes(searchVal));
    }

    // Category filter
    const cat = categoryFilter.value;
    if (cat !== 'ALL') {
        list = list.filter(p => p.category === cat);
    }

    // Low stock filter
    if (activeFilter === 'low_stock') {
        list = list.filter(p => p.quantity <= p.minThreshold);
    }

    tableBody.innerHTML = '';
    tableCountLabel.textContent = `Showing ${list.length} products`;

    if (list.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding: 24px; color: #64748b;">No products matched the filter.</td></tr>`;
        return;
    }

    list.forEach(prod => {
        const tr = document.createElement('tr');
        let statusBadge = '';
        if (prod.quantity === 0) {
            statusBadge = `<span class="status-badge status-out-stock">Out of Stock</span>`;
        } else if (prod.quantity <= prod.minThreshold) {
            statusBadge = `<span class="status-badge status-low-stock">Low Stock (${prod.quantity})</span>`;
        } else {
            statusBadge = `<span class="status-badge status-in-stock">In Stock</span>`;
        }

        tr.innerHTML = `
            <td class="prod-id-cell">${escapeHtml(prod.productId)}</td>
            <td><strong>${escapeHtml(prod.name)}</strong></td>
            <td>${escapeHtml(prod.category)}</td>
            <td><strong>${prod.quantity}</strong> units</td>
            <td>${prod.minThreshold}</td>
            <td>&#8377;${prod.price.toLocaleString('en-IN')}</td>
            <td>${statusBadge}</td>
            <td>
                <div class="table-actions">
                    <button class="btn-tbl-action btn-edit-qty" onclick="handleEditStock('${prod.productId}')">Adjust Qty</button>
                    <button class="btn-tbl-action btn-delete" onclick="handleDeleteProduct('${prod.productId}')">Delete</button>
                </div>
            </td>
        `;
        tableBody.appendChild(tr);
    });
}

function populateProductSelect() {
    orderProductSelect.innerHTML = '';
    productHashMap.forEach(p => {
        const opt = document.createElement('option');
        opt.value = p.productId;
        opt.textContent = `${p.name} (Available: ${p.quantity})`;
        orderProductSelect.appendChild(opt);
    });
}

// --- 5. Interactive Operations ---

// Process Next Order from FIFO Queue
processOrderBtn.onclick = () => {
    if (orderQueue.length === 0) {
        alert("FIFO Queue is empty! Place an order first.");
        return;
    }

    const order = orderQueue[0];
    const product = productHashMap.get(order.productId);

    if (!product) {
        alert(`Product ${order.productId} does not exist in catalog.`);
        orderQueue.shift();
        render();
        return;
    }

    if (product.quantity < order.quantity) {
        alert(`Insufficient stock! ${product.name} only has ${product.quantity} units, but order requires ${order.quantity}.`);
        return;
    }

    // Dequeue order
    orderQueue.shift();
    const oldQty = product.quantity;
    product.quantity -= order.quantity;

    // Push transaction to LIFO Stack
    transactionStack.push({
        productId: product.productId,
        oldQty: oldQty,
        newQty: product.quantity,
        reason: `Order ${order.orderId} Dispatched`
    });

    alert(`Order ${order.orderId} processed successfully! Stock decremented for ${product.name}.`);
    render();
};

// Undo Last Transaction from LIFO Stack
undoBtn.onclick = () => {
    if (transactionStack.length === 0) {
        alert("Transaction stack is empty! No actions to undo.");
        return;
    }

    const lastTx = transactionStack.pop();
    const product = productHashMap.get(lastTx.productId);

    if (product) {
        product.quantity = lastTx.oldQty;
        alert(`Undo Successful! Restored ${product.name} stock back to ${lastTx.oldQty}.`);
        render();
    }
};

// Adjust Stock manually
window.handleEditStock = (id) => {
    const prod = productHashMap.get(id);
    if (!prod) return;
    const input = prompt(`Enter new stock quantity for ${prod.name} (Current: ${prod.quantity}):`, prod.quantity);
    if (input !== null) {
        const val = parseInt(input);
        if (!isNaN(val) && val >= 0) {
            transactionStack.push({
                productId: prod.productId,
                oldQty: prod.quantity,
                newQty: val,
                reason: "Manual Stock Adjustment"
            });
            prod.quantity = val;
            render();
        } else {
            alert("Please enter a valid non-negative integer.");
        }
    }
};

window.handleDeleteProduct = (id) => {
    const prod = productHashMap.get(id);
    if (!prod) return;
    if (confirm(`Are you sure you want to delete ${prod.name} (${prod.productId})?`)) {
        productHashMap.delete(id);
        render();
    }
};

// Search & Filter Events
searchInput.addEventListener('input', () => {
    clearSearchBtn.style.display = searchInput.value ? 'block' : 'none';
    renderTable();
});

clearSearchBtn.onclick = () => {
    searchInput.value = '';
    clearSearchBtn.style.display = 'none';
    renderTable();
};

categoryFilter.onchange = () => renderTable();

filterLowStockBtn.onclick = () => {
    activeFilter = 'low_stock';
    renderTable();
};

resetFilterBtn.onclick = () => {
    activeFilter = 'all';
    searchInput.value = '';
    categoryFilter.value = 'ALL';
    clearSearchBtn.style.display = 'none';
    renderTable();
};

// Modal Logic
openAddModalBtn.onclick = () => addProductModal.style.display = 'flex';
closeAddModalBtn.onclick = () => addProductModal.style.display = 'none';
cancelAddBtn.onclick = () => addProductModal.style.display = 'none';

addProductForm.onsubmit = (e) => {
    e.preventDefault();
    const id = document.getElementById('newProdId').value.trim().toUpperCase();
    const name = document.getElementById('newProdName').value.trim();
    const category = document.getElementById('newProdCategory').value.trim();
    const qty = parseInt(document.getElementById('newProdQty').value);
    const threshold = parseInt(document.getElementById('newProdThreshold').value);
    const price = parseFloat(document.getElementById('newProdPrice').value);

    if (productHashMap.has(id)) {
        alert(`Product ID ${id} already exists in catalog. Please use a unique SKU.`);
        return;
    }

    productHashMap.set(id, { productId: id, name, category, quantity: qty, minThreshold: threshold, price });
    addProductModal.style.display = 'none';
    addProductForm.reset();
    render();
};

openOrderModalBtn.onclick = () => {
    populateProductSelect();
    placeOrderModal.style.display = 'flex';
};
closeOrderModalBtn.onclick = () => placeOrderModal.style.display = 'none';
cancelOrderBtn.onclick = () => placeOrderModal.style.display = 'none';

placeOrderForm.onsubmit = (e) => {
    e.preventDefault();
    const customer = document.getElementById('orderCustomerName').value.trim();
    const prodId = orderProductSelect.value;
    const qty = parseInt(document.getElementById('orderQuantity').value);
    const newOrderId = "ORD-" + Math.floor(1000 + Math.random() * 9000);

    orderQueue.push({
        orderId: newOrderId,
        productId: prodId,
        quantity: qty,
        customerName: customer,
        timestamp: "Just now"
    });

    placeOrderModal.style.display = 'none';
    placeOrderForm.reset();
    render();
    alert(`Order ${newOrderId} placed and added to FIFO Queue!`);
};

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

// Initial Boot
render();
