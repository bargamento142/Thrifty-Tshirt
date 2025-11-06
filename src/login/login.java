package login;

import config.dbconnector;
import java.util.*;
import thrifty.tshirt.admin_dashboard;
import thrifty.tshirt.user_dashboard;

public class login {
    private dbconnector db = new dbconnector();

    public void loginUser() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Thrifty T-Shirt System =====");
            System.out.print("Enter username: ");
            String username = sc.nextLine();

            System.out.print("Enter password: ");
            String password = sc.nextLine();

            // Fetch user by username
            List<Map<String, Object>> users = db.fetchRecords("SELECT * FROM users WHERE username = ?", username);

            if (users.isEmpty()) {
                System.out.println("❌ Invalid username.");
                continue; // ask username again
            }

            Map<String, Object> user = users.get(0);
            String storedHash = (String) user.get("password");
            String hashedPassword = dbconnector.hashPassword(password);

            // check both username + password
            if (!storedHash.equals(hashedPassword)) {
                System.out.println("❌ Invalid password.");
                // loop for password input until correct
                while (true) {
                    System.out.print("Enter password: ");
                    password = sc.nextLine();
                    hashedPassword = dbconnector.hashPassword(password);
                    if (storedHash.equals(hashedPassword)) break;
                    System.out.println("❌ Invalid password.");
                }
            }

            // if username and password are correct:
            String role = (String) user.get("role");
            String status = (String) user.get("status");
            int userId = (int) user.get("id"); // ✅ store user id for user_dashboard

            if (!"active".equalsIgnoreCase(status)) {
                System.out.println("⚠️ Account not yet active. Please wait for admin approval.");
                return;
            }

            String displayRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
            System.out.println("Login successful! Welcome, " + username + " (" + displayRole + ")");


            // Redirect based on role
            if ("admin".equalsIgnoreCase(role)) {
                admin_dashboard admin = new admin_dashboard(username);
                admin.showAdminMenu();

            } else if ("user".equalsIgnoreCase(role)) {
                user_dashboard userDash = new user_dashboard();
                userDash.showMenu(userId); // ✅ Correct method for user dashboard

            } else {
                System.out.println("❌ Unknown role assigned. Contact administrator.");
            }

            break; // stop login loop after successful login
        }
    }

    public void forgotPassword() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your registered email: ");
        String email = sc.nextLine();

        List<Map<String, Object>> result = db.fetchRecords("SELECT * FROM users WHERE email = ?", email);

        if (result.isEmpty()) {
            System.out.println("❌ Email not found!");
            return;
        }

        System.out.print("Enter your new password: ");
        String newPassword = sc.nextLine();
        String hashedNew = dbconnector.hashPassword(newPassword);

        db.updateRecord("UPDATE users SET password = ? WHERE email = ?", hashedNew, email);
        System.out.println("✅ Password updated successfully!");
    }
}
