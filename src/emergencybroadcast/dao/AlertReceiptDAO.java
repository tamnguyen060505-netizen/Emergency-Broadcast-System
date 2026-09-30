package emergencybroadcast.dao;

import emergencybroadcast.database.DatabaseConnection;
import emergencybroadcast.model.AlertReceipt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) quản lý các thao tác dữ liệu với bảng 'alert_receipts'.
 */
public class AlertReceiptDAO {

    /**
     * Thêm nhật ký xác nhận nhận cảnh báo từ máy trạm vào CSDL.
     * @param receipt Đối tượng AlertReceipt cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insertReceipt(AlertReceipt receipt) {
        String sql = "INSERT INTO alert_receipts (alert_id, receiver_name, receiver_ip, status) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, receipt.getAlertId());
            pstmt.setString(2, receipt.getReceiverName());
            pstmt.setString(3, receipt.getReceiverIp());
            pstmt.setString(4, receipt.getStatus() != null ? receipt.getStatus() : "RECEIVED");
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        receipt.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi insertReceipt(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy danh sách nhật ký các máy trạm đã nhận của một cảnh báo cụ thể.
     * @param alertId Khóa chính id của bản ghi cảnh báo trong bảng alerts
     * @return Danh sách các AlertReceipt
     */
    public List<AlertReceipt> getReceiptsByAlertId(int alertId) {
        List<AlertReceipt> list = new ArrayList<>();
        String sql = "SELECT id, alert_id, receiver_name, receiver_ip, received_at, status " +
                     "FROM alert_receipts WHERE alert_id = ? ORDER BY received_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, alertId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AlertReceipt receipt = mapResultSetToAlertReceipt(rs);
                    list.add(receipt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getReceiptsByAlertId(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy toàn bộ danh sách nhật ký nhận cảnh báo trong hệ thống.
     * @return Danh sách tất cả AlertReceipt
     */
    public List<AlertReceipt> getAllReceipts() {
        List<AlertReceipt> list = new ArrayList<>();
        String sql = "SELECT id, alert_id, receiver_name, receiver_ip, received_at, status " +
                     "FROM alert_receipts ORDER BY received_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                AlertReceipt receipt = mapResultSetToAlertReceipt(rs);
                list.add(receipt);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getAllReceipts(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Helper method chuyển đổi dữ liệu dòng ResultSet thành đối tượng AlertReceipt.
     */
    private AlertReceipt mapResultSetToAlertReceipt(ResultSet rs) throws SQLException {
        AlertReceipt receipt = new AlertReceipt();
        receipt.setId(rs.getInt("id"));
        receipt.setAlertId(rs.getInt("alert_id"));
        receipt.setReceiverName(rs.getString("receiver_name"));
        receipt.setReceiverIp(rs.getString("receiver_ip"));
        receipt.setReceivedAt(rs.getTimestamp("received_at"));
        receipt.setStatus(rs.getString("status"));
        return receipt;
    }
}
