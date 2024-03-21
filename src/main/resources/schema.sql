CREATE TABLE IF NOT EXISTS app_user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    usr_password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_request_rate (
    user_id INT NOT NULL,
    count INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES app_user(id)
);
