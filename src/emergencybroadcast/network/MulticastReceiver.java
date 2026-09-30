package emergencybroadcast.network;

import emergencybroadcast.dao.AlertReceiptDAO;
import emergencybroadcast.model.Alert;
import emergencybroadcast.model.AlertReceipt;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp MulticastReceiver chạy trên một Thread riêng biệt để ngầm lắng nghe các gói tin UDP Multicast
 * từ dải 239.10.10.10:5000 mà không làm treo giao diện Swing.
 */
public class MulticastReceiver implements Runnable {

    public static final String MULTICAST_IP = "239.10.10.10";
    public static final int PORT = 5000;
    private static final int BUFFER_SIZE = 4096;

    private MulticastSocket socket;
    private InetAddress groupAddress;
    private NetworkInterface netIf;
    private boolean running = false;
    private Thread workerThread;

    private OnAlertReceivedListener listener;
    private AlertReceiptDAO receiptDAO;

    /**
     * Interface Callback gửi dữ liệu Alert đã bóc tách về Giao diện ReceiverFrame.
     */
    public interface OnAlertReceivedListener {
        void onAlertReceived(Alert alert);
        void onError(String errorMessage);
    }

    public MulticastReceiver(OnAlertReceivedListener listener) {
        this.listener = listener;
        this.receiptDAO = new AlertReceiptDAO();
    }

    /**
     * Khởi động Thread ngầm lắng nghe Multicast.
     */
    public synchronized void startListening() {
        if (running) return;

        running = true;
        workerThread = new Thread(this, "MulticastReceiverThread");
        workerThread.setDaemon(true); // Thread ngầm tự ngắt khi ứng dụng tắt
        workerThread.start();
    }

    /**
     * Dừng việc lắng nghe và giải phóng Socket.
     */
    public synchronized void stopListening() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            try {
                if (groupAddress != null) {
                    if (netIf != null) {
                        socket.leaveGroup(new InetSocketAddress(groupAddress, PORT), netIf);
                    }
                }
            } catch (Exception ignored) {
            }
            socket.close();
        }
    }

    @Override
    public void run() {
        try {
            groupAddress = InetAddress.getByName(MULTICAST_IP);
            socket = new MulticastSocket(PORT);
            socket.setReuseAddress(true);

            // 1. Bật multicast loopback để cho phép nhận gói tin phát từ cùng 1 máy tính
            try {
                socket.setOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP, true);
            } catch (Exception ignored) {
            }

            // 2. Tham gia nhóm Multicast trên TẤT CẢ các Card mạng (Wi-Fi, Ethernet, Loopback)
            InetSocketAddress groupSocketAddr = new InetSocketAddress(groupAddress, PORT);
            boolean joinedAny = false;
            try {
                java.util.Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                while (ifaces != null && ifaces.hasMoreElements()) {
                    NetworkInterface iface = ifaces.nextElement();
                    if (iface.isUp() && iface.supportsMulticast()) {
                        try {
                            socket.joinGroup(groupSocketAddr, iface);
                            joinedAny = true;
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            if (!joinedAny) {
                // Fallback nếu không liệt kê được interface
                socket.joinGroup(groupSocketAddr, null);
            }

            System.out.println("-> [MULTICAST RECEIVER] Đã tham gia thành công nhóm " + MULTICAST_IP + ":" + PORT);

            byte[] buffer = new byte[BUFFER_SIZE];

            while (running && !socket.isClosed()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                // Nhận gói tin UDP (Hàm này sẽ block cho đến khi có gói tin tới)
                socket.receive(packet);

                String receivedData = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("-> [MULTICAST RECEIVER NHẬN GÓI TIN] " + receivedData);

                // Bóc tách dữ liệu chuỗi Key-Value
                Alert alert = parseRawMessageToAlert(receivedData);

                if (alert != null) {
                    // 1. Tự động lưu nhật ký nhận vào MySQL Database
                    saveAlertReceiptToDB(alert, packet.getAddress().getHostAddress());

                    // 2. Chuyển đối tượng Alert về Cập nhật Giao diện Swing an toàn trên EDT
                    if (listener != null) {
                        SwingUtilities.invokeLater(() -> listener.onAlertReceived(alert));
                    }
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("-> [LỖI MULTICAST RECEIVER] " + e.getMessage());
                if (listener != null) {
                    SwingUtilities.invokeLater(() -> listener.onError(e.getMessage()));
                }
            }
        } finally {
            stopListening();
        }
    }

    /**
     * Bóc tách chuỗi dữ liệu "KEY=VALUE;" thành đối tượng Alert.
     */
    public static Alert parseRawMessageToAlert(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        String[] pairs = rawMessage.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        try {
            Alert alert = new Alert();
            alert.setAlertCode(map.getOrDefault("ALERT_CODE", "ALT-UNKNOWN"));
            alert.setAlertType(map.getOrDefault("TYPE", "OTHER"));
            alert.setSeverity(map.getOrDefault("SEVERITY", "HIGH"));
            alert.setLocation(map.getOrDefault("LOCATION", "Không xác định"));
            alert.setMessage(map.getOrDefault("MESSAGE", "Cảnh báo khẩn cấp!"));
            alert.setSenderUsername(map.getOrDefault("SENDER_USER", "system"));
            alert.setSenderIp(map.getOrDefault("SENDER_IP", "127.0.0.1"));

            if (map.containsKey("ALERT_ID")) {
                try {
                    alert.setId(Integer.parseInt(map.get("ALERT_ID")));
                } catch (NumberFormatException ignored) {
                }
            }

            alert.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            return alert;

        } catch (Exception e) {
            System.err.println("-> Lỗi parse raw message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lưu bản ghi xác nhận nhận cảnh báo vào bảng alert_receipts trong MySQL.
     */
    private void saveAlertReceiptToDB(Alert alert, String packetSenderIp) {
        new Thread(() -> {
            try {
                String receiverName = "Máy Trạm (" + InetAddress.getLocalHost().getHostName() + ")";
                String receiverIp = InetAddress.getLocalHost().getHostAddress();

                int alertId = alert.getId();
                if (alertId <= 0) {
                    alertId = 1; // ID mặc định nếu không parse được alert_id
                }

                AlertReceipt receipt = new AlertReceipt(alertId, receiverName, receiverIp, "RECEIVED");
                boolean saved = receiptDAO.insertReceipt(receipt);
                if (saved) {
                    System.out.println("-> [CSDL] Đã tự động lưu nhật ký nhận cảnh báo vào bảng alert_receipts.");
                }
            } catch (Exception e) {
                System.err.println("-> Lỗi tự động lưu nhật ký nhận: " + e.getMessage());
            }
        }).start();
    }
}
