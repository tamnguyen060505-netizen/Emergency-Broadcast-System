package emergencybroadcast.network;

import emergencybroadcast.model.Alert;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Lớp chịu trách nhiệm phát gói tin UDP Multicast ra toàn bộ mạng LAN.
 * Dải IP Multicast mặc định: 239.10.10.10
 * Cổng Port mặc định: 5000
 */
public class MulticastSender {

    // Thông số mạng UDP Multicast cố định theo đề tài
    public static final String MULTICAST_IP = "239.10.10.10";
    public static final int PORT = 5000;

    /**
     * Phương thức chính phát một thông báo Alert qua giao thức UDP Multicast.
     * @param alert Đối tượng Alert chứa thông tin sự cố
     * @return true nếu phát gói tin thành công, false nếu bị lỗi mạng
     */
    public static boolean sendAlert(Alert alert) {
        if (alert == null) {
            System.err.println("-> [MULTICAST SENDER] Lỗi: Alert object bị null!");
            return false;
        }

        // Định dạng thời gian gửi
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = (alert.getCreatedAt() != null) ? sdf.format(alert.getCreatedAt()) : sdf.format(new Date());

        // Đóng gói thông tin thành chuỗi chuẩn hóa Key-Value UTF-8
        String rawMessage = String.format(
                "ALERT_CODE=%s;TYPE=%s;SEVERITY=%s;LOCATION=%s;MESSAGE=%s;SENDER_USER=%s;SENDER_IP=%s;TIME=%s;ALERT_ID=%d",
                sanitize(alert.getAlertCode()),
                sanitize(alert.getAlertType()),
                sanitize(alert.getSeverity()),
                sanitize(alert.getLocation()),
                sanitize(alert.getMessage()),
                sanitize(alert.getSenderUsername()),
                sanitize(alert.getSenderIp()),
                timeStr,
                alert.getId()
        );

        return sendRawMessage(rawMessage);
    }

    /**
     * Gửi trực tiếp một chuỗi byte thô đến dải IP Multicast 239.10.10.10:5000.
     * @param rawMessage Chuỗi văn bản cần phát
     * @return true nếu thành công, false nếu thất bại
     */
    public static boolean sendRawMessage(String rawMessage) {
        byte[] buffer = rawMessage.getBytes(StandardCharsets.UTF_8);

        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress groupAddress = InetAddress.getByName(MULTICAST_IP);
            socket.setReuseAddress(true);
            socket.setTimeToLive(32); // Đảm bảo gói tin đi qua các Switch/Router LAN
            try {
                socket.setOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP, true);
            } catch (Exception ignored) {
            }

            // Đóng gói và phát gói tin
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, groupAddress, PORT);
            socket.send(packet);

            System.out.println("-> [MULTICAST SENDER THÀNH CÔNG] Đã phát gói UDP đến " + MULTICAST_IP + ":" + PORT);
            System.out.println("   Dung lượng gói: " + buffer.length + " bytes");
            System.out.println("   Nội dung: " + rawMessage);
            return true;

        } catch (UnknownHostException e) {
            System.err.println("-> [LỖI MULTICAST] Địa chỉ Multicast IP không hợp lệ: " + MULTICAST_IP);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("-> [LỖI MULTICAST] Lỗi gửi gói tin UDP qua mạng: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loạibỏ các ký tự phân cách ';' trong dữ liệu nhập để tránh phá vỡ cấu trúc chuỗi Key-Value.
     */
    private static String sanitize(String input) {
        if (input == null) return "";
        return input.replace(";", ",");
    }

    /**
     * Hàm main hỗ trợ chạy thử nghiệm gửi gói tin Multicast trực tiếp trong NetBeans.
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  TEST GỬI GÓI TIN UDP MULTICAST");
        System.out.println("==================================================");

        Alert testAlert = new Alert(
                "ALT-TEST-001",
                "FIRE",
                "CRITICAL",
                "Tầng 3 - Phòng Máy Server",
                "Cảnh báo thử nghiệm phát tin Multicast!",
                "127.0.0.1",
                "admin"
        );
        testAlert.setId(1);

        boolean success = sendAlert(testAlert);
        System.out.println("Kết quả test gửi: " + (success ? "THÀNH CÔNG" : "THẤT BẠI"));
    }
}
