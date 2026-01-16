package jX;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.io.File;

public class TicketHomePage extends JFrame {

    // --- CẤU HÌNH GIAO DIỆN ---
    private final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private final Color COLOR_BG = new Color(242, 247, 250);
    private final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private CardLayout cardLayout;
    private JPanel centerPanel;
    private JComboBox<String> cbFrom, cbTo;
    private JTable resultTable, myTicketTable, historyTable;
    private DefaultTableModel tableModel, myTicketModel, historyModel;
    
    // Components cho tab Tài khoản
    private JTextField txtProfileUser, txtProfilePhone;

    // Format ngày giờ
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private List<Trip> allTrips = new ArrayList<>();
    
    // Danh sách 63 tỉnh thành
    private final String[] STATIONS = { 
        "An Giang", "Bà Rịa - Vũng Tàu", "Bạc Liêu", "Bắc Kạn", "Bắc Giang", "Bắc Ninh", "Bến Tre", "Bình Dương", "Bình Định", "Bình Phước", "Bình Thuận", 
        "Cà Mau", "Cao Bằng", "Cần Thơ", "Đà Nẵng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội", 
        "Hà Tĩnh", "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lạng Sơn", "Lào Cai", 
        "Lâm Đồng", "Long An", "Nam Định", "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", 
        "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", "TP. Hồ Chí Minh", "Trà Vinh", 
        "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái" 
    };
    
    static class Trip {
        String name, time, route, type, seats, price, status;
        public Trip(String n, String t, String r, String ty, String s, String p, String st) {
            name=n; time=t; route=r; type=ty; seats=s; price=p; status=st;
        }
    }

    public TicketHomePage() {
        setTitle("APP ĐẶT XE KHÁCH - Trang Chủ");
        setSize(1280, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        loadTripsFromDB(); 

        setLayout(new BorderLayout());
        add(initNavbar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        
        centerPanel.add(initHomeView(), "HOME");
        centerPanel.add(initMyTicketView(), "MY_TICKET");
        centerPanel.add(initHistoryView(), "HISTORY");
        centerPanel.add(initProfileView(), "PROFILE");

        add(centerPanel, BorderLayout.CENTER);
        cardLayout.show(centerPanel, "HOME");
    }

    // =========================================================================
    // LOGIC DATABASE & HELPER
    // =========================================================================
    private void loadTripsFromDB() {
        allTrips.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;
            String sql = "SELECT * FROM trips"; 
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                allTrips.add(new Trip(rs.getString("nhaxe"), rs.getString("gio_chay"), rs.getString("lo_trinh"), rs.getString("loai_xe"), rs.getString("ghe_trong"), rs.getString("gia_ve"), rs.getString("trang_thai")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // Autocomplete cho ComboBox
    private void setupAutoComplete(final JComboBox<String> comboBox, final String[] items) {
        comboBox.setEditable(true);
        final JTextField textInput = (JTextField) comboBox.getEditor().getEditorComponent();
        
        textInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) return;
                SwingUtilities.invokeLater(() -> {
                    String text = textInput.getText();
                    List<String> filterList = new ArrayList<>();
                    for (String item : items) {
                        if (item.toLowerCase().contains(text.toLowerCase())) filterList.add(item);
                    }
                    if (!filterList.isEmpty()) {
                        comboBox.setModel(new DefaultComboBoxModel<>(filterList.toArray(new String[0])));
                        comboBox.setSelectedItem(text);
                        comboBox.showPopup();
                        if (comboBox.getItemCount() > 0) textInput.setCaretPosition(text.length()); 
                    } else {
                        comboBox.setModel(new DefaultComboBoxModel<>(new String[]{"Không tìm thấy tỉnh thành này"}));
                        comboBox.setSelectedItem(text);
                        comboBox.showPopup();
                    }
                });
            }
        });
    }

    private void resetHomeView() {
        if(cbFrom != null && cbTo != null) {
            cbFrom.setModel(new DefaultComboBoxModel<>(STATIONS));
            cbTo.setModel(new DefaultComboBoxModel<>(STATIONS));
            cbFrom.setSelectedItem("TP. Hồ Chí Minh");
            cbTo.setSelectedItem("Lâm Đồng");
        }
        loadTripsFromDB();
        loadTableData(allTrips);
        if(resultTable != null) resultTable.clearSelection();
    }

    private void loadUserProfile() {
        txtProfileUser.setText(Session.currentUsername);
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT phone_number FROM users WHERE username=?");
            stmt.setString(1, Session.currentUsername);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) txtProfilePhone.setText(rs.getString("phone_number"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updatePhoneNumber() {
        String newPhone = txtProfilePhone.getText().trim();
        if (newPhone.isEmpty()) { JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
        String regex = "^(0|\\+84)\\d{9}$";
        if (!Pattern.matches(regex, newPhone)) { JOptionPane.showMessageDialog(this, "Số điện thoại sai định dạng!\n(Phải bắt đầu 0 hoặc +84 và đủ 10 số)", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT username FROM users WHERE phone_number=? AND username != ?");
            check.setString(1, newPhone); check.setString(2, Session.currentUsername);
            if (check.executeQuery().next()) { JOptionPane.showMessageDialog(this, "Số điện thoại đã được tài khoản khác sử dụng!", "Lỗi trùng", JOptionPane.ERROR_MESSAGE); return; }
            PreparedStatement update = conn.prepareStatement("UPDATE users SET phone_number=?, updated_at=NOW() WHERE username=?");
            update.setString(1, newPhone); update.setString(2, Session.currentUsername);
            update.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cập nhật số điện thoại thành công!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // UI COMPONENTS & TABS
    // =========================================================================
    
    // NAVBAR
    private JPanel initNavbar() {
        JPanel nav = new JPanel(new BorderLayout()); nav.setBackground(COLOR_PRIMARY); nav.setPreferredSize(new Dimension(getWidth(), 60)); nav.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel lblBrand = new JLabel("APP ĐẶT XE KHÁCH"); lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblBrand.setForeground(Color.WHITE);
        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 15)); links.setOpaque(false);
        links.add(createNavLink("Trang chủ", "HOME")); links.add(createNavLink("Vé của tôi", "MY_TICKET")); links.add(createNavLink("Lịch sử", "HISTORY")); links.add(createNavLink("Thông tin tài khoản", "PROFILE"));
        JButton btnLogout = new JButton("Đăng xuất"); styleButton(btnLogout, new Color(231, 76, 60)); btnLogout.setPreferredSize(new Dimension(100, 30));
        btnLogout.addActionListener(e -> { int choice = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION); if (choice == JOptionPane.YES_OPTION) { this.dispose(); try { new TicketLoginGUI().setVisible(true); } catch (Exception ex) {} } });
        links.add(btnLogout); nav.add(lblBrand, BorderLayout.WEST); nav.add(links, BorderLayout.EAST); return nav;
    }
    private JLabel createNavLink(String t, String c) { JLabel l = new JLabel(t); l.setFont(FONT_MAIN); l.setForeground(Color.WHITE); l.setCursor(new Cursor(Cursor.HAND_CURSOR)); l.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(centerPanel, c); if(c.equals("HOME")) resetHomeView(); if(c.equals("MY_TICKET")) refreshMyTicketTable(); if(c.equals("HISTORY")) refreshHistoryTable(); if(c.equals("PROFILE")) loadUserProfile(); } public void mouseEntered(MouseEvent e) { l.setForeground(Color.YELLOW); l.setFont(FONT_BOLD); } public void mouseExited(MouseEvent e) { l.setForeground(Color.WHITE); l.setFont(FONT_MAIN); } }); return l; }

    // --- TAB 1: HOME (GIAO DIỆN TÌM KIẾM ĐÃ CĂN CHỈNH) ---
    private JPanel initHomeView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(initFilterPanel(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(COLOR_BG);

        // Thanh tìm kiếm căn giữa, khoảng cách rộng rãi
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 25)); 
        searchBox.setBackground(Color.WHITE); 
        searchBox.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        cbFrom = new JComboBox<>(STATIONS); cbTo = new JComboBox<>(STATIONS);
        setupAutoComplete(cbFrom, STATIONS); setupAutoComplete(cbTo, STATIONS);
        cbFrom.setSelectedItem("TP. Hồ Chí Minh"); cbTo.setSelectedItem("Lâm Đồng");
        styleComboBox(cbFrom); styleComboBox(cbTo);
        
        JButton btnSearch = new JButton("TÌM CHUYẾN"); styleButton(btnSearch, new Color(52, 152, 219));
        btnSearch.setPreferredSize(new Dimension(150, 35)); // Chiều cao bằng ô nhập
        
        btnSearch.addActionListener(e -> {
            String from = (String)cbFrom.getSelectedItem();
            String to = (String)cbTo.getSelectedItem();
            if (from == null || from.contains("Không tìm thấy") || to == null || to.contains("Không tìm thấy")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn tỉnh thành hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE); return;
            }
            filterTrips(from, to);
        });
        
        // Panel riêng cho nút bấm để căn thẳng hàng với Label "Điểm đi"
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(Color.WHITE);
        JLabel lblDummy = new JLabel(" "); // Label rỗng để đẩy nút xuống
        lblDummy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPanel.add(lblDummy, BorderLayout.NORTH);
        btnPanel.add(btnSearch, BorderLayout.CENTER);
        
        searchBox.add(createInputGroup("Điểm đi:", cbFrom)); 
        searchBox.add(createInputGroup("Điểm đến:", cbTo)); 
        searchBox.add(btnPanel); // Thêm Panel nút đã căn chỉnh

        String[] cols = {"Nhà xe", "Giờ chạy", "Lộ trình", "Loại xe", "Ghế trống", "Giá vé (VND)", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        resultTable = new JTable(tableModel); styleTable(resultTable);
        loadTableData(allTrips); 

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT)); footer.setBackground(COLOR_BG); footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        JButton btnBook = new JButton("ĐẶT VÉ NGAY"); styleButton(btnBook, new Color(39, 174, 96)); btnBook.setPreferredSize(new Dimension(150, 45));
        btnBook.addActionListener(e -> processBooking());
        footer.add(btnBook);
        
        main.add(searchBox, BorderLayout.NORTH); main.add(new JScrollPane(resultTable), BorderLayout.CENTER); main.add(footer, BorderLayout.SOUTH);
        panel.add(main, BorderLayout.CENTER); return panel;
    }

    // TAB 2: MY TICKETS
    private JPanel initMyTicketView() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        JLabel title = new JLabel("VÉ CỦA TÔI (Đã đặt)"); title.setFont(new Font("Segoe UI", Font.BOLD, 20)); title.setForeground(COLOR_PRIMARY);
        String[] cols = {"Mã Vé", "Chuyến đi", "Giá vé", "Thời gian đặt", "Trạng thái"};
        myTicketModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        myTicketTable = new JTable(myTicketModel); styleTable(myTicketTable);
        panel.add(title, BorderLayout.NORTH); panel.add(new JScrollPane(myTicketTable), BorderLayout.CENTER); return panel;
    }

    // TAB 3: HISTORY
    private JPanel initHistoryView() {
        JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(20, 40, 20, 40));
        JLabel title = new JLabel("LỊCH SỬ GIAO DỊCH"); title.setFont(new Font("Segoe UI", Font.BOLD, 20)); title.setForeground(COLOR_PRIMARY);
        String[] cols = {"Mã GD", "Chuyến đi", "Số tiền", "Thời gian", "Trạng thái"};
        historyModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        historyTable = new JTable(historyModel); styleTable(historyTable);
        panel.add(title, BorderLayout.NORTH); panel.add(new JScrollPane(historyTable), BorderLayout.CENTER); return panel;
    }

    // TAB 4: PROFILE
    private JPanel initProfileView() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Color.WHITE);
        JPanel c = new JPanel(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); c.setBackground(Color.WHITE); 
        c.setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(30,50,30,50)));
        JLabel t = new JLabel("CẬP NHẬT THÔNG TIN"); t.setFont(new Font("Segoe UI", Font.BOLD, 22)); t.setForeground(COLOR_PRIMARY); t.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtProfileUser = new JTextField(); txtProfileUser.setEditable(false); styleField(txtProfileUser);
        txtProfilePhone = new JTextField(); styleField(txtProfilePhone);
        JButton b = new JButton("XÁC NHẬN CẬP NHẬT"); styleButton(b, new Color(39, 174, 96)); b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(e -> updatePhoneNumber());
        c.add(t); c.add(Box.createVerticalStrut(20)); c.add(new JLabel("Tên đăng nhập:")); c.add(txtProfileUser); c.add(Box.createVerticalStrut(15));
        c.add(new JLabel("Số điện thoại (0xxx...):")); c.add(txtProfilePhone); c.add(Box.createVerticalStrut(20)); c.add(b);
        p.add(c); return p;
    }

    // =========================================================================
    // LOGIC XỬ LÝ
    // =========================================================================
    private void loadTableData(List<Trip> trips) {
        tableModel.setRowCount(0);
        for (Trip t : trips) tableModel.addRow(new Object[]{t.name, t.time, t.route, t.type, t.seats, t.price, t.status});
    }

    private void filterTrips(String from, String to) {
        List<Trip> filtered = new ArrayList<>();
        String fromKey = from.toLowerCase().replace("tp. hồ chí minh", "sài gòn").replace("thừa thiên huế", "huế").replace("bà rịa - vũng tàu", "vũng tàu");
        String toKey = to.toLowerCase().replace("tp. hồ chí minh", "sài gòn").replace("thừa thiên huế", "huế").replace("bà rịa - vũng tàu", "vũng tàu");
        for (Trip t : allTrips) {
            String routeLower = t.route.toLowerCase();
            if (routeLower.contains(fromKey) && routeLower.contains(toKey)) filtered.add(t);
        }
        if (filtered.isEmpty()) { JOptionPane.showMessageDialog(this, "Chưa tìm thấy chuyến từ " + from + " đến " + to); tableModel.setRowCount(0); } else { loadTableData(filtered); }
    }

    private void processBooking() {
        int row = resultTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn chuyến xe muốn đặt!", "Chưa chọn vé", JOptionPane.WARNING_MESSAGE); return; }
        String status = tableModel.getValueAt(row, 6).toString();
        if (status.equalsIgnoreCase("Hết vé") || status.equalsIgnoreCase("Hết chỗ")) {
            JOptionPane.showMessageDialog(this, "Đã hết vé không đặt vé được nữa!", "Thông báo", JOptionPane.ERROR_MESSAGE); return;
        }
        String bus = tableModel.getValueAt(row, 0).toString();
        String route = tableModel.getValueAt(row, 2).toString();
        String time = tableModel.getValueAt(row, 1).toString();
        String price = tableModel.getValueAt(row, 5).toString();
        showPaymentDialog(bus, route, time, price);
    }

    // =========================================================================
    // PAYMENT DIALOG
    // =========================================================================
    private void showPaymentDialog(String bus, String route, String time, String price) {
        JDialog dialog = new JDialog(this, "Cổng Thanh Toán QR", true); dialog.setSize(500, 680); dialog.setLayout(new BorderLayout()); dialog.setLocationRelativeTo(this); dialog.getContentPane().setBackground(Color.WHITE);
        JPanel mainContent = new JPanel(); mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS)); mainContent.setBackground(Color.WHITE); mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));
        JLabel lblTitle = new JLabel("XÁC NHẬN ĐẶT VÉ"); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblTitle.setForeground(COLOR_PRIMARY); lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel infoPanel = new JPanel(new GridBagLayout()); infoPanel.setBackground(new Color(245, 245, 245)); infoPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true), new EmptyBorder(15, 20, 15, 20))); infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT); infoPanel.setMaximumSize(new Dimension(400, 200));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(5, 5, 5, 10); gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0;
        addInfoRow(infoPanel, gbc, 0, "Nhà xe:", bus, false); addInfoRow(infoPanel, gbc, 1, "Lộ trình:", route, false); addInfoRow(infoPanel, gbc, 2, "Giờ khởi hành:", time, false); addInfoRow(infoPanel, gbc, 3, "TỔNG TIỀN:", price + " VNĐ", true);
        JPanel qrPanel = new JPanel(); qrPanel.setLayout(new BoxLayout(qrPanel, BoxLayout.Y_AXIS)); qrPanel.setBackground(Color.WHITE); qrPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel lblInstruct = new JLabel("Quét mã QR bên dưới để thanh toán"); lblInstruct.setFont(new Font("Segoe UI", Font.ITALIC, 14)); lblInstruct.setForeground(Color.DARK_GRAY); lblInstruct.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel qrLabel = new JLabel(); qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT); qrLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        String qrPath = "D:\\payment_qr.jpg"; File qrFile = new File(qrPath);
        if (qrFile.exists()) { ImageIcon qrIcon = new ImageIcon(qrPath); qrLabel.setIcon(new ImageIcon(qrIcon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH))); } else { qrLabel.setText("<html><center><font color='red'>Không tìm thấy ảnh QR!<br>" + qrPath + "</font></center></html>"); qrLabel.setPreferredSize(new Dimension(220, 220)); qrLabel.setHorizontalAlignment(SwingConstants.CENTER); }
        qrPanel.add(lblInstruct); qrPanel.add(Box.createVerticalStrut(10)); qrPanel.add(qrLabel);
        mainContent.add(lblTitle); mainContent.add(Box.createVerticalStrut(20)); mainContent.add(infoPanel); mainContent.add(Box.createVerticalStrut(25)); mainContent.add(qrPanel); mainContent.add(Box.createVerticalGlue());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); buttonPanel.setBackground(Color.WHITE); buttonPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        JButton btnConfirm = new JButton("TÔI ĐÃ CHUYỂN KHOẢN"); styleButton(btnConfirm, new Color(39, 174, 96)); btnConfirm.setPreferredSize(new Dimension(250, 45)); btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnConfirm.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO bookings (username, trip_info, price, status) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, Session.currentUsername); stmt.setString(2, route + " (" + time + ")"); stmt.setString(3, price); stmt.setString(4, "Chờ xác nhận");
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Đặt vé thành công!\nVui lòng chờ Admin xác nhận."); dialog.dispose(); cardLayout.show(centerPanel, "MY_TICKET"); refreshMyTicketTable();
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage()); }
        });
        buttonPanel.add(btnConfirm); dialog.add(mainContent, BorderLayout.CENTER); dialog.add(buttonPanel, BorderLayout.SOUTH); dialog.setVisible(true);
    }
    private void addInfoRow(JPanel p, GridBagConstraints gbc, int row, String labelStr, String valueStr, boolean isBold) { gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.EAST; JLabel lbl = new JLabel(labelStr); lbl.setFont(FONT_MAIN); if (isBold) lbl.setFont(FONT_BOLD); p.add(lbl, gbc); gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1.0; JLabel val = new JLabel(valueStr); val.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 15)); if (isBold) val.setForeground(new Color(231, 76, 60)); p.add(val, gbc); }

    private void refreshMyTicketTable() {
        myTicketModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM bookings WHERE username = ? ORDER BY id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql); stmt.setString(1, Session.currentUsername);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Timestamp ts = rs.getTimestamp("booking_time"); String timeStr = (ts != null) ? dateFormat.format(ts) : "Vừa xong";
                myTicketModel.addRow(new Object[]{rs.getInt("id"), rs.getString("trip_info"), rs.getString("price"), timeStr, rs.getString("status")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void refreshHistoryTable() {
        historyModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM bookings WHERE username = ? ORDER BY id DESC";
            PreparedStatement stmt = conn.prepareStatement(sql); stmt.setString(1, Session.currentUsername);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                Timestamp ts = rs.getTimestamp("booking_time"); String timeStr = (ts != null) ? dateFormat.format(ts) : "Vừa xong";
                historyModel.addRow(new Object[]{"TRX" + rs.getInt("id"), rs.getString("trip_info"), rs.getString("price"), timeStr, rs.getString("status")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // =========================================================================
    // UI HELPERS (BỘ LỌC ĐẸP & CĂN TRÁI)
    // =========================================================================
    private JPanel initFilterPanel() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE); p.setPreferredSize(new Dimension(240, getHeight()));
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1, new Color(230, 230, 230)), new EmptyBorder(30, 25, 30, 10)));
        JLabel lbl = new JLabel("BỘ LỌC TÌM KIẾM"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 18)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl); p.add(Box.createVerticalStrut(25));
        addFilterGroup(p, "PHƯƠNG TIỆN", new String[]{"Xe khách", "Limousine", "Tàu hỏa"});
        JSeparator sep = new JSeparator(); sep.setMaximumSize(new Dimension(180, 1)); sep.setForeground(new Color(240, 240, 240)); sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sep); p.add(Box.createVerticalStrut(15));
        addFilterGroup(p, "GIỜ KHỞI HÀNH", new String[]{"Sáng (06:00 - 12:00)", "Chiều (12:00 - 18:00)", "Tối (18:00 - 06:00)"});
        p.add(Box.createVerticalGlue()); return p;
    }

    private void addFilterGroup(JPanel p, String title, String[] opts) {
        JLabel l = new JLabel(title); l.setFont(new Font("Segoe UI", Font.BOLD, 13)); l.setForeground(Color.GRAY); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l); p.add(Box.createVerticalStrut(10));
        for (String s : opts) {
            JCheckBox c = new JCheckBox(s); c.setFont(new Font("Segoe UI", Font.PLAIN, 14)); c.setBackground(Color.WHITE); c.setFocusPainted(false); c.setSelected(true); c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setIconTextGap(10); c.setCursor(new Cursor(Cursor.HAND_CURSOR));
            p.add(c); p.add(Box.createVerticalStrut(6));
        } p.add(Box.createVerticalStrut(20));
    }

    private JPanel createInputGroup(String l, JComponent c) { 
        JPanel p=new JPanel(new BorderLayout()); p.add(new JLabel(l),BorderLayout.NORTH); p.add(c,BorderLayout.CENTER); p.setBackground(Color.WHITE); 
        p.setPreferredSize(new Dimension(200, 60)); // Fixed Size để đều nhau
        return p; 
    }
    private void styleButton(JButton b, Color c) { b.setBackground(c); b.setForeground(Color.WHITE); b.setFont(FONT_BOLD); b.setFocusPainted(false); }
    private void styleComboBox(JComboBox c) { c.setBackground(Color.WHITE); c.setPreferredSize(new Dimension(150,35)); }
    private void styleTable(JTable t) { t.setRowHeight(35); t.setFont(FONT_MAIN); t.getTableHeader().setBackground(COLOR_PRIMARY); t.getTableHeader().setForeground(Color.WHITE); DefaultTableCellRenderer c=new DefaultTableCellRenderer(); c.setHorizontalAlignment(JLabel.CENTER); for(int i=0; i<t.getColumnCount();i++) t.getColumnModel().getColumn(i).setCellRenderer(c);}
    private void styleField(JTextField tf) { tf.setPreferredSize(new Dimension(300, 35)); tf.setMaximumSize(new Dimension(300, 35)); tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(0,10,0,10))); }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new TicketHomePage().setVisible(true)); }
}