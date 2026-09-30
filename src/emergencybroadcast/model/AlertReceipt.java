package emergencybroadcast.model;

import java.sql.Timestamp;

/**
 * Model biểu diễn thông tin xác nhận nhận cảnh báo từ các máy trạm trong mạng.
 * Tương ứng với bảng 'alert_receipts' trong CSDL MySQL.
 */
public class AlertReceipt {

    private int id;
    private int alertId;           // Khóa ngoại tham chiếu đến alerts.id
    private String receiverName;   // Tên máy nhận / Tên phòng
    private String receiverIp;     // Địa chỉ IP của máy nhận
    private Timestamp receivedAt;  // Thời gian máy trạm nhận được cảnh báo
    private String status;         // 'RECEIVED', 'ACKNOWLEDGED'

    public AlertReceipt() {
    }

    public AlertReceipt(int id, int alertId, String receiverName, String receiverIp, Timestamp receivedAt, String status) {
        this.id = id;
        this.alertId = alertId;
        this.receiverName = receiverName;
        this.receiverIp = receiverIp;
        this.receivedAt = receivedAt;
        this.status = status;
    }

    public AlertReceipt(int alertId, String receiverName, String receiverIp, String status) {
        this.alertId = alertId;
        this.receiverName = receiverName;
        this.receiverIp = receiverIp;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverIp() {
        return receiverIp;
    }

    public void setReceiverIp(String receiverIp) {
        this.receiverIp = receiverIp;
    }

    public Timestamp getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Timestamp receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AlertReceipt{" +
                "id=" + id +
                ", alertId=" + alertId +
                ", receiverName='" + receiverName + '\'' +
                ", receiverIp='" + receiverIp + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
