package emergencybroadcast;

import emergencybroadcast.model.Alert;
import emergencybroadcast.model.User;
import emergencybroadcast.network.MulticastReceiver;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Giao diện Máy Nhận Cảnh Báo Khẩn Cấp (ReceiverFrame).
 * Hiển thị nổi bật các thông báo cảnh báo nhận được từ mạng UDP Multicast,
 * thay đổi màu nền theo mức độ nghiêm trọng và phát âm thanh beep.
 */
public class ReceiverFrame extends JFrame implements MulticastReceiver.OnAlertReceivedListener {

    private User currentUser;
    private MulticastReceiver multicastReceiver;

    // Các thành phần GUI bắt buộc
    private JLabel lblStatus;
    private JLabel lblMulticastIp;
    private JLabel lblPort;
    
    // Banner cảnh báo nổi bật
    private JPanel pnlAlertBanner;
    private JLabel lblAlertTitle;
    private JTextArea txtAlertContent;
    private JLabel lblSenderIp;
    private JLabel lblReceivedTime;
    private JButton btnAcknowledge;

    // Bảng lịch sử nhận tại máy trạm
    private JTable tblReceiptHistory;
    private DefaultTableModel tableModel;
    private JButton btnBack;

    public ReceiverFrame(User user) {
        this.currentUser = user;
        initComponents();
        resetToNormalState();
        startNetworkReceiver();
    }

    public ReceiverFrame() {
        this(null);
    }

    private void initComponents() {
        String title = "MÁY TRẠM NHẬN CẢNH BÁO KHẨN CẤP (RECEIVER)";
        if (currentUser != null) {
            title += " - Người dùng: " + currentUser.getFullName();
        }
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. Connection Status Bar Panel (Top Header)
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setBackground(new Color(236, 240, 241));

        lblStatus = new JLabel("Trạng thái: KẾT NỐI VÀ LẮNG NGHE...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(new Color(39, 174, 96));

        lblMulticastIp = new JLabel("Multicast IP: 239.10.10.10");
        lblMulticastIp.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        lblPort = new JLabel("Port: 5000");
        lblPort.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        statusPanel.add(lblStatus);
        statusPanel.add(new JSeparator(SwingConstants.VERTICAL));
        statusPanel.add(lblMulticastIp);
        statusPanel.add(new JSeparator(SwingConstants.VERTICAL));
        statusPanel.add(lblPort);

        // 2. Banner Cảnh Báo Nổi Bật (Center Main Display)
        pnlAlertBanner = new JPanel(new BorderLayout(10, 10));
        pnlAlertBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        pnlAlertBanner.setPreferredSize(new Dimension(850, 240));

        lblAlertTitle = new JLabel("KHÔNG CÓ CẢNH BẢO MỚI", SwingConstants.CENTER);
        lblAlertTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));

        txtAlertContent = new JTextArea("Hệ thống hoạt động bình thường. Đang sẵn sàng nhận tín hiệu cảnh báo từ mạng LAN...");
        txtAlertContent.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtAlertContent.setEditable(false);
        txtAlertContent.setLineWrap(true);
        txtAlertContent.setWrapStyleWord(true);
        txtAlertContent.setOpaque(false);

        // Sub-info bar
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        infoBar.setOpaque(false);
        lblSenderIp = new JLabel("IP Máy Gửi: ---");
        lblSenderIp.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lblReceivedTime = new JLabel("Thời Gian Nhận: ---");
        lblReceivedTime.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnAcknowledge = new JButton("XÁC NHẬN ĐÃ NHẬN");
        btnAcknowledge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAcknowledge.setBackground(new Color(52, 152, 219));
        btnAcknowledge.setForeground(Color.WHITE);
        btnAcknowledge.setFocusPainted(false);
        btnAcknowledge.setCursor(new Cursor(Cursor.HAND_CURSOR));

        infoBar.add(lblSenderIp);
        infoBar.add(lblReceivedTime);
        infoBar.add(btnAcknowledge);

        pnlAlertBanner.add(lblAlertTitle, BorderLayout.NORTH);
        pnlAlertBanner.add(txtAlertContent, BorderLayout.CENTER);
        pnlAlertBanner.add(infoBar, BorderLayout.SOUTH);

        // 3. Receipt History Table (Bottom List)
        String[] columnNames = {"STT", "Mã Cảnh Báo", "Loại Sự Cố", "Mức Độ", "Vị Trí", "IP Máy Gửi", "Thời Gian Nhận"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblReceiptHistory = new JTable(tableModel);
        tblReceiptHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblReceiptHistory.setRowHeight(24);
        tblReceiptHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane scrollTable = new JScrollPane(tblReceiptHistory);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Nhật Ký Cảnh Báo Đã Nhận Trên Máy Này"));

        // Footer Action Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        btnBack = new JButton("Đóng Màn Hình");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.setBackground(new Color(149, 165, 166));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        footerPanel.add(btnBack);

        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(pnlAlertBanner, BorderLayout.NORTH);
        centerContainer.add(scrollTable, BorderLayout.CENTER);

        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Sự kiện nút Xác nhận đã nhận
        btnAcknowledge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToNormalState();
            }
        });

        // Nút Đóng
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopNetworkReceiver();
                ReceiverFrame.this.dispose();
            }
        });

        // Tự động ngắt Multicast thread khi đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopNetworkReceiver();
            }
        });
    }

    private void startNetworkReceiver() {
        multicastReceiver = new MulticastReceiver(this);
        multicastReceiver.startListening();
        updateConnectionStatus(true);
    }

    private void stopNetworkReceiver() {
        if (multicastReceiver != null) {
            multicastReceiver.stopListening();
        }
        updateConnectionStatus(false);
    }

    // --- CALLBACK INTERFACE IMPLEMENTATION ---

    @Override
    public void onAlertReceived(Alert alert) {
        displayAlert(alert);
    }

    @Override
    public void onError(String errorMessage) {
        updateConnectionStatus(false);
    }

    /**
     * Cập nhật thông tin cảnh báo nhận được lên giao diện Swing và thay đổi màu nền theo mức độ.
     * @param alert Đối tượng Alert nhận được
     */
    public void displayAlert(Alert alert) {
        if (alert == null) return;

        String severity = alert.getSeverity().toUpperCase();

        Color bgColor;
        Color fgColor;
        Font titleFont;

        switch (severity) {
            case "LOW":
                bgColor = new Color(46, 204, 113); // Green
                fgColor = Color.WHITE;
                titleFont = new Font("Segoe UI", Font.BOLD, 22);
                break;
            case "MEDIUM":
                bgColor = new Color(241, 196, 15); // Yellow
                fgColor = Color.BLACK;
                titleFont = new Font("Segoe UI", Font.BOLD, 24);
                break;
            case "HIGH":
                bgColor = new Color(230, 126, 34); // Orange
                fgColor = Color.WHITE;
                titleFont = new Font("Segoe UI", Font.BOLD, 26);
                break;
            case "CRITICAL":
            default:
                bgColor = new Color(231, 76, 60); // Red
                fgColor = Color.YELLOW;
                titleFont = new Font("Segoe UI", Font.BOLD, 28);
                break;
        }

        pnlAlertBanner.setBackground(bgColor);
        lblAlertTitle.setText("CẢNH BÁO: [" + alert.getAlertType() + "] - MỨC ĐỘ: " + severity);
        lblAlertTitle.setFont(titleFont);
        lblAlertTitle.setForeground(fgColor);

        txtAlertContent.setText("VỊ TRÍ SỰ CỐ: " + alert.getLocation() + "\n\nNỘI DUNG: " + alert.getMessage());
        txtAlertContent.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtAlertContent.setForeground(fgColor);

        lblSenderIp.setText("IP Máy Gửi: " + alert.getSenderIp());
        lblSenderIp.setForeground(fgColor);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(new Date());
        lblReceivedTime.setText("Thời Gian Nhận: " + timeStr);
        lblReceivedTime.setForeground(fgColor);

        int nextStt = tableModel.getRowCount() + 1;
        Object[] row = {
                nextStt,
                alert.getAlertCode(),
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getLocation(),
                alert.getSenderIp(),
                timeStr
        };
        tableModel.insertRow(0, row);

        // Phát tiếng beep cảnh báo
        Toolkit.getDefaultToolkit().beep();
    }

    public void resetToNormalState() {
        pnlAlertBanner.setBackground(new Color(236, 240, 241));
        lblAlertTitle.setText("KHÔNG CÓ CẢNH BẢO MỚI");
        lblAlertTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblAlertTitle.setForeground(new Color(127, 140, 141));

        txtAlertContent.setText("Hệ thống đang chạy thường trực. Đang sẵn sàng nhận gói tin UDP Multicast...");
        txtAlertContent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtAlertContent.setForeground(new Color(44, 62, 80));

        lblSenderIp.setText("IP Máy Gửi: ---");
        lblSenderIp.setForeground(Color.BLACK);

        lblReceivedTime.setText("Thời Gian Nhận: ---");
        lblReceivedTime.setForeground(Color.BLACK);
    }

    public void updateConnectionStatus(boolean isListening) {
        if (isListening) {
            lblStatus.setText("Trạng thái: ĐANG KẾT NỐI VÀ LẮNG NGHE...");
            lblStatus.setForeground(new Color(39, 174, 96));
        } else {
            lblStatus.setText("Trạng thái: MẤT KẾT NỐI MẠNG / ĐÃ DỪNG!");
            lblStatus.setForeground(new Color(231, 76, 60));
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            ReceiverFrame receiver = new ReceiverFrame();
            receiver.setVisible(true);
        });
    }
}
