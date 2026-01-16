package jX;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Pattern;

public class TicketLoginGUI extends JFrame {

    private final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private final Color COLOR_BG = new Color(240, 248, 255);
    private final Dimension FIELD_SIZE = new Dimension(350, 40);

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String tempUsernameForReset = "";

    public TicketLoginGUI() {
        setTitle("APP ĐẶT XE KHÁCH - Hệ Thống Đăng Nhập");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(initLoginScreen(), "LOGIN");
        mainPanel.add(initRegisterScreen(), "REGISTER");
        mainPanel.add(initForgotPasswordScreen(), "FORGOT");
        mainPanel.add(initSecurityQuestionScreen(), "SECURITY_CHECK");
        mainPanel.add(initResetPasswordScreen(), "RESET_PASS");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    // ================= HELPER VALIDATION =================
    private boolean isValidPhone(String phone) {
        return Pattern.matches("^(0|\\+84)\\d{9}$", phone);
    }
    private boolean isPhoneExist(String phone) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE phone_number = ?");
            stmt.setString(1, phone);
            return stmt.executeQuery().next();
        } catch (Exception e) { return false; }
    }

    // ================= 1. LOGIN =================
    private JPanel initLoginScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP"); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26)); lblTitle.setForeground(COLOR_PRIMARY); lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField txtUser = new JTextField("Tên đăng nhập"); stylePlaceholder(txtUser, "Tên đăng nhập");
        JPasswordField txtPass = new JPasswordField("Mật khẩu"); stylePlaceholderPass(txtPass, "Mật khẩu");

        JLabel lblForgot = new JLabel("Quên mật khẩu?"); styleHyperlink(lblForgot);
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); forgotPanel.setBackground(Color.WHITE); forgotPanel.setMaximumSize(new Dimension(350, 25)); forgotPanel.add(lblForgot);
        lblForgot.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "FORGOT"); } });

        JButton btnLogin = new JButton("ĐĂNG NHẬP"); styleButton(btnLogin, COLOR_PRIMARY); btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> {
            if (checkLoginDB(txtUser.getText(), new String(txtPass.getPassword()))) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                this.dispose();
                if (Session.currentRole.equals("ADMIN")) try { new TicketAdminGUI().setVisible(true); } catch (Exception ex) {}
                else new TicketHomePage().setVisible(true);
            } else { JOptionPane.showMessageDialog(this, "Sai thông tin hoặc tài khoản bị khóa!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });

        JLabel lblReg = new JLabel("<html>Chưa có tài khoản? <font color='#2980b9'><u>Đăng ký ngay</u></font></html>");
        lblReg.setCursor(new Cursor(Cursor.HAND_CURSOR)); lblReg.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblReg.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "REGISTER"); } });

        card.add(lblTitle); card.add(Box.createVerticalStrut(30)); card.add(txtUser); card.add(Box.createVerticalStrut(15));
        card.add(txtPass); card.add(Box.createVerticalStrut(5)); card.add(forgotPanel); card.add(Box.createVerticalStrut(20));
        card.add(btnLogin); card.add(Box.createVerticalStrut(20)); card.add(lblReg);
        panel.add(card); return panel;
    }

    // ================= 2. REGISTER (ĐÃ CĂN GIỮA LABEL BẢO MẬT) =================
    private JPanel initRegisterScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        
        JLabel lblTitle = new JLabel("ĐĂNG KÝ TÀI KHOẢN"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
        lblTitle.setForeground(COLOR_PRIMARY); 
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtUser = new JTextField("Tên đăng nhập"); stylePlaceholder(txtUser, "Tên đăng nhập");
        JPasswordField txtPass = new JPasswordField("Mật khẩu"); stylePlaceholderPass(txtPass, "Mật khẩu");
        JTextField txtPhone = new JTextField("Số điện thoại (0xxx...)"); stylePlaceholder(txtPhone, "Số điện thoại (0xxx...)");
        
        // --- CĂN GIỮA DÒNG NÀY ---
        JLabel lblSec = new JLabel("--- Thiết lập bảo mật ---");
        lblSec.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSec.setForeground(Color.GRAY);
        lblSec.setAlignmentX(Component.CENTER_ALIGNMENT); // <--- Lệnh căn giữa
        
        JTextField txtQ1 = new JTextField("Câu hỏi 1: Con vật yêu thích?"); stylePlaceholder(txtQ1, "Câu hỏi 1: Con vật yêu thích?");
        JTextField txtA1 = new JTextField("Câu trả lời 1"); stylePlaceholder(txtA1, "Câu trả lời 1");
        JTextField txtQ2 = new JTextField("Câu hỏi 2: Tên trường cấp 3?"); stylePlaceholder(txtQ2, "Câu hỏi 2: Tên trường cấp 3?");
        JTextField txtA2 = new JTextField("Câu trả lời 2"); stylePlaceholder(txtA2, "Câu trả lời 2");

        JButton btnReg = new JButton("ĐĂNG KÝ & THIẾT LẬP BẢO MẬT"); styleButton(btnReg, new Color(39, 174, 96));
        btnReg.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnReg.addActionListener(e -> {
            String u = txtUser.getText(), p = new String(txtPass.getPassword()), ph = txtPhone.getText();
            String q1 = txtQ1.getText(), a1 = txtA1.getText(), q2 = txtQ2.getText(), a2 = txtA2.getText();

            if (u.equals("Tên đăng nhập") || p.equals("Mật khẩu") || ph.startsWith("Số điện thoại") || a1.startsWith("Câu trả lời")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!"); return;
            }
            if (p.length() < 6) { JOptionPane.showMessageDialog(this, "Mật khẩu >= 6 ký tự!"); return; }
            if (!isValidPhone(ph)) { JOptionPane.showMessageDialog(this, "SĐT không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }
            if (isPhoneExist(ph)) { JOptionPane.showMessageDialog(this, "SĐT đã tồn tại!", "Lỗi", JOptionPane.WARNING_MESSAGE); return; }

            if (registerUserDB(u, p, ph, q1, a1, q2, a2)) {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!"); cardLayout.show(mainPanel, "LOGIN");
            } else { JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });

        JLabel lblBack = new JLabel("<html><u><< Quay lại</u></html>"); styleHyperlink(lblBack); lblBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBack.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { cardLayout.show(mainPanel, "LOGIN"); } });

        card.add(lblTitle); card.add(Box.createVerticalStrut(15));
        card.add(txtUser); card.add(Box.createVerticalStrut(5));
        card.add(txtPass); card.add(Box.createVerticalStrut(5));
        card.add(txtPhone); card.add(Box.createVerticalStrut(15));
        
        card.add(lblSec); // Thêm Label đã căn giữa vào
        
        card.add(Box.createVerticalStrut(5));
        card.add(txtQ1); card.add(Box.createVerticalStrut(5)); card.add(txtA1);
        card.add(Box.createVerticalStrut(5));
        card.add(txtQ2); card.add(Box.createVerticalStrut(5)); card.add(txtA2);
        card.add(Box.createVerticalStrut(20));
        card.add(btnReg); card.add(Box.createVerticalStrut(10)); card.add(lblBack);
        panel.add(card); return panel;
    }

    // ================= 3. FORGOT PASSWORD =================
    private JPanel initForgotPasswordScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        JLabel lbl = new JLabel("BƯỚC 1: NHẬP TÀI KHOẢN"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 20)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField txtU = new JTextField("Nhập username của bạn"); stylePlaceholder(txtU, "Nhập username của bạn");
        JButton btnNext = new JButton("TIẾP TỤC"); styleButton(btnNext, COLOR_PRIMARY); btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnNext.addActionListener(e -> {
            if (checkUserExist(txtU.getText())) {
                tempUsernameForReset = txtU.getText();
                prepareSecurityQuestionUI(tempUsernameForReset);
                cardLayout.show(mainPanel, "SECURITY_CHECK");
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

    private JPanel initSecurityQuestionScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        JLabel lbl = new JLabel("BƯỚC 2: XÁC MINH BẢO MẬT"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 20)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblQuestionDisplay.setFont(new Font("Segoe UI", Font.ITALIC, 16)); lblQuestionDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        stylePlaceholder(txtAnswerInput, "Nhập câu trả lời của bạn");
        JButton btnVerify = new JButton("XÁC MINH"); styleButton(btnVerify, new Color(231, 76, 60)); btnVerify.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnVerify.addActionListener(e -> {
            if (verifyAnswer(tempUsernameForReset, currentQuestionIndex, txtAnswerInput.getText())) {
                JOptionPane.showMessageDialog(this, "Xác minh thành công!"); cardLayout.show(mainPanel, "RESET_PASS");
            } else { JOptionPane.showMessageDialog(this, "Sai câu trả lời!", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        });
        card.add(lbl); card.add(Box.createVerticalStrut(20)); card.add(lblQuestionDisplay); card.add(Box.createVerticalStrut(10)); card.add(txtAnswerInput); card.add(Box.createVerticalStrut(20)); card.add(btnVerify);
        panel.add(card); return panel;
    }

    private JPanel initResetPasswordScreen() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(COLOR_BG);
        JPanel card = createCard();
        JLabel lbl = new JLabel("BƯỚC 3: MẬT KHẨU MỚI"); lbl.setFont(new Font("Segoe UI", Font.BOLD, 20)); lbl.setForeground(COLOR_PRIMARY); lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField txtNewPass = new JPasswordField(); stylePlaceholderPass(txtNewPass, "Mật khẩu mới");
        JButton btnDone = new JButton("CẬP NHẬT"); styleButton(btnDone, new Color(39, 174, 96)); btnDone.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnDone.addActionListener(e -> {
            String np = new String(txtNewPass.getPassword());
            if (np.length() < 6) { JOptionPane.showMessageDialog(this, "Mật khẩu >= 6 ký tự"); return; }
            if (updatePassDB(tempUsernameForReset, np)) { JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!"); cardLayout.show(mainPanel, "LOGIN"); }
        });
        card.add(lbl); card.add(Box.createVerticalStrut(20)); card.add(txtNewPass); card.add(Box.createVerticalStrut(20)); card.add(btnDone);
        panel.add(card); return panel;
    }

    // ================= DB LOGIC =================
    private void prepareSecurityQuestionUI(String user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            currentQuestionIndex = (Math.random() < 0.5) ? 1 : 2;
            ResultSet rs = conn.createStatement().executeQuery("SELECT question_" + currentQuestionIndex + " FROM users WHERE username='" + user + "'");
            if (rs.next()) { lblQuestionDisplay.setText("Câu hỏi: " + rs.getString(1)); txtAnswerInput.setText(""); }
        } catch (Exception e) {}
    }
    private boolean verifyAnswer(String user, int idx, String ans) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT answer_" + idx + " FROM users WHERE username='" + user + "'");
            if (rs.next()) return rs.getString(1).equalsIgnoreCase(ans);
        } catch (Exception e) {} return false;
    }
    private boolean checkLoginDB(String u, String p) {
        try (Connection c = DatabaseConnection.getConnection()) {
            PreparedStatement s = c.prepareStatement("SELECT * FROM users WHERE username=? AND password=? AND status=1");
            s.setString(1, u); s.setString(2, p); ResultSet rs = s.executeQuery();
            if (rs.next()) { Session.currentUsername = u; Session.currentRole = rs.getString("role"); return true; }
        } catch (Exception e) {} return false;
    }
    private boolean registerUserDB(String u, String p, String ph, String q1, String a1, String q2, String a2) {
        try (Connection c = DatabaseConnection.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO users (username,password,role,phone_number,question_1,answer_1,question_2,answer_2,status) VALUES (?,?,'CUSTOMER',?,?,?,?,?,1)");
            s.setString(1,u); s.setString(2,p); s.setString(3,ph); s.setString(4,q1); s.setString(5,a1); s.setString(6,q2); s.setString(7,a2);
            s.executeUpdate(); return true;
        } catch (Exception e) { return false; }
    }
    private boolean checkUserExist(String u) { try(Connection c=DatabaseConnection.getConnection()){return c.createStatement().executeQuery("SELECT 1 FROM users WHERE username='"+u+"'").next();}catch(Exception e){return false;} }
    private boolean updatePassDB(String u, String p) { try(Connection c=DatabaseConnection.getConnection()){PreparedStatement s=c.prepareStatement("UPDATE users SET password=? WHERE username=?");s.setString(1,p);s.setString(2,u);return s.executeUpdate()>0;}catch(Exception e){return false;} }

    // Styles
    private JPanel createCard() { JPanel c=new JPanel(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); c.setBackground(Color.WHITE); c.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220)), new EmptyBorder(30,50,30,50))); return c; }
    private void stylePlaceholder(JTextField tf, String ph) { tf.setPreferredSize(FIELD_SIZE); tf.setMaximumSize(FIELD_SIZE); tf.setAlignmentX(Component.CENTER_ALIGNMENT); tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(0,10,0,10))); tf.setText(ph); tf.setForeground(Color.GRAY); tf.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e){if(tf.getText().equals(ph)){tf.setText("");tf.setForeground(Color.BLACK);}} public void focusLost(FocusEvent e){if(tf.getText().isEmpty()){tf.setText(ph);tf.setForeground(Color.GRAY);}} }); }
    private void stylePlaceholderPass(JPasswordField pf, String ph) { pf.setPreferredSize(FIELD_SIZE); pf.setMaximumSize(FIELD_SIZE); pf.setAlignmentX(Component.CENTER_ALIGNMENT); pf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(0,10,0,10))); pf.setEchoChar((char)0); pf.setText(ph); pf.setForeground(Color.GRAY); pf.addFocusListener(new FocusAdapter() { public void focusGained(FocusEvent e){if(new String(pf.getPassword()).equals(ph)){pf.setText("");pf.setEchoChar('•');pf.setForeground(Color.BLACK);}} public void focusLost(FocusEvent e){if(pf.getPassword().length==0){pf.setText(ph);pf.setEchoChar((char)0);pf.setForeground(Color.GRAY);}} }); }
    private void styleButton(JButton b, Color c) { b.setBackground(c); b.setForeground(Color.WHITE); b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setFocusPainted(false); b.setMaximumSize(FIELD_SIZE); }
    private void styleHyperlink(JLabel l) { l.setForeground(Color.GRAY); l.setCursor(new Cursor(Cursor.HAND_CURSOR)); }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new TicketLoginGUI().setVisible(true)); }
}