package jX;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Lưu ý: Tên DB phải khớp với trong SQL vừa chạy
    private static final String URL = "jdbc:mysql://localhost:3306/ticket_booking_db";
    private static final String USER = "root"; 
    private static final String PASS = "243231"; // Pass của bạn trong ảnh

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Chạy thử file này để xem kết nối được chưa
    public static void main(String[] args) {
        if (getConnection() != null) {
            System.out.println("✅ KẾT NỐI THÀNH CÔNG!");
        } else {
            System.out.println("❌ KẾT NỐI THẤT BẠI! Kiểm tra lại tên DB/Pass.");
        }
    }
}