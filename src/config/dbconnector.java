package config;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class dbconnector {

    // ✅ 1. SQLite Database Connection
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:store.db"); // Database file (compatible with SQLiteStudio)
            // System.out.println("✅ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ SQLite JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed: " + e.getMessage());
        }
        return con;
    }

    // ✅ 2. Hash Password (SHA-256)
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b)); // convert to hex
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("❌ Error hashing password: " + e.getMessage());
            return null;
        }
    }

    // ✅ 3. Add Record (INSERT)
    public void addRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

            pstmt.executeUpdate();
            System.out.println("✅ Record added successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error adding record: " + e.getMessage());
        }
    }

    // ✅ 4. Update Record (UPDATE)
    public int updateRecord(String sql, Object... params) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Record updated successfully!");
            } else {
                System.out.println("⚠️ No records were affected.");
            }

            return rows;

        } catch (SQLException e) {
            System.out.println("❌ Error updating record: " + e.getMessage());
            return 0; // 0 means failed
        }
    }


    // ✅ 5. Delete Record (DELETE)
    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

            pstmt.executeUpdate();
            System.out.println("✅ Record deleted successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error deleting record: " + e.getMessage());
        }
    }

    // ✅ 6. Fetch Records (SELECT, returns List<Map>)
    public List<Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                records.add(row);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching records: " + e.getMessage());
        }

        return records;
    }
}
