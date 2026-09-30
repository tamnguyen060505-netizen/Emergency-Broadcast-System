package emergencybroadcast;

import emergencybroadcast.dao.UserDAO;
import emergencybroadcast.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Giao diện Đăng nhập hệ thống (LoginFrame).
 * Phân quyền truy cập: ADMIN, OPERATOR -> MainMenu / SenderFrame; RECEIVER -> ReceiverFrame.
 */
public class LoginFrame extends JFrame {

    // Khai báo các thành phần GUI
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JLabel lblTitle;
    private JLabel lblUsername;
    private JLabel lblPassword;

    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initComponents();
    }

    /**
     * Khởi tạo và sắp xếp các component trên giao diện Swing.
     */
    private void initComponents() {
        setTitle("ĐĂNG NHẬP HỆ THỐNG THÔNG BÁO KHẨN CẤP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 320);
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setResizable(false);

        // Panel chính với Padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. Title Header Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        lblTitle = new JLabel("HỆ THỐNG THÔNG BÁO KHẨN CẤP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(192, 57, 43)); // Red accent color
        titlePanel.add(lblTitle);

        // 2. Form Input Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(lblUsername, gbc);

        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        formPanel.add(txtUsername, gbc);

        lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        formPanel.add(txtPassword, gbc);

        // 3. Buttons Panel
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
buttonPanel.setOpaque(false);

btnLogin = new JButton("Đăng Nhập");
btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

// Ép sử dụng giao diện nút cơ bản
btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI());
btnLogin.setOpaque(true);
btnLogin.setContentAreaFilled(true);
btnLogin.setBorderPainted(true);

btnLogin.setBackground(new Color(41, 128, 185));
btnLogin.setForeground(Color.WHITE);
btnLogin.setFocusPainted(false);
btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));


btnExit = new JButton("Thoát");
btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 14));

// Ép sử dụng giao diện nút cơ bản
btnExit.setUI(new javax.swing.plaf.basic.BasicButtonUI());
btnExit.setOpaque(true);
btnExit.setContentAreaFilled(true);
btnExit.setBorderPainted(true);

btnExit.setBackground(new Color(149, 165, 166));
btnExit.setForeground(Color.WHITE);
btnExit.setFocusPainted(false);
btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));


buttonPanel.add(btnLogin);
buttonPanel.add(btnExit);

        // Thêm các Panel vào Frame
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Sự kiện Đăng nhập
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Nhấn Enter trên ô Password để Đăng nhập
        txtPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Sự kiện Thoát
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        LoginFrame.this,
                        "Bạn có chắc chắn muốn thoát ứng dụng?",
                        "Xác nhận thoát",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Xử lý kiểm tra tài khoản và chuyển đổi giao diện theo phân quyền.
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validate dữ liệu không rỗng
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Thực thi kiểm tra CSDL
        User user = userDAO.login(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đăng nhập thành công!\nXin chào: " + user.getFullName() + " (Quyền: " + user.getRole() + ")",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Đóng màn hình Login
            this.dispose();

            // Phân quyền điều hướng màn hình
            if ("ADMIN".equalsIgnoreCase(user.getRole()) || "OPERATOR".equalsIgnoreCase(user.getRole())) {
                // Chuyển sang MainMenuFrame (hoặc SenderFrame)
                SwingUtilities.invokeLater(() -> {
                    MainMenuFrame mainMenu = new MainMenuFrame(user);
                    mainMenu.setVisible(true);
                });
            } else {
                // Chuyển sang ReceiverFrame cho máy nhận
                SwingUtilities.invokeLater(() -> {
                    ReceiverFrame receiver = new ReceiverFrame(user);
                    receiver.setVisible(true);
                });
            }

        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Tên đăng nhập hoặc mật khẩu không chính xác!\nVui lòng thử lại.",
                    "Lỗi đăng nhập",
                    JOptionPane.ERROR_MESSAGE
            );
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
