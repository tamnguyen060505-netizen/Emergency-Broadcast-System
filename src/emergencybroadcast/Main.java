package emergencybroadcast;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main Application Launcher for Emergency Broadcast System.
 * System: Java Swing + UDP Multicast + MySQL (XAMPP)
 */
public class Main {

    public static void main(String[] args) {
        // Cấu hình Look and Feel hiển thị giao diện chuẩn của hệ điều hành OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Không thể thiết lập System Look and Feel: " + e.getMessage());
        }

        // Đẩy khởi tạo giao diện Swing LoginFrame lên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }
}
