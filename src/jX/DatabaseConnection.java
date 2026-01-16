package jX; // Khai báo file này nằm trong gói (folder) tên là 'jX' để quản lý code gọn gàng.

// --- NHẬP THƯ VIỆN (IMPORT) ---
import java.sql.Connection;     // Interface đại diện cho một phiên kết nối tới Database.
import java.sql.DriverManager;  // Lớp quản lý danh sách các Database Driver (trình điều khiển).
import java.sql.SQLException;   // Lớp xử lý các lỗi liên quan đến SQL (sai pass, sai câu lệnh...).

public class DatabaseConnection {
    // --- KHAI BÁO HẰNG SỐ CẤU HÌNH (CONSTANTS) ---
    // 'private': Chỉ dùng nội bộ trong class này, bảo mật thông tin.
    // 'static final': Biến tĩnh (dùng chung, không cần tạo object) và không thể thay đổi giá trị (hằng số).
    
    // Chuỗi kết nối JDBC: quy định giao thức (jdbc:mysql), máy chủ (localhost), cổng (3306), tên DB.
    // Nếu bạn đổi tên DB, chỉ cần sửa dòng này.
    private static final String URL = "jdbc:mysql://localhost:3306/ticket_booking_db";
    
    private static final String USER = "root";   // Tên đăng nhập mặc định của XAMPP/MySQL là 'root'.
    private static final String PASS = "243231"; // Mật khẩu database của bạn.

    // --- HÀM LẤY KẾT NỐI (CORE FUNCTION) ---
    // 'public static': Để các file khác có thể gọi trực tiếp bằng DatabaseConnection.getConnection() mà không cần 'new DatabaseConnection()'.
    public static Connection getConnection() {
        Connection conn = null; // Khởi tạo biến kết nối là null (chưa có gì).
        try {
            // Bước 1: Nạp Driver vào bộ nhớ Java.
            // Dòng này giúp Java biết cách "nói chuyện" với MySQL. Nếu thiếu thư viện .jar, dòng này sẽ báo lỗi.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Bước 2: Yêu cầu DriverManager mở kết nối.
            // Nó sẽ gửi user/pass tới MySQL Server. Nếu đúng -> trả về đối tượng Connection.
            conn = DriverManager.getConnection(URL, USER, PASS);
            
        } catch (ClassNotFoundException e) {
            // Lỗi này xảy ra nếu bạn quên add file thư viện mysql-connector-java.jar vào dự án.
            System.out.println("❌ Lỗi: Không tìm thấy Driver MySQL!");
            e.printStackTrace(); // In chi tiết lỗi đỏ lòm ra console để debug.
        } catch (SQLException e) {
            // Lỗi này xảy ra nếu sai tên DB, sai User/Pass, hoặc chưa bật XAMPP.
            System.out.println("❌ Lỗi: Không thể kết nối tới Database!");
            e.printStackTrace();
        }
        return conn; // Trả về kết nối (để các file khác dùng tạo câu lệnh SQL).
    }

    // --- HÀM MAIN (UNIT TEST) ---
    // Hàm này chỉ dùng để chạy kiểm tra riêng file này xem code kết nối có đúng không.
    public static void main(String[] args) {
        // Gọi hàm getConnection(). Nếu khác null nghĩa là kết nối thành công.
        if (getConnection() != null) {
            System.out.println("✅ KẾT NỐI THÀNH CÔNG! (Database đã sẵn sàng)");
        } else {
            System.out.println("❌ KẾT NỐI THẤT BẠI! (Kiểm tra lại XAMPP hoặc Password)");
        }
    }
}
