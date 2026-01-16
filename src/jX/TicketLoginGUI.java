package jX; // [LỢI ÍCH]: Gom nhóm file này vào thư mục 'jX' để dễ quản lý. [NẾU XOÁ]: Code không chạy được nếu cấu trúc thư mục không khớp.

// --- KHU VỰC IMPORT THƯ VIỆN ---
import javax.swing.*; // [LỢI ÍCH]: Nhập toàn bộ component giao diện (Button, Label, Panel...). [NẾU XOÁ]: Báo lỗi không tìm thấy JFrame, JButton...
import javax.swing.border.*; // [LỢI ÍCH]: Để tạo viền đẹp (EmptyBorder, LineBorder). [NẾU XOÁ]: Giao diện bị dính sát lề, xấu.
import java.awt.*; // [LỢI ÍCH]: Nhập các công cụ đồ họa (Màu sắc, Font, Layout). [NẾU XOÁ]: Không set được màu, font, kích thước.
import java.awt.event.*; // [LỢI ÍCH]: Xử lý sự kiện chuột và bàn phím. [NẾU XOÁ]: Bấm nút không có phản ứng gì.
import java.sql.*; // [LỢI ÍCH]: Làm việc với MySQL (Kết nối, Truy vấn). [NẾU XOÁ]: Không thể đăng nhập, đăng ký.
import java.util.regex.Pattern; // [LỢI ÍCH]: Xử lý biểu thức chính quy (Regex) để kiểm tra SĐT. [NẾU XOÁ]: Không kiểm tra được định dạng SĐT.

public class TicketLoginGUI extends JFrame { // JFrame = Cửa sổ Windows

    // --- CẤU HÌNH HẰNG SỐ (CONSTANTS) ---
    // [LỢI ÍCH]: Định nghĩa màu sắc và kích thước ở 1 chỗ. Muốn sửa màu cả app chỉ cần sửa ở đây.
    private final Color COLOR_PRIMARY = new Color(41, 128, 185); // Màu xanh chủ đạo
    private final Color COLOR_BG = new Color(240, 248, 255);     // Màu nền nhạt
    private final Dimension FIELD_SIZE = new Dimension(350, 40); // Kích thước chuẩn cho ô nhập

    // [LỢI ÍCH]: CardLayout giúp xếp chồng các màn hình (Login, Register...) lên nhau như bộ bài.
    // [NẾU XOÁ]: Không thể chuyển đổi qua lại giữa Đăng nhập/Đăng ký trên cùng 1 cửa sổ.
    private CardLayout cardLayout;
    private JPanel mainPanel; // Panel chính chứa bộ bài
    
    // [LỢI ÍCH]: Lưu tạm username khi người dùng đang ở quy trình Quên mật khẩu.
    // [NẾU XOÁ]: Đến bước đổi mật khẩu sẽ không biết đổi cho user nào -> Lỗi logic.
    private String tempUsernameForReset = "";

    // --- CONSTRUCTOR (Hàm khởi tạo chạy đầu tiên) ---
    public TicketLoginGUI() {
        // [LỢI ÍCH]: Cài đặt tiêu đề cửa sổ.
        setTitle("APP ĐẶT XE KHÁCH - Hệ Thống Đăng Nhập");
        
        // [LỢI ÍCH]: Set kích thước cửa sổ.
        setSize(1100, 800);
        
        // [LỢI ÍCH]: Khi bấm dấu X đỏ thì tắt hẳn chương trình (giải phóng RAM).
        // [NẾU XOÁ]: Bấm X cửa sổ biến mất nhưng chương trình vẫn chạy ngầm tốn RAM.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
        // [LỢI ÍCH]: Căn cửa sổ ra chính giữa màn hình máy tính.
        // [NẾU XOÁ]: Cửa sổ sẽ hiện ở góc trên cùng bên trái.
        setLocationRelativeTo(null); 

        // [LỢI ÍCH]: Khởi tạo "bộ bài" layout.
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // [LỢI ÍCH]: Thêm các màn hình con vào bộ bài và đặt tên mã (LOGIN, REGISTER...).
        mainPanel.add(initLoginScreen(), "LOGIN");           // Màn hình Đăng nhập
        mainPanel.add(initRegisterScreen(), "REGISTER");     // Màn hình Đăng ký
        mainPanel.add(initForgotPasswordScreen(), "FORGOT"); // Quên Pass bước 1
        mainPanel.add(initSecurityQuestionScreen(), "SECURITY_CHECK"); // Quên Pass bước 2
        mainPanel.add(initResetPasswordScreen(), "RESET_PASS"); // Quên Pass bước 3

        add(mainPanel); // Đưa panel chính lên cửa sổ
        cardLayout.show(mainPanel, "LOGIN"); // [LỢI ÍCH]: Mặc định lật lá bài LOGIN lên đầu tiên.
    }

    // --- CÁC HÀM VALIDATION (KIỂM TRA DỮ LIỆU) ---
    
    // [LỢI ÍCH]: Kiểm tra SĐT có đúng chuẩn Việt Nam không bằng Regex.
    // ^(0|\+84): Bắt đầu bằng 0 hoặc +84. \\d{9}: Theo sau là 9 số.
    private boolean isValidPhone(String phone) {
        return Pattern.matches("^(0|\\+84)\\d{9}$", phone); 
    }
    
    // [LỢI ÍCH]: Kiểm tra SĐT đã tồn tại trong DB chưa để tránh trùng lặp.
    private boolean isPhoneExist(String phone) {
        // try (...) : Tự động đóng kết nối sau khi dùng xong (tránh tràn bộ nhớ).
        try (Connection conn = DatabaseConnection.getConnection()) {
            // [LỢI ÍCH]: PreparedStatement chống hack SQL Injection.
            PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE phone_number = ?");
            stmt.setString(1, phone);
            // executeQuery().next() trả về true nếu tìm thấy ít nhất 1 dòng -> Đã tồn tại.
            return stmt.executeQuery().next(); 
        } catch (Exception e) { return false; }
    }

    // ================= 1. MÀN HÌNH ĐĂNG NHẬP =================
    private JPanel initLoginScreen() {
        // [LỢI ÍCH]: GridBagLayout giúp căn các phần tử vào giữa màn hình bất kể kích thước cửa sổ.
        JPanel panel = new JPanel(new GridBagLayout()); 
        panel.setBackground(COLOR_BG);
        JPanel card = createCard(); // [LỢI ÍCH]: Tạo khung trắng bo góc (hàm này viết ở dưới để tái sử dụng).

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26)); 
        lblTitle.setForeground(COLOR_PRIMARY); 
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa chữ

        // [LỢI ÍCH]: stylePlaceholder tạo hiệu ứng chữ mờ "Tên đăng nhập" khi ô trống.
        JTextField txtUser = new JTextField("Tên đăng nhập"); stylePlaceholder(txtUser, "Tên đăng nhập");
        JPasswordField txtPass = new JPasswordField("Mật khẩu"); stylePlaceholderPass(txtPass, "Mật khẩu");

        // Label Quên mật khẩu
        JLabel lblForgot = new JLabel("Quên mật khẩu?"); styleHyperlink(lblForgot);
        // [LỢI ÍCH]: Sự kiện Click chuột để chuyển sang màn hình FORGOT.
        lblForgot.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "FORGOT"); } });

        // Panel con để căn phải nút Quên mật khẩu
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); 
        forgotPanel.setBackground(Color.WHITE); forgotPanel.setMaximumSize(new Dimension(350, 25)); forgotPanel.add(lblForgot);

        JButton btnLogin = new JButton("ĐĂNG NHẬP"); styleButton(btnLogin, COLOR_PRIMARY); btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // [QUAN TRỌNG]: Sự kiện bấm nút Đăng nhập
        btnLogin.addActionListener(e -> {
            // Gọi hàm checkLoginDB để kiểm tra trong MySQL
            if (checkLoginDB(txtUser.getText(), new String(txtPass.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                this.dispose(); // [LỢI ÍCH]: Tắt cửa sổ đăng nhập đi cho gọn.
                
                // [LỢI ÍCH]: Kiểm tra Session để phân quyền.
                // Nếu là ADMIN -> Mở trang Admin. Nếu là Khách -> Mở trang Home.
                if (Session.currentRole.equals("ADMIN")) try { new TicketAdminGUI().setVisible(true); } catch (Exception ex) {}
                else new TicketHomePage().setVisible(true);
            } else { 
                JOptionPane.showMessageDialog(this, "Sai thông tin hoặc tài khoản bị khóa!", "Lỗi", JOptionPane.ERROR_MESSAGE); 
            }
        });

        // Nút chuyển sang Đăng ký
        JLabel lblReg = new JLabel("<html>Chưa có tài khoản? <font color='#2980b9'><u>Đăng ký ngay</u></font></html>");
        lblReg.setCursor(new Cursor(Cursor.HAND_CURSOR)); lblReg.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblReg.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "REGISTER"); } });

        // Thêm tất cả vào khung trắng (VerticalStrut tạo khoảng trống dọc)
        card.add(lblTitle); card.add(Box.createVerticalStrut(30)); 
        card.add(txtUser); card.add(Box.createVerticalStrut(15));
        card.add(txtPass); card.add(Box.createVerticalStrut(5)); 
        card.add(forgotPanel); card.add(Box.createVerticalStrut(20));
        card.add(btnLogin); card.add(Box.createVerticalStrut(20)); card.add(lblReg);
        
        panel.add(card); return panel;
    }

    // ================= 2. MÀN HÌNH ĐĂNG KÝ =================
    private JPanel initRegisterScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        
        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblTitle.setForeground(COLOR_PRIMARY); lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Khai báo các ô nhập liệu
        JTextField txtUser = new JTextField("Tên đăng nhập"); stylePlaceholder(txtUser, "Tên đăng nhập");
        JPasswordField txtPass = new JPasswordField("Mật khẩu"); stylePlaceholderPass(txtPass, "Mật khẩu");
        JTextField txtPhone = new JTextField("Số điện thoại (0xxx...)"); stylePlaceholder(txtPhone, "Số điện thoại (0xxx...)");
        
        JLabel lblSec = new JLabel("--- Thiết lập bảo mật ---"); lblSec.setFont(new Font("Segoe UI", Font.ITALIC, 13)); lblSec.setForeground(Color.GRAY); lblSec.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Các câu hỏi bảo mật để khôi phục mật khẩu sau này
        JTextField txtQ1 = new JTextField("Câu hỏi 1: Con vật yêu thích?"); stylePlaceholder(txtQ1, "Câu hỏi 1: Con vật yêu thích?");
        JTextField txtA1 = new JTextField("Câu trả lời 1"); stylePlaceholder(txtA1, "Câu trả lời 1");
        JTextField txtQ2 = new JTextField("Câu hỏi 2: Tên trường cấp 3?"); stylePlaceholder(txtQ2, "Câu hỏi 2: Tên trường cấp 3?");
        JTextField txtA2 = new JTextField("Câu trả lời 2"); stylePlaceholder(txtA2, "Câu trả lời 2");

        JButton btnReg = new JButton("ĐĂNG KÝ & THIẾT LẬP BẢO MẬT"); styleButton(btnReg, new Color(39, 174, 96)); btnReg.setAlignmentX(Component.CENTER_ALIGNMENT);

        // [QUAN TRỌNG]: Sự kiện bấm nút Đăng ký
        btnReg.addActionListener(e -> {
            String u = txtUser.getText(), p = new String(txtPass.getPassword()), ph = txtPhone.getText();
            String q1 = txtQ1.getText(), a1 = txtA1.getText(), q2 = txtQ2.getText(), a2 = txtA2.getText();

            // [LỢI ÍCH]: Validation - Chặn người dùng nhập thiếu hoặc sai dữ liệu.
            if (u.equals("Tên đăng nhập") || p.equals("Mật khẩu") || ph.startsWith("Số điện thoại") || a1.startsWith("Câu trả lời")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!"); return;
            }
            if (p.length() < 6) { JOptionPane.showMessageDialog(this, "Mật khẩu >= 6 ký tự!"); return; }
            if (!isValidPhone(ph)) { JOptionPane.showMessageDialog(this, "SĐT không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }
            if (isPhoneExist(ph)) { JOptionPane.showMessageDialog(this, "SĐT đã tồn tại!", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }

            // Gọi hàm insert vào DB
            if (registerUserDB(u, p, ph, q1, a1, q2, a2)) {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!"); 
                cardLayout.show(mainPanel, "LOGIN"); // Quay về trang đăng nhập
            } else { JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });

        // Nút quay lại
        JLabel lblBack = new JLabel("<html><u><< Quay lại</u></html>"); styleHyperlink(lblBack); lblBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBack.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "LOGIN"); } });

        // Thêm components vào panel...
        card.add(lblTitle); card.add(Box.createVerticalStrut(15));
        card.add(txtUser); card.add(Box.createVerticalStrut(5));
        card.add(txtPass); card.add(Box.createVerticalStrut(5));
        card.add(txtPhone); card.add(Box.createVerticalStrut(15));
        card.add(lblSec); card.add(Box.createVerticalStrut(5));
        card.add(txtQ1); card.add(Box.createVerticalStrut(5)); card.add(txtA1);
        card.add(Box.createVerticalStrut(5));
        card.add(txtQ2); card.add(Box.createVerticalStrut(5)); card.add(txtA2);
        card.add(Box.createVerticalStrut(20));
        card.add(btnReg); card.add(Box.createVerticalStrut(10)); card.add(lblBack);
        panel.add(card); return panel;
    }

    // ================= 3. QUY TRÌNH QUÊN MẬT KHẨU (3 MÀN HÌNH) =================
    
    // Màn hình 1: Nhập Username
    private JPanel initForgotPasswordScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        JLabel lbl = new JLabel("BƯỚC 1: NHẬP TÀI KHOẢN"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 20)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField txtU = new JTextField("Nhập username của bạn"); stylePlaceholder(txtU, "Nhập username của bạn");
        JButton btnNext = new JButton("TIẾP TỤC"); styleButton(btnNext, COLOR_PRIMARY); btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnNext.addActionListener(e -> {
            if (checkUserExist(txtU.getText())) {
                tempUsernameForReset = txtU.getText(); // [LỢI ÍCH]: Lưu lại username để dùng cho bước sau.
                prepareSecurityQuestionUI(tempUsernameForReset); // Lấy câu hỏi từ DB.
                cardLayout.show(mainPanel, "SECURITY_CHECK"); // Chuyển sang bước 2.
            } else { JOptionPane.showMessageDialog(this, "Tài khoản không tồn tại!"); }
        });
        
        JLabel lblBack = new JLabel("<html><u>Hủy bỏ</u></html>"); styleHyperlink(lblBack); lblBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBack.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "LOGIN"); } });
        card.add(lbl); card.add(Box.createVerticalStrut(20)); card.add(txtU); card.add(Box.createVerticalStrut(20)); card.add(btnNext); card.add(Box.createVerticalStrut(10)); card.add(lblBack);
        panel.add(card); return panel;
    }

    private JLabel lblQuestionDisplay = new JLabel();
    private JTextField txtAnswerInput = new JTextField();
    private int currentQuestionIndex = 1;

    // Màn hình 2: Trả lời câu hỏi bảo mật
    private JPanel initSecurityQuestionScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        JLabel lbl = new JLabel("BƯỚC 2: XÁC MINH BẢO MẬT"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 20)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblQuestionDisplay.setFont(new Font("Segoe UI", Font.ITALIC, 16)); lblQuestionDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        stylePlaceholder(txtAnswerInput, "Nhập câu trả lời của bạn");
        JButton btnVerify = new JButton("XÁC MINH"); styleButton(btnVerify, new Color(231, 76, 60)); btnVerify.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnVerify.addActionListener(e -> {
            // [LỢI ÍCH]: So sánh câu trả lời với DB.
            if (verifyAnswer(tempUsernameForReset, currentQuestionIndex, txtAnswerInput.getText())) {
                JOptionPane.showMessageDialog(this, "Xác minh thành công!"); 
                cardLayout.show(mainPanel, "RESET_PASS"); // Chuyển sang bước 3
            } else { JOptionPane.showMessageDialog(this, "Sai câu trả lời!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });
        card.add(lbl); card.add(Box.createVerticalStrut(20)); card.add(lblQuestionDisplay); card.add(Box.createVerticalStrut(10)); card.add(txtAnswerInput); card.add(Box.createVerticalStrut(20)); card.add(btnVerify);
        panel.add(card); return panel;
    }

    // Màn hình 3: Đổi mật khẩu
    private JPanel initResetPasswordScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        JLabel lbl = new JLabel("BƯỚC 3: MẬT KHẨU MỚI"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 20)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField txtNewPass = new JPasswordField(); stylePlaceholderPass(txtNewPass, "Mật khẩu mới");
        JButton btnDone = new JButton("CẬP NHẬT"); styleButton(btnDone, new Color(39, 174, 96)); btnDone.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnDone.addActionListener(e -> {
            String np = new String(txtNewPass.getPassword());
            if (np.length() < 6) { JOptionPane.showMessageDialog(this, "Mật khẩu >= 6 ký tự"); return; }
            // Gọi hàm UPDATE password vào DB
            if (updatePassDB(tempUsernameForReset, np)) { 
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!"); 
                cardLayout.show(mainPanel, "LOGIN"); // Xong hết, về trang chủ
            }
        });
        card.add(lbl); card.add(Box.createVerticalStrut(20)); card.add(txtNewPass); card.add(Box.createVerticalStrut(20)); card.add(btnDone);
        panel.add(card); return panel;
    }

    // ================= DATABASE LOGIC (Xử lý dữ liệu nền) =================
    
    // Lấy câu hỏi ngẫu nhiên từ DB để hiển thị
    private void prepareSecurityQuestionUI(String user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            currentQuestionIndex = (Math.random() < 0.5) ? 1 : 2; // [LỢI ÍCH]: Random để hacker khó đoán.
            ResultSet rs = conn.createStatement().executeQuery("SELECT question_" + currentQuestionIndex + " FROM users WHERE username='" + user + "'");
            if (rs.next()) { lblQuestionDisplay.setText("Câu hỏi: " + rs.getString(1)); txtAnswerInput.setText(""); }
        } catch (Exception e) {}
    }
    
    // Kiểm tra câu trả lời
    private boolean verifyAnswer(String user, int idx, String ans) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT answer_" + idx + " FROM users WHERE username='" + user + "'");
            if (rs.next()) return rs.getString(1).equalsIgnoreCase(ans); // [LỢI ÍCH]: So sánh không phân biệt hoa thường (A = a).
        } catch (Exception e) {} return false;
    }
    
    // Kiểm tra đăng nhập (Username + Pass + Status=1)
    private boolean checkLoginDB(String u, String p) {
        try (Connection c = DatabaseConnection.getConnection()) {
            PreparedStatement s = c.prepareStatement("SELECT * FROM users WHERE username=? AND password=? AND status=1");
            s.setString(1, u); s.setString(2, p); ResultSet rs = s.executeQuery();
            if (rs.next()) { 
                // [LỢI ÍCH]: Lưu Session để các màn hình sau biết ai đang dùng.
                Session.currentUsername = u; 
                Session.currentRole = rs.getString("role"); 
                return true; 
            }
        } catch (Exception e) {} return false;
    }
    
    // Thêm User mới vào bảng
    private boolean registerUserDB(String u, String p, String ph, String q1, String a1, String q2, String a2) {
        try (Connection c = DatabaseConnection.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO users (username,password,role,phone_number,question_1,answer_1,question_2,answer_2,status) VALUES (?,?,'CUSTOMER',?,?,?,?,?,1)");
            s.setString(1,u); s.setString(2,p); s.setString(3,ph); s.setString(4,q1); s.setString(5,a1); s.setString(6,q2); s.setString(7,a2);
            s.executeUpdate(); return true;
        } catch (Exception e) { return false; }
    }
    
    private boolean checkUserExist(String u) { try(Connection c=DatabaseConnection.getConnection()){return c.createStatement().executeQuery("SELECT 1 FROM users WHERE username='"+u+"'").next();}catch(Exception e){return false;} }
    private boolean updatePassDB(String u, String p) { try(Connection c=DatabaseConnection.getConnection()){PreparedStatement s=c.prepareStatement("UPDATE users SET password=? WHERE username=?");s.setString(1,p);s.setString(2,u);return s.executeUpdate()>0;}catch(Exception e){return false;} }

    // --- UI HELPERS (Hàm phụ trợ để trang trí giao diện) ---
    // [LỢI ÍCH]: Tái sử dụng code tạo khung trắng, đỡ phải viết lại nhiều lần.
    private JPanel createCard() { JPanel c=new JPanel(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); c.setBackground(Color.WHITE); c.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220)), new EmptyBorder(30,50,30,50))); return c; }
    
    // [LỢI ÍCH]: Tạo Placeholder (chữ mờ hướng dẫn) cho ô nhập liệu.
    private void stylePlaceholder(JTextField tf, String ph) { tf.setPreferredSize(FIELD_SIZE); tf.setMaximumSize(FIELD_SIZE); tf.setAlignmentX(Component.CENTER_ALIGNMENT); tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(0,10,0,10))); tf.setText(ph); tf.setForeground(Color.GRAY); tf.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e){if(tf.getText().equals(ph)){tf.setText("");tf.setForeground(Color.BLACK);}} public void focusLost(FocusEvent e){if(tf.getText().isEmpty()){tf.setText(ph);tf.setForeground(Color.GRAY);}} }); }
    private void stylePlaceholderPass(JPasswordField pf, String ph) { pf.setPreferredSize(FIELD_SIZE); pf.setMaximumSize(FIELD_SIZE); pf.setAlignmentX(Component.CENTER_ALIGNMENT); pf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(0,10,0,10))); pf.setEchoChar((char)0); pf.setText(ph); pf.setForeground(Color.GRAY); pf.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e){if(new String(pf.getPassword()).equals(ph)){pf.setText("");pf.setEchoChar('•');pf.setForeground(Color.BLACK);}} public void focusLost(FocusEvent e){if(pf.getPassword().length==0){pf.setText(ph);pf.setEchoChar((char)0);pf.setForeground(Color.GRAY);}} }); }
    private void styleButton(JButton b, Color c) { b.setBackground(c); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setFocusPainted(false); b.setMaximumSize(FIELD_SIZE); }
    private void styleHyperlink(JLabel l) { l.setForeground(Color.GRAY); l.setCursor(new Cursor(Cursor.HAND_CURSOR)); }

    // [LỢI ÍCH]: Hàm main để chạy chương trình. Sử dụng invokeLater để đảm bảo an toàn luồng giao diện.
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new TicketLoginGUI().setVisible(true)); }
}
