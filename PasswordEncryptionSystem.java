import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class PasswordEncryptionSystemGUI extends JFrame {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/password_system?useSSL=false&serverTimezone=UTC";
    private static final String DB_URL = envOrDefault("DB_URL", DEFAULT_URL);
    private static final String DB_USER = envOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = envOrDefault("DB_PASSWORD", "");

    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JTextField usernameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextArea outputArea = new JTextArea(9, 42);

    public PasswordEncryptionSystemGUI() {
        super("Password Encryption System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 520);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        JLabel title = new JLabel("Password Encryption System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(form, c, 0, "Username:", usernameField);
        addFormRow(form, c, 1, "Email:", emailField);
        addFormRow(form, c, 2, "Password:", passwordField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");
        JButton strengthButton = new JButton("Check Strength");
        JButton generateButton = new JButton("Generate Password");
        JButton activityButton = new JButton("View Login Activity");
        buttons.add(registerButton);
        buttons.add(loginButton);
        buttons.add(strengthButton);
        buttons.add(generateButton);
        buttons.add(activityButton);

        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(form, BorderLayout.NORTH);
        center.add(buttons, BorderLayout.CENTER);
        center.add(outputScroll, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        registerButton.addActionListener(e -> register());
        loginButton.addActionListener(e -> login());
        strengthButton.addActionListener(e ->
                outputArea.setText("Password strength: " + passwordStrength(new String(passwordField.getPassword()))));
        generateButton.addActionListener(e -> {
            String generated = generatePassword(14);
            passwordField.setText(generated);
            outputArea.setText("Generated password:\n" + generated
                    + "\nCopy it to a safe password manager. Do not reuse it.");
        });
        activityButton.addActionListener(e -> viewActivity());

        outputArea.setText("Connects to MySQL database: password_system\n"
                + "Create the database using database.sql, then configure MySQL Connector/J.");
    }

    private static void addFormRow(JPanel panel, GridBagConstraints c, int row,
                                   String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(field, c);
    }

    private static String envOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username, email, and password.");
            return;
        }
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            showMessage("Please enter a valid email address.");
            return;
        }
        if (password.length() < 8) {
            showMessage("Use a password with at least 8 characters.");
            return;
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        String hash;
        try {
            hash = hashPassword(passwordChars, salt);
        } catch (Exception ex) {
            showMessage("Could not hash password: " + ex.getMessage());
            return;
        }

        String sql = "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, saltText);
            ps.executeUpdate();

            long userId = -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) userId = keys.getLong(1);
            }
            if (userId > 0) {
                savePasswordRecord(con, userId, hash, saltText);
                logActivity(con, userId, username, "SUCCESS", "Registration");
            }
            showMessage("Registration successful!\nPassword hash stored in MySQL.\nStrength: "
                    + passwordStrength(password));
        } catch (SQLIntegrityConstraintViolationException ex) {
            showMessage("That username or email is already registered.");
        } catch (SQLException ex) {
            showDatabaseError(ex);
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private void login() {
        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        if (username.isEmpty() || passwordChars.length == 0) {
            showMessage("Enter username and password.");
            return;
        }

        String sql = "SELECT user_id, password_hash, salt FROM users WHERE username = ?";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            long userId = -1;
            String storedHash = null;
            String saltText = null;
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getLong("user_id");
                    storedHash = rs.getString("password_hash");
                    saltText = rs.getString("salt");
                }
            }

            boolean success = false;
            if (storedHash != null && saltText != null) {
                String calculated = hashPassword(passwordChars,
                        Base64.getDecoder().decode(saltText));
                success = constantTimeEquals(storedHash, calculated);
            }
            logActivity(con, success ? userId : (userId > 0 ? userId : null),
                    username, success ? "SUCCESS" : "FAILED", "Login");
            showMessage(success ? "Login successful. Welcome, " + username + "!"
                    : "Login failed. Check your username and password.");
        } catch (SQLException ex) {
            showDatabaseError(ex);
        } catch (Exception ex) {
            showMessage("Password verification failed: " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private void viewActivity() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showMessage("Enter a username to view its login activity.");
            return;
        }

        String sql = "SELECT activity_time, action, status, details "
                + "FROM login_activity WHERE username_attempted = ? "
                + "ORDER BY activity_time DESC LIMIT 30";
        StringBuilder result = new StringBuilder("Recent activity for ")
                .append(username).append(":\n\n");
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    result.append(rs.getTimestamp("activity_time")).append(" | ")
                            .append(rs.getString("action")).append(" | ")
                            .append(rs.getString("status")).append(" | ")
                            .append(rs.getString("details")).append('\n');
                }
                if (!found) result.append("No activity records found.");
            }
            outputArea.setText(result.toString());
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private static void savePasswordRecord(Connection con, long userId,
                                           String hash, String salt) throws SQLException {
        String sql = "INSERT INTO passwords (user_id, password_hash, salt) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.executeUpdate();
        }
    }

    private static void logActivity(Connection con, Long userId, String username,
                                    String status, String action) throws SQLException {
        String sql = "INSERT INTO login_activity "
                + "(user_id, username_attempted, action, status, details) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (userId == null) ps.setNull(1, Types.BIGINT);
            else ps.setLong(1, userId);
            ps.setString(2, username);
            ps.setString(3, action);
            ps.setString(4, status);
            ps.setString(5, action.equals("Registration") ? "Account created" : "User login attempt");
            ps.executeUpdate();
        }
    }

    private static String hashPassword(String password, byte[] salt) throws Exception {
        return hashPassword(password.toCharArray(), salt);
    }

    private static String hashPassword(char[] password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] y = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(x, y);
    }

    private static String passwordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;
        if (score <= 2) return "Weak";
        if (score <= 4) return "Medium";
        return "Strong";
    }

    private static String generatePassword(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*?";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return result.toString();
    }

    private void showMessage(String message) {
        outputArea.setText(message);
        JOptionPane.showMessageDialog(this, message, "Password Encryption System",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDatabaseError(SQLException ex) {
        outputArea.setText("Database error: " + ex.getMessage()
                + "\nCheck that MySQL is running, database.sql was executed, and Connector/J is configured.");
        JOptionPane.showMessageDialog(this,
                "Could not complete the database operation.\n"
                        + "Check MySQL, database.sql, Connector/J, and DB settings.\n\n"
                        + ex.getMessage(),
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PasswordEncryptionSystemGUI().setVisible(true));
    }
}
