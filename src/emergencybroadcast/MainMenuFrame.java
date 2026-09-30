package emergencybroadcast;

import emergencybroadcast.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Giao diện Menu chính (MainMenuFrame) dành cho ADMIN và OPERATOR.
 * Cung cấp lối tắt điều hướng đến các chức năng: Phát cảnh báo, Nhận cảnh báo, Xem lịch sử và Đăng xuất.
 */
public class MainMenuFrame extends JFrame {

    private User currentUser;

    private JLabel lblWelcome;
    private JLabel lblRoleInfo;
    private JButton btnSender;
    private JButton btnReceiver;
    private JButton btnHistory;
    private JButton btnLogout;
    private JButton btnExit;

    public MainMenuFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("MENU CHÍNH - HỆ THỐNG THÔNG BÁO KHẨN CẤP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);

        String displayName = (currentUser != null) ? currentUser.getFullName() : "N/A";
        String displayRole = (currentUser != null) ? currentUser.getRole() : "N/A";

        lblWelcome = new JLabel("Xin chào, " + displayName + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblWelcome.setForeground(new Color(41, 128, 185));

        lblRoleInfo = new JLabel("Quyền hạn: [" + displayRole + "] - Hãy lựa chọn chức năng bên dưới", SwingConstants.CENTER);
        lblRoleInfo.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblRoleInfo.setForeground(new Color(127, 140, 141));

        headerPanel.add(lblWelcome);
        headerPanel.add(lblRoleInfo);

        // 2. Buttons Grid Panel
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setOpaque(false);

        btnSender = createStyledButton("PHÁT CẢNH BÁO KHẨN CẤP", new Color(192, 57, 43)); // Đỏ
        btnReceiver = createStyledButton("CHẾ ĐỘ LẮNG NGHE CẢNH BÁO", new Color(39, 174, 96)); // Xanh lá
        btnHistory = createStyledButton("XEM LỊCH SỬ CẢNH BÁO", new Color(142, 68, 173)); // Tím
        btnLogout = createStyledButton("ĐĂNG XUẤT TÀI KHOẢN", new Color(211, 84, 0)); // Cam

        gridPanel.add(btnSender);
        gridPanel.add(btnReceiver);
        gridPanel.add(btnHistory);
        gridPanel.add(btnLogout);

        // 3. Footer Exit Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        btnExit = new JButton("Thoát Ứng Dụng");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnExit.setBackground(new Color(149, 165, 166));
        btnExit.setForeground(Color.WHITE);
        btnExit.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnExit.setOpaque(true);
        btnExit.setContentAreaFilled(true);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        footerPanel.add(btnExit);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- XỬ LÝ SỰ KIỆN NÚT BẤM ---

        // Nút Phát cảnh báo -> Mở SenderFrame
        btnSender.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    SenderFrame senderFrame = new SenderFrame(currentUser);
                    senderFrame.setVisible(true);
                });
            }
        });

        // Nút Nhận cảnh báo -> Mở ReceiverFrame
        btnReceiver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    ReceiverFrame receiverFrame = new ReceiverFrame(currentUser);
                    receiverFrame.setVisible(true);
                });
            }
        });

        // Nút Xem lịch sử -> Mở HistoryFrame
        btnHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    HistoryFrame historyFrame = new HistoryFrame();
                    historyFrame.setVisible(true);
                });
            }
        });

        // Nút Đăng xuất
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        MainMenuFrame.this,
                        "Bạn có muốn đăng xuất khỏi tài khoản?",
                        "Đăng xuất",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    MainMenuFrame.this.dispose();
                    SwingUtilities.invokeLater(() -> {
                        LoginFrame loginFrame = new LoginFrame();
                        loginFrame.setVisible(true);
                    });
                }
            }
        });

        // Nút Thoát
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

   private JButton createStyledButton(String text, Color bg) {
    JButton btn = new JButton(text);

    // Dùng giao diện nút cơ bản của Swing
    btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());

    btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);

    btn.setOpaque(true);
    btn.setContentAreaFilled(true);
    btn.setBorderPainted(true);
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    return btn;
}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        User testUser = new User("admin", "admin123", "Quản Trị Viên Demo", "ADMIN");
        SwingUtilities.invokeLater(() -> new MainMenuFrame(testUser).setVisible(true));
    }
}
