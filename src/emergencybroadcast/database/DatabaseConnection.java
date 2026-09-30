package emergencybroadcast.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối Cơ sở dữ liệu MySQL trên XAMPP.
 * Database: emergency_broadcast
 * Host: localhost | Port: 3306 | User: root | Password: ""
 */
public class DatabaseConnection {

    // Cấu hình tham số kết nối mặc định
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB_NAME = "emergency_broadcast";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Đặt mật khẩu nếu XAMPP có mật khẩu

    // URL kết nối JDBC với tham số mã hóa UTF-8 và múi giờ
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME 
            + "?useUnicode=true&characterEncoding=UTF-8"
            + "&useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Ho_Chi_Minh";

    // Driver MySQL Connector/J 8.x
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            // Nạp Driver MySQL JDBC
            Class.forName(DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            System.err.println("LỖI: Không tìm thấy MySQL Driver! " + e.getMessage());
        }
    }

    /**
     * Phương thức lấy kết nối (Connection) đến MySQL Database.
     * @return java.sql.Connection đối tượng kết nối
     * @throws SQLException nếu xảy ra lỗi kết nối
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Hàm kiểm tra nhanh trạng thái kết nối đến MySQL.
     * @return true nếu kết nối thành công, false nếu thất bại
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("-> [THÀNH CÔNG] Kết nối Cơ sở dữ liệu MySQL thành công!");
                System.out.println("   Database: " + DB_NAME + " @ " + HOST + ":" + PORT);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("-> [THẤT BẠI] Không thể kết nối Cơ sở dữ liệu MySQL!");
            System.err.println("   Mã lỗi SQL: " + e.getErrorCode());
            System.err.println("   Chi tiết: " + e.getMessage());
        }
        return false;
    }

    /**
     * Phương thức main cho phép chạy trực tiếp để kiểm thử kết nối trong NetBeans.
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  KIỂM TRA KẾT NỐI DATABASE MYSQL (XAMPP)");
        System.out.println("==================================================");
        testConnection();
    }
}
