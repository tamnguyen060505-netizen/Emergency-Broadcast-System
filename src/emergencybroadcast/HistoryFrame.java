package emergencybroadcast;

import emergencybroadcast.dao.AlertDAO;
import emergencybroadcast.dao.AlertReceiptDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.AlertReceipt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Giao diện Xem Lịch Sử Cảnh Báo (HistoryFrame).
 * Cho phép tìm kiếm từ khóa, lọc theo Loại sự cố, Mức độ nghiêm trọng và xem chi tiết nhật ký máy trạm nhận.
 */
public class HistoryFrame extends JFrame {

    private AlertDAO alertDAO;
    private AlertReceiptDAO receiptDAO;

    private JTextField txtSearch;
    private JComboBox<String> cboFilterType;
    private JComboBox<String> cboFilterSeverity;
    private JButton btnSearch;
    private JButton btnReset;
    private JButton btnRefresh;
    private JButton btnViewDetails;
    private JButton btnClose;

    private JTable tblHistory;
    private DefaultTableModel tableModel;
    private List<Alert> currentAlertList;

    public HistoryFrame() {
        alertDAO = new AlertDAO();
        receiptDAO = new AlertReceiptDAO();
        initComponents();
        loadDataToTable();
    }

    private void initComponents() {
        setTitle("LỊCH SỬ THÔNG BÁO CẢNH BẢO KHẨN CẤP");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 247, 250));

        // 1. Header Title
        JLabel lblTitle = new JLabel("LỊCH SỬ VÀ TRA CỨU CẢNH BẢO KHẨN CẤP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(44, 62, 80));

        // 2. Filter & Search Panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                "Bộ Lọc & Tìm Kiếm Dữ Liệu",
                0, 0, new Font("Segoe UI", Font.BOLD, 13), new Color(52, 73, 94)
        ));
        filterPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Search keyword
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        filterPanel.add(new JLabel("Từ khóa:"), gbc);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.4;
        filterPanel.add(txtSearch, gbc);

        btnSearch = new JButton("Tìm Kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.setBackground(new Color(41, 128, 185));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.15;
        filterPanel.add(btnSearch, gbc);

        btnReset = new JButton("Xóa Bộ Lọc");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.15;
        filterPanel.add(btnReset, gbc);

        // Row 1: Combobox filters
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        filterPanel.add(new JLabel("Loại sự cố:"), gbc);

        String[] types = {"ALL", "FIRE", "SECURITY", "POWER", "GAS", "OTHER"};
        cboFilterType = new JComboBox<>(types);
        cboFilterType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.4;
        filterPanel.add(cboFilterType, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1;
        filterPanel.add(new JLabel("Mức độ:"), gbc);

        String[] severities = {"ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL"};
        cboFilterSeverity = new JComboBox<>(severities);
        cboFilterSeverity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.4;
        filterPanel.add(cboFilterSeverity, gbc);

        // 3. Table Panel
        String[] columnNames = {"ID", "Mã Cảnh Báo", "Loại Sự Cố", "Mức Độ", "Vị Trí", "Nội Dung", "Người Gửi", "Thời Gian Gửi"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHistory = new JTable(tableModel);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblHistory.setRowHeight(24);
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblHistory.getTableHeader().setBackground(new Color(236, 240, 241));
        
        // Hidden column ID
        tblHistory.getColumnModel().getColumn(0).setMinWidth(0);
        tblHistory.getColumnModel().getColumn(0).setMaxWidth(0);
        tblHistory.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollTable = new JScrollPane(tblHistory);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Danh Sách Cảnh Báo Lưu Trong CSDL"));

        // 4. Action Buttons Panel (Bottom)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        buttonPanel.setOpaque(false);

        btnViewDetails = new JButton("Xem Chi Tiết & Máy Nhận");
        btnViewDetails.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnViewDetails.setBackground(new Color(142, 68, 173));
        btnViewDetails.setForeground(Color.WHITE);
        btnViewDetails.setFocusPainted(false);
        btnViewDetails.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnRefresh = new JButton("Làm Mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setBackground(new Color(39, 174, 96));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);

        btnClose = new JButton("Đóng Window");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClose.setBackground(new Color(149, 165, 166));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);

        buttonPanel.add(btnViewDetails);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        // Container Layout Assembly
        JPanel topContainer = new JPanel(new BorderLayout(5, 5));
        topContainer.setOpaque(false);
        topContainer.add(lblTitle, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.CENTER);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(scrollTable, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- SỰ KIỆN NÚT BẤM ---

        btnSearch.addActionListener(e -> applyFilter());
        txtSearch.addActionListener(e -> applyFilter());
        cboFilterType.addActionListener(e -> applyFilter());
        cboFilterSeverity.addActionListener(e -> applyFilter());

        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cboFilterType.setSelectedIndex(0);
            cboFilterSeverity.setSelectedIndex(0);
            loadDataToTable();
        });

        btnRefresh.addActionListener(e -> loadDataToTable());

        btnViewDetails.addActionListener(e -> showSelectedAlertDetails());

        btnClose.addActionListener(e -> this.dispose());
    }

    private void loadDataToTable() {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        String type = (String) cboFilterType.getSelectedItem();
        String severity = (String) cboFilterSeverity.getSelectedItem();

        currentAlertList = alertDAO.searchAlertsFiltered(keyword, type, severity);
        tableModel.setRowCount(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Alert a : currentAlertList) {
            String timeStr = (a.getCreatedAt() != null) ? sdf.format(a.getCreatedAt()) : "";
            Object[] row = {
                    a.getId(),
                    a.getAlertCode(),
                    a.getAlertType(),
                    a.getSeverity(),
                    a.getLocation(),
                    a.getMessage(),
                    a.getSenderUsername() + " (" + a.getSenderIp() + ")",
                    timeStr
            };
            tableModel.addRow(row);
        }
    }

    private void showSelectedAlertDetails() {
        int selectedRow = tblHistory.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn 1 dòng cảnh báo trong bảng để xem chi tiết!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int alertId = (int) tableModel.getValueAt(selectedRow, 0);
        Alert alert = alertDAO.getAlertById(alertId);

        if (alert == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu cảnh báo!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<AlertReceipt> receipts = receiptDAO.getReceiptsByAlertId(alertId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== CHI TIẾT CẢNH BÁO KHẨN CẤP ===\n");
        sb.append("Mã cảnh báo: ").append(alert.getAlertCode()).append("\n");
        sb.append("Loại sự cố: ").append(alert.getAlertType()).append("\n");
        sb.append("Mức độ nghiêm trọng: ").append(alert.getSeverity()).append("\n");
        sb.append("Vị trí: ").append(alert.getLocation()).append("\n");
        sb.append("Nội dung: ").append(alert.getMessage()).append("\n");
        sb.append("Người phát: ").append(alert.getSenderUsername()).append(" (IP: ").append(alert.getSenderIp()).append(")\n");
        sb.append("Thời gian phát: ").append(alert.getCreatedAt()).append("\n\n");

        sb.append("=== NHẬT KÝ MÁY TRẠM ĐÃ NHẬN (").append(receipts.size()).append(" máy) ===\n");
        if (receipts.isEmpty()) {
            sb.append("(Chưa có máy trạm nào gửi ghi nhận vào CSDL)\n");
        } else {
            for (int i = 0; i < receipts.size(); i++) {
                AlertReceipt r = receipts.get(i);
                sb.append(i + 1).append(". ").append(r.getReceiverName())
                        .append(" [IP: ").append(r.getReceiverIp()).append("]")
                        .append(" - ").append(r.getReceivedAt())
                        .append(" (Status: ").append(r.getStatus()).append(")\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString(), 15, 45);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Chi Tiết Cảnh Báo " + alert.getAlertCode(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new HistoryFrame().setVisible(true));
    }
}
