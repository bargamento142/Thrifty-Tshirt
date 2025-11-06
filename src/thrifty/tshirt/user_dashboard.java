package thrifty.tshirt;

import config.dbconnector;
import java.sql.*;
import java.util.*;

public class user_dashboard {

    private Connection con;
    private Map<Integer, Integer> cart = new HashMap<>();

    public user_dashboard() {
        con = dbconnector.connectDB();
    }

    private String getUsername(int customerId) {
        String username = "User";
        try {
            PreparedStatement ps = con.prepareStatement("SELECT name FROM customers WHERE id = ?");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) username = rs.getString("name");
        } catch (SQLException e) {
            System.out.println("❌ Error fetching username: " + e.getMessage());
        }
        return username;
    }

    public void showMenu(int customerId) {
        Scanner sc = new Scanner(System.in);
        int choice;
        String username = getUsername(customerId);

        do {
            System.out.println("\n====================================");
            System.out.println("👕 Welcome, " + username + "!");
            System.out.println("========== USER DASHBOARD ==========");
            System.out.println("1. View & Order T-Shirts");
            System.out.println("2. Manage Cart");
            System.out.println("3. View My Orders");
            System.out.println("4. Suggested T-Shirts");
            System.out.println("5. Exit");
            System.out.println("====================================");
            System.out.print("Enter choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("❌ Invalid input. Enter a number: ");
                sc.next();
            }

            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    viewAndOrder(customerId);
                    break;
                case 2:
                    manageCart(customerId);
                    break;
                case 3:
                    viewOrders(customerId);
                    break;
                case 4:
                    suggestTshirts();
                    break;
                case 5:
                    System.out.println("Logging out... 👋");
                    break;
                default:
                    System.out.println("❌ Invalid choice!");
                    break;
            }
        } while (choice != 5);
    }

    // ✅ Combines View + Add to Cart + Direct Checkout
    private void viewAndOrder(int customerId) {
        Scanner sc = new Scanner(System.in);

        try {
            String query = "SELECT * FROM tshirts WHERE stock > 0";
            ResultSet rs = con.prepareStatement(query).executeQuery();

            System.out.println("\n=== 🧾 Available T-Shirts ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Brand: " + rs.getString("brand"));
                System.out.println("Product: " + rs.getString("product_name"));
                System.out.println("Color: " + rs.getString("color"));
                System.out.println("Size: " + rs.getString("size"));
                System.out.println("Price: ₱" + rs.getDouble("price"));
                System.out.println("Stock: " + rs.getInt("stock"));
                System.out.println("---------------------------");
            }

            System.out.print("\nEnter T-shirt ID to select (0 to cancel): ");
            int id = sc.nextInt();
            if (id == 0) return;

            System.out.print("Enter quantity: ");
            int qty = sc.nextInt();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM tshirts WHERE id = ?");
            ps.setInt(1, id);
            ResultSet product = ps.executeQuery();

            if (!product.next()) {
                System.out.println("❌ Product not found!");
                return;
            }

            int stock = product.getInt("stock");
            String productName = product.getString("product_name");
            if (qty > stock) {
                System.out.println("❌ Not enough stock available!");
                return;
            }

            System.out.println("\n1️⃣ Add to Cart");
            System.out.println("2️⃣ Checkout Now");
            System.out.print("Choose an action: ");
            int action = sc.nextInt();

            if (action == 1) {
                cart.put(id, cart.getOrDefault(id, 0) + qty);
                System.out.println("🛒 " + productName + " added to cart!");
            } else if (action == 2) {
                checkoutSingle(customerId, id, qty);
            } else {
                System.out.println("❌ Invalid choice.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error viewing T-shirts: " + e.getMessage());
        }
    }

    private void checkoutSingle(int customerId, int tshirtId, int qty) {
        try {
            con.setAutoCommit(false);

            PreparedStatement insert = con.prepareStatement(
                "INSERT INTO orders (customer_id, tshirt_id, quantity, status) VALUES (?, ?, ?, 'Pending')"
            );
            insert.setInt(1, customerId);
            insert.setInt(2, tshirtId);
            insert.setInt(3, qty);
            insert.executeUpdate();

            PreparedStatement update = con.prepareStatement(
                "UPDATE tshirts SET stock = stock - ? WHERE id = ?"
            );
            update.setInt(1, qty);
            update.setInt(2, tshirtId);
            update.executeUpdate();

            con.commit();
            System.out.println("✅ Order placed successfully! Pending admin approval.");

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            System.out.println("❌ Checkout failed: " + e.getMessage());
        }
    }

    // ✅ Manage cart (Java 8–compatible switch)
    private void manageCart(int customerId) {
        Scanner sc = new Scanner(System.in);

        if (cart.isEmpty()) {
            System.out.println("🛍️ Your cart is empty.");
            return;
        }

        double total = 0;
        System.out.println("\n=== 🛒 Your Cart ===");
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            int id = entry.getKey();
            int qty = entry.getValue();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT product_name, price FROM tshirts WHERE id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double subtotal = rs.getDouble("price") * qty;
                    total += subtotal;
                    System.out.println(id + " | " + rs.getString("product_name") + " | Qty: " + qty + " | ₱" + subtotal);
                }
            } catch (SQLException e) {
                System.out.println("❌ Error reading cart item: " + e.getMessage());
            }
        }

        System.out.println("🧾 Total: ₱" + total);
        System.out.println("\n1️⃣ Edit Quantity");
        System.out.println("2️⃣ Remove Item");
        System.out.println("3️⃣ Checkout All");
        System.out.println("4️⃣ Back");
        System.out.print("Choose action: ");

        int choice = sc.nextInt();

        switch (choice) {
            case 1:
                System.out.print("Enter T-shirt ID to edit: ");
                int editId = sc.nextInt();
                if (cart.containsKey(editId)) {
                    System.out.print("New quantity: ");
                    cart.put(editId, sc.nextInt());
                    System.out.println("✅ Quantity updated!");
                } else {
                    System.out.println("❌ Item not found.");
                }
                break;

            case 2:
                System.out.print("Enter T-shirt ID to remove: ");
                int removeId = sc.nextInt();
                if (cart.remove(removeId) != null)
                    System.out.println("🗑️ Item removed!");
                else
                    System.out.println("❌ Item not found.");
                break;

            case 3:
                checkout(customerId);
                break;

            case 4:
                break;

            default:
                System.out.println("❌ Invalid choice.");
                break;
        }
    }

    private void checkout(int customerId) {
        if (cart.isEmpty()) {
            System.out.println("🛒 Cart is empty!");
            return;
        }

        try {
            con.setAutoCommit(false);

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO orders (customer_id, tshirt_id, quantity, status) VALUES (?, ?, ?, 'Pending')"
                );
                insert.setInt(1, customerId);
                insert.setInt(2, entry.getKey());
                insert.setInt(3, entry.getValue());
                insert.executeUpdate();

                PreparedStatement update = con.prepareStatement(
                    "UPDATE tshirts SET stock = stock - ? WHERE id = ?"
                );
                update.setInt(1, entry.getValue());
                update.setInt(2, entry.getKey());
                update.executeUpdate();
            }

            con.commit();
            cart.clear();
            System.out.println("✅ All items checked out successfully!");

        } catch (SQLException e) {
            try { con.rollback(); } catch (SQLException ignored) {}
            System.out.println("❌ Checkout failed: " + e.getMessage());
        }
    }

    private void viewOrders(int customerId) {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT o.id, t.product_name, o.quantity, o.status " +
                "FROM orders o " +
                "JOIN tshirts t ON o.tshirt_id = t.id " +
                "WHERE o.customer_id = ?"
            );
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== 📦 My Orders ===");
            boolean hasOrders = false;
            while (rs.next()) {
                hasOrders = true;
                System.out.println("Order ID: " + rs.getInt("id"));
                System.out.println("Product: " + rs.getString("product_name"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("---------------------------");
            }
            if (!hasOrders) System.out.println("You have no orders yet.");

        } catch (SQLException e) {
            System.out.println("❌ Error viewing orders: " + e.getMessage());
        }
    }

    private void suggestTshirts() {
        try {
            String query = "SELECT t.brand, COUNT(*) AS total_orders " +
                           "FROM orders o JOIN tshirts t ON o.tshirt_id = t.id " +
                           "GROUP BY t.brand ORDER BY total_orders DESC LIMIT 1";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String topBrand = rs.getString("brand");
                System.out.println("\n🔥 Suggested from brand: " + topBrand);

                PreparedStatement suggestPs = con.prepareStatement("SELECT * FROM tshirts WHERE brand = ?");
                suggestPs.setString(1, topBrand);
                ResultSet suggestRs = suggestPs.executeQuery();

                while (suggestRs.next()) {
                    System.out.println("→ " + suggestRs.getString("product_name") + " (₱" + suggestRs.getDouble("price") + ")");
                }
            } else {
                System.out.println("No suggestions yet.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching suggestions: " + e.getMessage());
        }
    }
}
