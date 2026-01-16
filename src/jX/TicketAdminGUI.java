package jX; // [LỢI ÍCH]: Giữ cấu trúc dự án ngăn nắp.

// --- IMPORT THƯ VIỆN ---
import javax.swing.*; // [LỢI ÍCH]: Các thành phần giao diện (Bảng, Nút, Tab...).
import javax.swing.border.*; // [LỢI ÍCH]: Tạo khoảng cách lề (Padding/Margin) cho đẹp.
import javax.swing.table.DefaultTableCellRenderer; // [LỢI ÍCH]: Dùng để tùy chỉnh cách hiển thị ô bảng (VD: Che mật khẩu).
import javax.swing.table.DefaultTableModel; // [LỢI ÍCH]: Quản lý dữ liệu bên trong bảng (Thêm/Xóa dòng).
import java.awt.*; // [LỢI ÍCH]: Layout, Màu sắc, Font chữ.
import java.sql.*; // [LỢI ÍCH]: Kết nối Database để lấy danh sách User/Vé.
import java.text.SimpleDateFormat; // [LỢI ÍCH]: Format ngày giờ từ dạng thô (Timestamp) sang dạng đẹp.

public class TicketAdminGUI extends JFrame {

    private final Color COLOR_PRIMARY = new Color(44, 62, 80); // Màu xanh đậm đặc trưng của trang Admin
    
    // [LỢI ÍCH]: JTabbedPane tạo ra các thẻ tab chuyển đổi (giống trình duyệt web).
    // [NẾU XOÁ]: Không thể chia giao diện thành 2 phần User và Booking, màn hình sẽ rất rối.
    private JTabbedPane tabbedPane;
    
    // Các biến toàn cục để truy cập được từ nhiều hàm khác nhau
    private JTable userTable;
    private DefaultTableModel userModel;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> cbRole;

    private JTable bookingTable;
    private DefaultTableModel bookingModel;
    
    // [LỢI ÍCH]: Định dạng ngày giờ chuẩn Việt Nam (Ngày/Tháng/Năm Giờ:Phút).
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TicketAdminGUI() {
        // [LỢI ÍCH]: Cài đặt cơ bản cho cửa sổ Admin.
        setTitle("ADMIN DASHBOARD - Quản Lý Hệ Thống");
        setSize(1100, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Thêm thanh tiêu đề (Header) ở trên cùng
        add(initHeader(), BorderLayout.NORTH);

        // [LỢI ÍCH]: Khởi tạo Tab Panel.
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // [LỢI ÍCH]: Thêm 2 tab chức năng chính.
        tabbedPane.addTab("QUẢN LÝ USER", initUserTab());
        tabbedPane.addTab("DUYỆT ĐƠN VÉ", initBookingTab());

        add(tabbedPane, BorderLayout.CENTER);
        
        // [QUAN TRỌNG]: Tải dữ liệu ngay khi mở ứng dụng.
        // [NẾU XOÁ]: Mở lên bảng sẽ trắng trơn, Admin phải bấm nút refresh mới thấy dữ liệu -> Trải nghiệm kém.
        loadUsers();    
        loadBookings(); 
    }

    // --- THANH TIÊU ĐỀ & ĐĂNG XUẤT ---
    private JPanel initHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_PRIMARY); 
        p.setPreferredSize(new Dimension(getWidth(), 60));
        p.setBorder(new EmptyBorder(0, 20, 0, 20)); // Padding 2 bên

        JLabel l = new JLabel("QUẢN TRỊ HỆ THỐNG"); 
        l.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
        l.setForeground(Color.WHITE);
        
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(231, 76, 60)); // Màu đỏ báo hiệu nút thoát
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        
        // [LỢI ÍCH]: Xử lý đăng xuất an toàn.
        btnLogout.addActionListener(e -> {
            this.dispose(); // Tắt màn hình Admin
            try { new TicketLoginGUI().setVisible(true); } catch (Exception ex) {} // Mở lại màn hình Login
        });
        
        p.add(l, BorderLayout.WEST); 
        p.add(btnLogout, BorderLayout.EAST);
        return p;
    }

    // ================= TAB 1: QUẢN LÝ USER (Có tính năng Che mật khẩu) =================
    private JPanel initUserTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Khu vực nhập liệu (Input Form)
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10)); // Grid 4 dòng 2 cột
        inputPanel.setBorder(new EmptyBorder(20, 100, 20, 100)); // Căn lề rộng cho đẹp
        
        txtUser = new JTextField(); 
        txtPass = new JPasswordField();
        cbRole = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN"}); // Combobox chọn quyền
        
        inputPanel.add(new JLabel("Tên đăng nhập:")); inputPanel.add(txtUser);
        inputPanel.add(new JLabel("Mật khẩu:")); inputPanel.add(txtPass);
        inputPanel.add(new JLabel("Vai trò:")); inputPanel.add(cbRole);
        
        // Nút Thêm User
        JButton btnAdd = new JButton("Thêm User");
        btnAdd.setBackground(new Color(39, 174, 96)); btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addUser()); // Gọi hàm thêm vào DB
        
        // Nút Xóa User
        JButton btnDel = new JButton("Xóa User");
        btnDel.setBackground(new Color(192, 57, 43)); btnDel.setForeground(Color.WHITE);
        btnDel.addActionListener(e -> deleteUser()); // Gọi hàm xóa khỏi DB

        JPanel btnP = new JPanel(); btnP.add(btnAdd); btnP.add(btnDel);
        
        JPanel top = new JPanel(new BorderLayout());
        top.add(inputPanel, BorderLayout.CENTER); top.add(btnP, BorderLayout.SOUTH);

        // [QUAN TRỌNG]: Khởi tạo Bảng hiển thị User
        userModel = new DefaultTableModel(new String[]{"Username", "Password", "Role", "SĐT"}, 0);
        userTable = new JTable(userModel);
        
        // [LỢI ÍCH]: Kỹ thuật CellRenderer để che mật khẩu.
        // Thay vì hiện "123456", nó sẽ vẽ đè lên thành "••••••".
        // [NẾU XOÁ]: Mật khẩu của khách hàng sẽ hiện rõ mồn một -> Lộ thông tin bảo mật nghiêm trọng.
        userTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText("••••••"); // Luôn hiển thị dấu chấm
                return this;
            }
        });
        
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    // ================= TAB 2: DUYỆT ĐƠN VÉ =================
    private JPanel initBookingTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Cấu hình bảng Booking
        String[] columns = {"ID", "Khách hàng", "Chuyến đi", "Giá vé", "Thời gian", "Trạng thái"};
        bookingModel = new DefaultTableModel(columns, 0);
        bookingTable = new JTable(bookingModel);
        
        // [LỢI ÍCH]: Tăng chiều cao dòng để dễ nhìn hơn.
        bookingTable.setRowHeight(30); 
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Nút Duyệt Đơn
        JButton btnApprove = new JButton("XÁC NHẬN THANH TOÁN (DUYỆT ĐƠN)");
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnApprove.setBackground(new Color(41, 128, 185));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setPreferredSize(new Dimension(250, 50));
        
        btnApprove.addActionListener(e -> approveBooking()); // Sự kiện duyệt
        
        JButton btnRefresh = new JButton("Làm mới danh sách");
        btnRefresh.addActionListener(e -> loadBookings());

        JPanel bot = new JPanel(); bot.add(btnRefresh); bot.add(btnApprove);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    // ================= LOGIC DATABASE (Xử lý dữ liệu nền) =================
    
    // 1. Tải danh sách đơn hàng
    private void loadBookings() {
        bookingModel.setRowCount(0); // Xóa bảng cũ
        try (Connection conn = DatabaseConnection.getConnection()) {
            // [LỢI ÍCH]: ORDER BY id DESC để đơn mới nhất hiện lên trên cùng -> Dễ quản lý.
            String sql = "SELECT * FROM bookings ORDER BY id DESC"; 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while(rs.next()) {
                // [LỢI ÍCH]: Xử lý Timestamp. Nếu null thì hiện "---", nếu có thì format đẹp.
                // [NẾU XOÁ]: Ngày giờ sẽ hiện ra một chuỗi số khó hiểu hoặc null.
                Timestamp ts = rs.getTimestamp("booking_time");
                String timeStr = (ts != null) ? dateFormat.format(ts) : "---";

                bookingModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("trip_info"),
                    rs.getString("price"),
                    timeStr, 
                    rs.getString("status")
                });
            }
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    // 2. Logic Duyệt đơn hàng
    private void approveBooking() {
        int row = bookingTable.getSelectedRow();
        // [LỢI ÍCH]: Kiểm tra xem Admin đã chọn dòng nào chưa.
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn cần duyệt!"); return; }
        
        String id = bookingTable.getValueAt(row, 0).toString();
        String status = bookingTable.getValueAt(row, 5).toString();
        
        // [LỢI ÍCH]: Chặn duyệt lại đơn đã thanh toán rồi.
        if (status.equals("Đã thanh toán")) {
            JOptionPane.showMessageDialog(this, "Đơn này đã được duyệt trước đó!"); return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // [LỢI ÍCH]: UPDATE trạng thái trong SQL.
            // Khi lệnh này chạy xong, bên phía Khách hàng cũng sẽ thấy trạng thái vé đổi màu.
            String sql = "UPDATE bookings SET status='Đã thanh toán' WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Đã duyệt đơn thành công!");
            loadBookings(); // [LỢI ÍCH]: Tải lại bảng ngay lập tức để thấy thay đổi.
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi duyệt: " + e.getMessage());
        }
    }

    // 3. Tải danh sách User
    private void loadUsers() {
        userModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users");
            while(rs.next()) {
                userModel.addRow(new Object[]{
                    rs.getString("username"), 
                    rs.getString("password"), 
                    rs.getString("role"),
                    rs.getString("phone_number") 
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // 4. Thêm User mới
    private void addUser() {
        String u = txtUser.getText(); String p = new String(txtPass.getPassword());
        if(u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Thiếu thông tin!"); return; }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // [LỢI ÍCH]: INSERT user mới. Mặc định status=1 để tài khoản hoạt động ngay.
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, 1)");
            stmt.setString(1, u); stmt.setString(2, p);
            stmt.setString(3, cbRole.getSelectedItem().toString());
            stmt.executeUpdate(); 
            
            loadUsers(); // Refresh bảng
            txtUser.setText(""); txtPass.setText(""); // Xóa trắng ô nhập
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: User đã tồn tại!"); }
    }
    
    // 5. Xóa User
    private void deleteUser() {
        if(txtUser.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập username để xóa!"); return; }
        
        // [LỢI ÍCH]: Hỏi xác nhận trước khi xóa để tránh lỡ tay.
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa user này?");
        if(confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // [LỢI ÍCH]: Lệnh DELETE xóa vĩnh viễn user khỏi DB.
            // [CẢNH BÁO]: Cẩn thận khi dùng lệnh này.
            conn.createStatement().execute("DELETE FROM users WHERE username='" + txtUser.getText() + "'");
            loadUsers(); 
            txtUser.setText(""); txtPass.setText("");
            JOptionPane.showMessageDialog(this, "Đã xóa!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Hàm Main để chạy thử riêng file Admin
    public static void main(String[] args) { 
        SwingUtilities.invokeLater(() -> new TicketAdminGUI().setVisible(true)); 
    }
}
