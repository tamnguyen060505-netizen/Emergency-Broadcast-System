package emergencybroadcast.model;

import java.sql.Timestamp;

/**
 * Model biểu diễn thông tin một cảnh báo khẩn cấp được phát đi.
 * Tương ứng với bảng 'alerts' trong CSDL MySQL.
 */
public class Alert {

    private int id;
    private String alertCode;       // Ví dụ: ALT-20260922-103000-849
    private String alertType;       // 'FIRE', 'SECURITY', 'POWER', 'GAS', 'OTHER'
    private String severity;        // 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'
    private String location;        // Ví dụ: Tầng 3 - Phòng 302
    private String message;         // Nội dung chi tiết cảnh báo
    private String senderIp;        // IP máy tính phát cảnh báo
    private String senderUsername;  // Username người phát cảnh báo
    private Timestamp createdAt;    // Thời gian tạo/gửi cảnh báo

    public Alert() {
    }

    public Alert(int id, String alertCode, String alertType, String severity, String location, 
                 String message, String senderIp, String senderUsername, Timestamp createdAt) {
        this.id = id;
        this.alertCode = alertCode;
        this.alertType = alertType;
        this.severity = severity;
        this.location = location;
        this.message = message;
        this.senderIp = senderIp;
        this.senderUsername = senderUsername;
        this.createdAt = createdAt;
    }

    public Alert(String alertCode, String alertType, String severity, String location, 
                 String message, String senderIp, String senderUsername) {
        this.alertCode = alertCode;
        this.alertType = alertType;
        this.severity = severity;
        this.location = location;
        this.message = message;
        this.senderIp = senderIp;
        this.senderUsername = senderUsername;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", alertCode='" + alertCode + '\'' +
                ", alertType='" + alertType + '\'' +
                ", severity='" + severity + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
