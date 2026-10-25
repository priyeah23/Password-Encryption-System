# Password Encryption System — Java Swing + MySQL JDBC

## Features
- Swing GUI for registration and login
- Password strength checker
- Random password generator
- Password hashing with PBKDF2-HMAC-SHA256, a unique random salt, and 210,000 iterations
- MySQL storage for users, password hash records, and login activity
- Login activity viewer

**Important:** Passwords are not stored as plaintext. This is a student/demo project, not a production-ready authentication service.

## Requirements
- JDK 11 or later
- MySQL Server
- MySQL Connector/J (JDBC driver)

## 1. Create the database
Open MySQL Workbench or the MySQL command-line client and execute `database.sql`.

## 2. Configure database credentials
Defaults in the Java file are:
- URL: `jdbc:mysql://localhost:3306/password_system?useSSL=false&serverTimezone=UTC`
- Username: `root`
- Password: empty

For a local setup, edit the constants near the top of `PasswordEncryptionSystemGUI.java`, or set environment variables:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`

Do not commit real database passwords to a public repository.

## 3. Add MySQL Connector/J
Download MySQL Connector/J from the official MySQL website and note the JAR path, for example `mysql-connector-j-8.x.x.jar`.

## 4. Compile and run

### Windows (Command Prompt)
Run these commands in the folder containing the Java source and Connector/J JAR. Replace the JAR filename with your actual filename:

```bat
javac PasswordEncryptionSystemGUI.java
java -cp ".;mysql-connector-j-8.x.x.jar" PasswordEncryptionSystemGUI
```

### macOS / Linux
```bash
javac PasswordEncryptionSystemGUI.java
java -cp ".:mysql-connector-j-8.x.x.jar" PasswordEncryptionSystemGUI
```

The source uses only standard `java.sql` APIs, so `javac` does not need the Connector/J JAR. The driver JAR must be on the runtime classpath.

## Troubleshooting
- **No suitable driver:** Connector/J is missing from the runtime classpath or the JAR filename in the command is wrong.
- **Access denied:** Check `DB_USER` and `DB_PASSWORD`.
- **Unknown database:** Execute `database.sql` first.
- **Connection refused:** Start MySQL Server and check the port in `DB_URL`.

## Tables
- `users`: username, email, salted password hash
- `passwords`: stored password-hash record
- `login_activity`: registration/login activity

## Verification status
The Java source is intended for a local MySQL setup. A live connection cannot be confirmed unless the project is run against your own configured MySQL Server and Connector/J.
