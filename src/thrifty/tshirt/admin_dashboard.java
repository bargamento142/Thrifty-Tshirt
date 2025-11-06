package thrifty.tshirt;

import config.dbconnector;
import static java.awt.Event.LEFT;
import java.sql.*;
import java.util.*;

public class admin_dashboard {
    private dbconnector db = new dbconnector();
    private String adminUsername;

    public admin_dashboard(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public void showAdminMenu() {
        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n========== ADMIN DASHBOARD ==========");
            System.out.println("👤 Logged in as: " + adminUsername);
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Products");
            System.out.println("3. View & Update Orders"); // ✅ renamed
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    manageUsers();
                    break;
                case 2:
                    manageProducts();
                    break;
                case 3:
                    viewOrders(); // ✅ now handles viewing + updating
                    break;
                case 4:
                    System.out.println("👋 Logging out...");
                    break;
                default:
                    System.out.println("❌ Invalid option. Try again.");
            }
        } while (choice != 4);
    }


    // ==========================================================
    // 1. MANAGE USERS
    // ==========================================================
    private void manageUsers() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n========== MANAGE USERS ==========");
            System.out.println("1. View All Users");
            System.out.println("2. Show Pending Users");
            System.out.println("3. Approve (Activate) Account");
            System.out.println("4. Add User");
            System.out.println("5. Edit User");
            System.out.println("6. Delete User");
            System.out.println("7. Back");
            System.out.print("Choose an option: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    showPendingUsers();
                    break;
                case 3:
                    approveAccount();
                    break;
                case 4:
                    addUser();
                    break;
                case 5:
                    editUser();
                    break;
                case 6:
                    deleteUser();
                    break;
                case 7:
                    System.out.println("🔙 Returning to Admin Dashboard...");
                    break;
                default:
                    System.out.println("❌ Invalid option. Try again.");
            }
        } while (choice != 7);
    }

    private void viewAllUsers() {
        String sql = "SELECT id, username, email, role, status FROM users";
        List<Map<String, Object>> users = db.fetchRecords(sql);
        System.out.println("\n========== ALL USERS ==========");
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        for (Map<String, Object> user : users) {
            System.out.println(
                "ID: " + user.get("id") +
                " | Username: " + user.get("username") +
                " | Email: " + user.get("email") +
                " | Role: " + user.get("role") +
                " | Status: " + user.get("status")
            );
        }
    }

    private void showPendingUsers() {
        String sql = "SELECT id, username, email FROM users WHERE status = 'pending'";
        List<Map<String, Object>> pending = db.fetchRecords(sql);
        System.out.println("\n========== PENDING USERS ==========");
        if (pending.isEmpty()) {
            System.out.println("No pending users found.");
            return;
        }
        for (Map<String, Object> user : pending) {
            System.out.println("ID: " + user.get("id") + " | Username: " + user.get("username") + " | Email: " + user.get("email"));
        }
    }

    private void approveAccount() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter User ID to activate: ");
        int userId = sc.nextInt();
        sc.nextLine();

        int rows = db.updateRecord("UPDATE users SET status = 'active' WHERE id = ?", userId);
        if (rows > 0)
            System.out.println("✅ Account activated successfully!");
        else
            System.out.println("❌ No user found with ID: " + userId);
    }

    private void addUser() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();
        System.out.print("Enter role (admin/user): ");
        String role = sc.nextLine();

        String hashed = dbconnector.hashPassword(password);
        db.updateRecord("INSERT INTO users (username, email, password, role, status) VALUES (?, ?, ?, ?, 'active')",
                username, email, hashed, role);
        System.out.println("✅ User added successfully!");
    }

    private void editUser() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter User ID to edit: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter new email: ");
        String email = sc.nextLine();
        System.out.print("Enter new role: ");
        String role = sc.nextLine();
        System.out.print("Enter new status: ");
        String status = sc.nextLine();
        db.updateRecord("UPDATE users SET email = ?, role = ?, status = ? WHERE id = ?", email, role, status, id);
        System.out.println("✅ User updated successfully!");
    }

    private void deleteUser() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter User ID to delete: ");
        int id = sc.nextInt();
        db.updateRecord("DELETE FROM users WHERE id = ?", id);
        System.out.println("🗑️ User deleted successfully!");
    }

    // ==========================================================
    // 2. MANAGE PRODUCTS
    // ==========================================================
    private void manageProducts() {
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n========== MANAGE PRODUCTS ==========");
            System.out.println("1. View All Products");
            System.out.println("2. Add Product");
            System.out.println("3. Edit Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Back");
            System.out.print("Choose an option: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    viewProducts();
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    editProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 5:
                    System.out.println("🔙 Returning to Admin Dashboard...");
                    break;
                default:
                    System.out.println("❌ Invalid option. Try again.");
            }
        } while (choice != 5);
    }

    private void viewProducts() {
        String sql = "SELECT * FROM tshirts";
        List<Map<String, Object>> products = db.fetchRecords(sql);
        System.out.println("\n========== PRODUCT LIST ==========");
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        for (Map<String, Object> p : products) {
            System.out.println("ID: " + p.get("id") +
                    " | Brand: " + p.get("brand") +
                    " | Product: " + p.get("product_name") +
                    " | Price: ₱" + p.get("price") +
                    " | Stock: " + p.get("stock"));
        }
    }

    public void addProduct() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter brand: ");
        String brand = sc.nextLine();
        System.out.print("Enter size: ");
        String size = sc.nextLine();
        System.out.print("Enter color: ");
        String color = sc.nextLine();
        System.out.print("Enter product name: ");
        String productName = sc.nextLine();

        double price;
        int stock;

        try {
            System.out.print("Enter price: ");
            price = Double.parseDouble(sc.nextLine());
            System.out.print("Enter stock quantity: ");
            stock = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
            return;
        }

        String sql = "INSERT INTO tshirts (brand, size, color, product_name, price, stock) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbconnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, brand);
            pstmt.setString(2, size);
            pstmt.setString(3, color);
            pstmt.setString(4, productName);
            pstmt.setDouble(5, price);
            pstmt.setInt(6, stock);
            pstmt.executeUpdate();
            System.out.println("✅ Product added successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error inserting product: " + e.getMessage());
        }
    }

    private void editProduct() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Product ID to edit: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter new product name: ");
        String name = sc.nextLine();
        System.out.print("Enter new price: ");
        double price = sc.nextDouble();
        System.out.print("Enter new stock: ");
        int stock = sc.nextInt();

        db.updateRecord("UPDATE tshirts SET product_name = ?, price = ?, stock = ? WHERE id = ?", name, price, stock, id);
        System.out.println("✅ Product updated successfully!");
    }

    private void deleteProduct() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Product ID to delete: ");
        int id = sc.nextInt();
        db.updateRecord("DELETE FROM tshirts WHERE id = ?", id);
        System.out.println("🗑️ Product deleted successfully!");
    }

    // ==========================================================
    // 3. VIEW & UPDATE ORDERS
    // ==========================================================
    private void viewOrders() {
        Scanner sc = new Scanner(System.in);

        String sql =
            "SELECT o.id, " +
            "COALESCE(c.name, 'Unknown Customer') AS customer, " +
            "COALESCE(c.contact_number, 'N/A') AS contact, " +
            "t.product_name, " +
            "o.quantity, " +
            "o.status " +
            "FROM orders o " +
            "LEFT JOIN customers c ON o.customer_id = c.id " +
            "JOIN tshirts t ON o.tshirt_id = t.id " +
            "ORDER BY o.id DESC";

        List<Map<String, Object>> orders = db.fetchRecords(sql);
        System.out.println("\n========== ALL ORDERS ==========");
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        for (Map<String, Object> o : orders) {
            System.out.println("Order ID: " + o.get("id")
                    + " | Customer: " + o.get("customer")
                    + " | Contact: " + o.get("contact")
                    + " | Product: " + o.get("product_name")
                    + " | Qty: " + o.get("quantity")
                    + " | Status: " + o.get("status"));
        }

        System.out.print("\nWould you like to update an order status? (y/n): ");
        String choice = sc.nextLine();

        if (choice.equalsIgnoreCase("y")) {
            System.out.print("Enter Order ID to update: ");
            int id = sc.nextInt();
            sc.nextLine(); // consume newline
            System.out.print("Enter new status (Pending / Approved / Shipped / Completed / Cancelled): ");
            String status = sc.nextLine();

            int rows = db.updateRecord("UPDATE orders SET status = ? WHERE id = ?", status, id);
            if (rows > 0)
                System.out.println("✅ Order status updated successfully!");
            else
                System.out.println("❌ Order not found.");
        } else {
            System.out.println("🔙 Returning to Admin Dashboard...");
        }
    }




    private void updateOrderStatus() {
        Scanner sc = new Scanner(System.in);
        viewOrders();
        System.out.print("\nEnter Order ID to update: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter new status (Pending / Approved / Shipped / Completed / Cancelled): ");
        String status = sc.nextLine();

        int rows = db.updateRecord("UPDATE orders SET status = ? WHERE id = ?", status, id);
        if (rows > 0)
            System.out.println("✅ Order status updated successfully!");
        else
            System.out.println("❌ Order not found.");
    }
}
