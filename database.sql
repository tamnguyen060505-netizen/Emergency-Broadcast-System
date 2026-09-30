-- ====================================================================
-- DỰ ÁN: HỆ THỐNG THÔNG BÁO KHẨN CẤP (EMERGENCY BROADCAST SYSTEM)
-- TÁC GIẢ: JAVA SWING + UDP MULTICAST + MYSQL DEMO
-- ====================================================================

-- 1. TẠO DATABASE
CREATE DATABASE IF NOT EXISTS emergency_broadcast 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE emergency_broadcast;

-- 2. TẠO BẢNG users (Quản lý tài khoản & phân quyền)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'RECEIVER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. TẠO BẢNG alerts (Lịch sử các cảnh báo được phát đi)
CREATE TABLE IF NOT EXISTS alerts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_code VARCHAR(50) NOT NULL UNIQUE,
    alert_type VARCHAR(30) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    sender_ip VARCHAR(45) NOT NULL,
    sender_username VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. TẠO BẢNG alert_receipts (Nhật ký xác nhận nhận cảnh báo từ các máy trạm)
CREATE TABLE IF NOT EXISTS alert_receipts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    alert_id INT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_ip VARCHAR(45) NOT NULL,
    received_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) DEFAULT 'RECEIVED',
    CONSTRAINT fk_receipt_alert FOREIGN KEY (alert_id) 
        REFERENCES alerts(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. THÊM DỮ LIỆU MẪU BAN ĐẦU (SEED DATA)
INSERT INTO users (username, password, full_name, role) VALUES 
('admin', 'admin123', 'Quản Trị Viên Hệ Thống', 'ADMIN'),
('operator', 'op123', 'Nhân Viên Bảo Vệ Direct', 'OPERATOR'),
('receiver1', 'rec123', 'Máy Trạm Phòng 101', 'RECEIVER');

-- Thêm 1 cảnh báo mẫu thử nghiệm
INSERT INTO alerts (alert_code, alert_type, severity, location, message, sender_ip, sender_username) VALUES 
('ALT-20260922-100000-001', 'FIRE', 'CRITICAL', 'Tầng 3 - Phòng 302', 'Phát hiện khói lớn tại khu vực phòng máy. Vui lòng di chuyển theo cầu thang bộ!', '127.0.0.1', 'admin');

-- Thêm 1 xác nhận nhận mẫu
INSERT INTO alert_receipts (alert_id, receiver_name, receiver_ip, status) VALUES 
(1, 'Máy Trạm Phòng 101', '127.0.0.1', 'RECEIVED');
