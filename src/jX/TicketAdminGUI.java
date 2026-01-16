package jX;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class TicketAdminGUI extends JFrame {

    private final Color COLOR_PRIMARY = new Color(44, 62, 80); // Màu xanh đậm Admin
    private JTabbedPane tabbedPane;
    
    // Tab 1: User
    private JTable userTable;
    private DefaultTableModel userModel;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> cbRole;

    // Tab 2: Booking
    private JTable bookingTable;
    private DefaultTableModel bookingModel;
    
    // Format ngày giờ cho đẹp
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TicketAdminGUI() {
        setTitle("ADMIN DASHBOARD - Quản Lý Hệ Thống");
        setSize(1100, 650); // Tăng chiều rộng một chút để chứa cột thời gian
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(initHeader(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabbedPane.addTab("QUẢN LÝ USER", initUserTab());
        tabbedPane.addTab("DUYỆT ĐƠN VÉ", initBookingTab());

        add(tabbedPane, BorderLayout.CENTER);
        
        loadUsers();
        loadBookings();
    }

    private JPanel initHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(COLOR_PRIMARY); 
        p.setPreferredSize(new Dimension(getWidth(), 60));
        p.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel l = new JLabel("QUẢN TRỊ HỆ THỐNG"); 
        l.setFont(new Font("Segoe UI", Font.BOLD, 20)); 
        l.setForeground(Color.WHITE);
        
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            this.dispose(); 
            try { new TicketLoginGUI().setVisible(true); } catch (Exception ex) {}
        });
        
        p.add(l, BorderLayout.WEST); 
        p.add(btnLogout, BorderLayout.EAST);
        return p;
    }

    // ================= TAB 1: QUẢN LÝ USER (Che mật khẩu) =================
    private JPanel initUserTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(new EmptyBorder(20, 100, 20, 100));
        
        txtUser = new JTextField(); 
        txtPass = new JPasswordField();
        cbRole = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN"});
        
        inputPanel.add(new JLabel("Tên đăng nhập:")); inputPanel.add(txtUser);
        inputPanel.add(new JLabel("Mật khẩu:")); inputPanel.add(txtPass);
        inputPanel.add(new JLabel("Vai trò:")); inputPanel.add(cbRole);
        
        JButton btnAdd = new JButton("Thêm User");
        btnAdd.setBackground(new Color(39, 174, 96)); btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> addUser());
        
        JButton btnDel = new JButton("Xóa User");
        btnDel.setBackground(new Color(192, 57, 43)); btnDel.setForeground(Color.WHITE);
        btnDel.addActionListener(e -> deleteUser());

        JPanel btnP = new JPanel(); btnP.add(btnAdd); btnP.add(btnDel);
        
        JPanel top = new JPanel(new BorderLayout());
        top.add(inputPanel, BorderLayout.CENTER); top.add(btnP, BorderLayout.SOUTH);

        userModel = new DefaultTableModel(new String[]{"Username", "Password", "Role", "SĐT"}, 0);
        userTable = new JTable(userModel);
        
        // Che mật khẩu bằng dấu chấm
        userTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText("••••••");
                return this;
            }
        });
        
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    // ================= TAB 2: DUYỆT ĐƠN VÉ (Có cột Thời gian) =================
    private JPanel initBookingTab() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Cấu hình bảng: Thêm cột "Thời gian"
        String[] columns = {"ID", "Khách hàng", "Chuyến đi", "Giá vé", "Thời gian", "Trạng thái"};
        
        bookingModel = new DefaultTableModel(columns, 0);
        bookingTable = new JTable(bookingModel);
        bookingTable.setRowHeight(30);
        bookingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Nút Duyệt
        JButton btnApprove = new JButton("XÁC NHẬN THANH TOÁN (DUYỆT ĐƠN)");
        btnApprove.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnApprove.setBackground(new Color(41, 128, 185));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setPreferredSize(new Dimension(250, 50));
        
        btnApprove.addActionListener(e -> approveBooking());
        
        JButton btnRefresh = new JButton("Làm mới danh sách");
        btnRefresh.addActionListener(e -> loadBookings());

        JPanel bot = new JPanel(); bot.add(btnRefresh); bot.add(btnApprove);

        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    // ================= LOGIC DATABASE =================
    
    // 1. Tải danh sách đơn hàng (Lấy thêm booking_time)
    private void loadBookings() {
        bookingModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM bookings ORDER BY id DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while(rs.next()) {
                // Lấy thời gian và format
                Timestamp ts = rs.getTimestamp("booking_time");
                String timeStr = (ts != null) ? dateFormat.format(ts) : "---";

                bookingModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("trip_info"),
                    rs.getString("price"),
                    timeStr, // Cột thời gian
                    rs.getString("status")
                });
            }
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    // 2. Duyệt đơn hàng
    private void approveBooking() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn cần duyệt!"); return; }
        
        String id = bookingTable.getValueAt(row, 0).toString();
        // Lưu ý: Cột trạng thái giờ nằm ở vị trí index 5 (do thêm cột thời gian)
        String status = bookingTable.getValueAt(row, 5).toString();
        
        if (status.equals("Đã thanh toán")) {
            JOptionPane.showMessageDialog(this, "Đơn này đã được duyệt trước đó!"); return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE bookings SET status='Đã thanh toán' WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Đã duyệt đơn thành công!");
            loadBookings(); // Refresh bảng
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi duyệt: " + e.getMessage());
        }
    }

    // 3. Quản lý User
    private void loadUsers() {
        userModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users");
            while(rs.next()) {
                userModel.addRow(new Object[]{
                    rs.getString("username"), 
                    rs.getString("password"), 
                    rs.getString("role"),
                    rs.getString("phone_number") // Thêm hiển thị SĐT
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void addUser() {
        String u = txtUser.getText(); String p = new String(txtPass.getPassword());
        if(u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Thiếu thông tin!"); return; }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, 1)");
            stmt.setString(1, u); stmt.setString(2, p);
            stmt.setString(3, cbRole.getSelectedItem().toString());
            stmt.executeUpdate(); loadUsers(); 
            txtUser.setText(""); txtPass.setText("");
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi: User đã tồn tại!"); }
    }
    
    private void deleteUser() {
        if(txtUser.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập username để xóa!"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa user này?");
        if(confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.createStatement().execute("DELETE FROM users WHERE username='" + txtUser.getText() + "'");
            loadUsers(); txtUser.setText(""); txtPass.setText("");
            JOptionPane.showMessageDialog(this, "Đã xóa!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) { 
        SwingUtilities.invokeLater(() -> new TicketAdminGUI().setVisible(true)); 
    }
}
