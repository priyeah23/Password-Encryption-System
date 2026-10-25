CREATE DATABASE IF NOT EXISTS password_system
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE password_system;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS passwords (
    password_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_passwords_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS login_activity (
    activity_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    username_attempted VARCHAR(60) NOT NULL,
    action VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    details VARCHAR(255),
    activity_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activity_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_activity_username_time (username_attempted, activity_time)
);
