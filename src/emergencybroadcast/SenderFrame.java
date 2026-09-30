package emergencybroadcast;

import emergencybroadcast.dao.AlertDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.User;
import emergencybroadcast.network.MulticastSender;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Giao diện Phát Cảnh Báo Khẩn Cấp (SenderFrame) dành cho ADMIN/OPERATOR.
 * Cho phép nhập thông tin sự cố, lưu vào CSDL MySQL và phát gói UDP Multicast đến mạng LAN.
 */
public class SenderFrame extends JFrame {

    private User currentUser;
    private AlertDAO alertDAO;

    // Các thành phần GUI bắt buộc
    private JComboBox<String> cboAlertType;
    private JComboBox<String> cboSeverity;
    private JTextField txtLocation;
    private JTextArea txtMessage;
    private JButton btnSend;
    private JButton btnClear;
    private JButton btnHistory;
    private JButton btnBack;
    private JTable tblAlerts;
    private DefaultTableModel tableModel;

    public SenderFrame(User user) {
        this.currentUser = user;
        this.alertDAO = new AlertDAO();
        initComponents();
        loadAlertHistoryToTable();
    }

    private void initComponents() {
        setTitle("MÁY PHÁT CẢNH BÁO KHẨN CẤP (SENDER)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel lblHeader = new JLabel("TRUNG TÂM PHÁT CẢNH BÁO KHẨN CẤP");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(new Color(192, 57, 43));
        headerPanel.add(lblHeader);

        // 2. Input Form Panel (Left/Top Panel)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                "Thành Phần Khai Báo Sự Cố",
                0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(44, 62, 80)
        ));
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Alert Type & Severity
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        formPanel.add(new JLabel("Loại sự cố:"), gbc);

        String[] alertTypes = {"FIRE", "SECURITY", "POWER", "GAS", "OTHER"};
        cboAlertType = new JComboBox<>(alertTypes);
        cboAlertType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(cboAlertType, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        formPanel.add(new JLabel("Mức độ:"), gbc);

        String[] severities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        cboSeverity = new JComboBox<>(severities);
        cboSeverity.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cboSeverity.setSelectedItem("HIGH");
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.35;
        formPanel.add(cboSeverity, gbc);

        // Row 1: Location
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        formPanel.add(new JLabel("Vị trí sự cố:"), gbc);

        txtLocation = new JTextField();
        txtLocation.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 0.85;
        formPanel.add(txtLocation, gbc);
        gbc.gridwidth = 1; // reset

        // Row 2: Message Details
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        formPanel.add(new JLabel("Nội dung cảnh báo:"), gbc);

        txtMessage = new JTextArea(3, 20);
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        JScrollPane scrollMsg = new JScrollPane(txtMessage);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 0.85;
        formPanel.add(scrollMsg, gbc);
        gbc.gridwidth = 1; // reset

        // Row 3: Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnPanel.setOpaque(false);

        btnSend = new JButton("GỬI CẢNH BÁO");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setBackground(new Color(192, 57, 43));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnClear = new JButton("Xóa Dữ Liệu");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClear.setBackground(new Color(149, 165, 166));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);

        btnHistory = new JButton("Xem Lịch Sử");
        btnHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnHistory.setBackground(new Color(142, 68, 173));
        btnHistory.setForeground(Color.WHITE);
        btnHistory.setFocusPainted(false);

        btnBack = new JButton("Quay Lại Menu");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.setBackground(new Color(52, 73, 94));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);

        btnPanel.add(btnSend);
        btnPanel.add(btnClear);
        btnPanel.add(btnHistory);
        btnPanel.add(btnBack);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(btnPanel, gbc);

        // 3. Table Panel (Display Sent Alerts History)
        String[] columnNames = {"Mã Cảnh Báo", "Loại Sự Cố", "Mức Độ", "Vị Trí", "Nội Dung", "Thời Gian Gửi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho chỉnh sửa ô trong bảng trực tiếp
            }
        };
        tblAlerts = new JTable(tableModel);
        tblAlerts.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblAlerts.setRowHeight(24);
        tblAlerts.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblAlerts.getTableHeader().setBackground(new Color(236, 240, 241));
        JScrollPane scrollTable = new JScrollPane(tblAlerts);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Danh Sách Các Cảnh Báo Đã Phát Độc Lập"));

        // Split Layout
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(formPanel, BorderLayout.CENTER);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(scrollTable, BorderLayout.CENTER);

        add(mainPanel);

        // --- SỰ KIỆN XỬ LÝ ---

        // Nút Gửi cảnh báo
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSendAlert();
            }
        });

        // Nút Xóa trắng dữ liệu nhập
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // Nút Xem lịch sử
        btnHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    HistoryFrame historyFrame = new HistoryFrame();
                    historyFrame.setVisible(true);
                });
            }
        });

        // Nút Quay lại
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SenderFrame.this.dispose();
            }
        });
    }

    /**
     * Xử lý Phát cảnh báo khẩn cấp: Validate -> Tạo mã -> Lưu MySQL -> Phát UDP Multicast.
     */
    private void handleSendAlert() {
        String alertType = (String) cboAlertType.getSelectedItem();
        String severity = (String) cboSeverity.getSelectedItem();
        String location = txtLocation.getText().trim();
        String message = txtMessage.getText().trim();

        // 1. Kiểm tra dữ liệu không được để trống
        if (location.isEmpty() || message.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ Vị trí sự cố và Nội dung cảnh báo!",
                    "Cảnh báo thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 2. Tạo mã cảnh báo duy nhất (ALERT-YYYYMMDD-HHMMSS-RAND)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String timestampStr = sdf.format(new Date());
        int randomNum = new Random().nextInt(900) + 100; // 100-999
        String alertCode = "ALT-" + timestampStr + "-" + randomNum;

        // 3. Lấy IP máy gửi và Username
        String senderIp = "127.0.0.1";
        try {
            senderIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        String senderUsername = (currentUser != null) ? currentUser.getUsername() : "admin";

        // 4. Tạo đối tượng Alert
        Alert alert = new Alert(alertCode, alertType, severity, location, message, senderIp, senderUsername);

        // 5. Lưu dữ liệu vào MySQL CSDL
        int generatedId = alertDAO.insertAlert(alert);
        if (generatedId <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi: Không thể lưu cảnh báo vào Cơ sở dữ liệu!",
                    "Lỗi CSDL",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 6. Gửi dữ liệu qua gói UDP Multicast
        boolean sendSuccess = MulticastSender.sendAlert(alert);

        if (sendSuccess) {
            JOptionPane.showMessageDialog(
                    this,
                    "Phát cảnh báo khẩn cấp thành công!\nMã cảnh báo: " + alertCode + "\nĐã gửi gói UDP Multicast đến dải 239.10.10.10:5000",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );
            clearForm();
            loadAlertHistoryToTable();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Cảnh báo đã lưu MySQL nhưng gặp LỖI MẠNG khi phát UDP Multicast!\nVui lòng kiểm tra lại Firewall/Mạng LAN.",
                    "Cảnh báo Mạng",
                    JOptionPane.WARNING_MESSAGE
            );
            loadAlertHistoryToTable();
        }
    }

    private void clearForm() {
        txtLocation.setText("");
        txtMessage.setText("");
        cboAlertType.setSelectedIndex(0);
        cboSeverity.setSelectedItem("HIGH");
        txtLocation.requestFocus();
    }

    /**
     * Tải danh sách cảnh báo đã lưu trong MySQL lên JTable.
     */
    private void loadAlertHistoryToTable() {
        tableModel.setRowCount(0); // Clear cũ
        List<Alert> alerts = alertDAO.getAllAlerts();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Alert a : alerts) {
            String timeStr = (a.getCreatedAt() != null) ? sdf.format(a.getCreatedAt()) : "";
            Object[] row = {
                    a.getAlertCode(),
                    a.getAlertType(),
                    a.getSeverity(),
                    a.getLocation(),
                    a.getMessage(),
                    timeStr
            };
            tableModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        User testUser = new User("admin", "admin123", "Quản Trị Viên", "ADMIN");
        SwingUtilities.invokeLater(() -> new SenderFrame(testUser).setVisible(true));
    }
}
