package emergencybroadcast.dao;

import emergencybroadcast.database.DatabaseConnection;
import emergencybroadcast.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) quản lý các thao tác dữ liệu với bảng 'users'.
 */
public class UserDAO {

    /**
     * Xác thực đăng nhập người dùng dựa trên Username và Password.
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Đối tượng User nếu đăng nhập thành công, null nếu sai thông tin
     */
    public User login(String username, String password) {
        String sql = "SELECT id, username, password, full_name, role, created_at FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi login(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm kiếm thông tin người dùng theo Username.
     * @param username Tên đăng nhập cần tìm
     * @return Đối tượng User hoặc null nếu không tìm thấy
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, full_name, role, created_at FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi findByUsername(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm mới một tài khoản người dùng vào CSDL.
     * @param user Đối tượng User cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi insertUser(): " + e.getMessage());
        }
        return false;
    }

    /**
     * Kiểm tra nhanh tài khoản hợp lệ.
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    public boolean checkAccount(String username, String password) {
        return login(username, password) != null;
    }

    /**
     * Lấy danh sách tất cả tài khoản trong hệ thống.
     * @return Danh sách các User
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password, full_name, role, created_at FROM users ORDER BY id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thực thi getAllUsers(): " + e.getMessage());
        }
        return list;
    }
}
