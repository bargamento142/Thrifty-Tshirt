package login;

import config.dbconnector;
import java.util.Scanner;

public class register {
    private dbconnector db = new dbconnector();

    public void createAccount() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String hashedPassword = dbconnector.hashPassword(password);

        // Default values
        String role = "user";
        String status = "pending";

        // ✅ Prevent duplicate username/email
        String checkSql = "SELECT * FROM users WHERE username = ? OR email = ?";
        if (!db.fetchRecords(checkSql, username, email).isEmpty()) {
            System.out.println("❌ Username or email already exists!");
            return;
        }

        // ✅ Insert record
        String sql = "INSERT INTO users (username, email, password, role, status) VALUES (?, ?, ?, ?, ?)";
        db.addRecord(sql, username, email, hashedPassword, role, status);

        System.out.println("✅ Account created successfully! Please wait for admin approval.");
    }
}
