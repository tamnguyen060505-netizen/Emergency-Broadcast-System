package emergencybroadcast.dao;

import emergencybroadcast.database.DatabaseConnection;
import emergencybroadcast.model.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) quản lý các thao tác dữ liệu với bảng 'alerts'.
 */
public class AlertDAO {

    /**
     * Lưu một bản ghi cảnh báo mới vào CSDL.
     * @param alert Đối tượng Alert cần lưu
     * @return ID tự tăng vừa được tạo trong CSDL, hoặc -1 nếu thất bại
     */
    public int insertAlert(Alert alert) {
        String sql = "INSERT INTO alerts (alert_code, alert_type, severity, location, message, sender_ip, sender_username) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, alert.getAlertCode());
            pstmt.setString(2, alert.getAlertType());
            pstmt.setString(3, alert.getSeverity());
            pstmt.setString(4, alert.getLocation());
            pstmt.setString(5, alert.getMessage());
            pstmt.setString(6, alert.getSenderIp());
            pstmt.setString(7, alert.getSenderUsername());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        alert.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi insertAlert(): " + e.getMessage());
        }
        return -1;
    }

    /**
     * Lấy toàn bộ danh sách cảnh báo đã phát đi (sắp xếp giảm dần theo thời gian mới nhất).
     * @return Danh sách các Alert
     */
    public List<Alert> getAllAlerts() {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT id, alert_code, alert_type, severity, location, message, sender_ip, sender_username, created_at " +
                     "FROM alerts ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Alert alert = mapResultSetToAlert(rs);
                list.add(alert);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getAllAlerts(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm thông tin cảnh báo theo ID.
     * @param id Khóa chính của alert
     * @return Đối tượng Alert hoặc null nếu không tìm thấy
     */
    public Alert getAlertById(int id) {
        String sql = "SELECT id, alert_code, alert_type, severity, location, message, sender_ip, sender_username, created_at " +
                     "FROM alerts WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAlert(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getAlertById(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Lọc danh sách cảnh báo theo Loại sự cố (alert_type).
     * @param alertType Loại sự cố (FIRE, SECURITY, POWER, GAS, OTHER)
     * @return Danh sách các Alert thuộc loại chỉ định
     */
    public List<Alert> getAlertsByType(String alertType) {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT id, alert_code, alert_type, severity, location, message, sender_ip, sender_username, created_at " +
                     "FROM alerts WHERE alert_type = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, alertType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAlert(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getAlertsByType(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Lọc danh sách cảnh báo theo Mức độ nghiêm trọng (severity).
     * @param severity Mức độ (LOW, MEDIUM, HIGH, CRITICAL)
     * @return Danh sách các Alert thuộc mức độ chỉ định
     */
    public List<Alert> getAlertsBySeverity(String severity) {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT id, alert_code, alert_type, severity, location, message, sender_ip, sender_username, created_at " +
                     "FROM alerts WHERE severity = ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, severity);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAlert(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getAlertsBySeverity(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm cảnh báo theo từ khóa xuất hiện trong nội dung (message) hoặc vị trí (location).
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách các Alert thỏa mãn
     */
    public List<Alert> searchAlerts(String keyword) {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT id, alert_code, alert_type, severity, location, message, sender_ip, sender_username, created_at " +
                     "FROM alerts WHERE message LIKE ? OR location LIKE ? OR alert_code LIKE ? ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAlert(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi searchAlerts(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm và lọc đa điều kiện tổng hợp (Keyword + AlertType + Severity).
     */
    public List<Alert> searchAlertsFiltered(String keyword, String alertType, String severity) {
        List<Alert> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, alert_code, alert_type, severity, location, message, sender_ip, sender_username, created_at FROM alerts WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (message LIKE ? OR location LIKE ? OR alert_code LIKE ?) ");
            String searchPattern = "%" + keyword.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (alertType != null && !alertType.equals("ALL") && !alertType.trim().isEmpty()) {
            sql.append("AND alert_type = ? ");
            params.add(alertType.trim());
        }

        if (severity != null && !severity.equals("ALL") && !severity.trim().isEmpty()) {
            sql.append("AND severity = ? ");
            params.add(severity.trim());
        }

        sql.append("ORDER BY created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAlert(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi searchAlertsFiltered(): " + e.getMessage());
        }
        return list;
    }

    /**
     * Helper method chuyển đổi dòng ResultSet thành đối tượng Alert.
     */
    private Alert mapResultSetToAlert(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setId(rs.getInt("id"));
        alert.setAlertCode(rs.getString("alert_code"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setSeverity(rs.getString("severity"));
        alert.setLocation(rs.getString("location"));
        alert.setMessage(rs.getString("message"));
        alert.setSenderIp(rs.getString("sender_ip"));
        alert.setSenderUsername(rs.getString("sender_username"));
        alert.setCreatedAt(rs.getTimestamp("created_at"));
        return alert;
    }
}
