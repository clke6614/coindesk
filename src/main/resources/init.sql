CREATE TABLE IF NOT EXISTS Currency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(3),
    nameZh VARCHAR(50),
    createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);