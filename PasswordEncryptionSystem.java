import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class PasswordEncryptionSystem {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int PBKDF2_ITERATIONS = 210_000;

    // Demonstration storage only; replace with a database for persistent storage.
    private static final Map<String, UserRecord> USERS = new HashMap<>();
    private static final List<String> LOGIN_ACTIVITY = new ArrayList<>();

    private static class UserRecord {
        private final String username;
        private final String saltBase64;
        private final String passwordHashBase64;

        private UserRecord(String username, String saltBase64, String passwordHashBase64) {
            this.username = username;
            this.saltBase64 = saltBase64;
            this.passwordHashBase64 = passwordHashBase64;
        }
    }

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("       PASSWORD ENCRYPTION SYSTEM");
        System.out.println("======================================");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("3. Check password strength");
            System.out.println("4. Generate strong password");
            System.out.println("5. View login activity ");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            String choice = SCANNER.nextLine().trim();
            switch (choice) {
                case "1":
                    registerUser();
                    break;
                case "2":
                    loginUser();
                    break;
                case "3":
                    checkPasswordStrength();
                    break;
                case "4":
                    generatePassword();
                    break;
                case "5":
                    viewLoginActivity();
                    break;
                case "0":
                    running = false;
                    System.out.println("Thank you for using the system.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        SCANNER.close();
    }

    private static void registerUser() {
        System.out.println("\n--- User Registration ---");
        System.out.print("Create username: ");
        String username = SCANNER.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (USERS.containsKey(username.toLowerCase())) {
            System.out.println("That username is already registered.");
            return;
        }

        System.out.print("Create password: ");
        char[] password = SCANNER.nextLine().toCharArray();

        String strength = getPasswordStrength(password);
        System.out.println("Password strength: " + strength);

        if ("Weak".equals(strength)) {
            System.out.println("Please choose a stronger password (at least 8 characters,");
            System.out.println("including uppercase, lowercase, a number, and a symbol).");
            clear(password);
            return;
        }

        try {
            byte[] salt = new byte[SALT_BYTES];
            SECURE_RANDOM.nextBytes(salt);
            byte[] hash = hashPassword(password, salt);

            UserRecord record = new UserRecord(
                    username,
                    Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(hash));

            USERS.put(username.toLowerCase(), record);
            System.out.println("Registration successful. Your password hash was stored.");
        } catch (GeneralSecurityException e) {
            System.out.println("Could not securely process the password.");
        } finally {
            clear(password);
        }
    }

    private static void loginUser() {
        System.out.println("\n--- User Login ---");
        System.out.print("Username: ");
        String username = SCANNER.nextLine().trim();

        System.out.print("Password: ");
        char[] password = SCANNER.nextLine().toCharArray();

        UserRecord record = USERS.get(username.toLowerCase());
        boolean success = false;

        try {
            if (record != null) {
                byte[] salt = Base64.getDecoder().decode(record.saltBase64);
                byte[] enteredHash = hashPassword(password, salt);
                byte[] storedHash = Base64.getDecoder().decode(record.passwordHashBase64);
                success = constantTimeEquals(enteredHash, storedHash);
            }

            String status = success ? "SUCCESS" : "FAILED";
            LOGIN_ACTIVITY.add("Username: " + username + " | Status: " + status);
            System.out.println(success ? "Login successful." : "Login failed. Check your username and password.");
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            LOGIN_ACTIVITY.add("Username: " + username + " | Status: FAILED");
            System.out.println("Login failed.");
        } finally {
            clear(password);
        }
    }

    private static void checkPasswordStrength() {
        System.out.println("\n--- Password Strength Check ---");
        System.out.print("Enter a password to check: ");
        char[] password = SCANNER.nextLine().toCharArray();
        System.out.println("Password strength: " + getPasswordStrength(password));
        clear(password);
    }

    private static String getPasswordStrength(char[] password) {
        if (password.length < 8) {
            return "Weak";
        }

        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;

        for (char c : password) {
            if (Character.isUpperCase(c)) upper = true;
            else if (Character.isLowerCase(c)) lower = true;
            else if (Character.isDigit(c)) digit = true;
            else symbol = true;
        }

        int score = 0;
        if (password.length >= 8) score++;
        if (password.length >= 12) score++;
        if (upper) score++;
        if (lower) score++;
        if (digit) score++;
        if (symbol) score++;

        if (score >= 5) return "Strong";
        if (score >= 3) return "Moderate";
        return "Weak";
    }

    private static void generatePassword() {
        System.out.println("\n--- Strong Password Generator ---");
        final String upper = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        final String lower = "abcdefghijkmnopqrstuvwxyz";
        final String digits = "23456789";
        final String symbols = "!@#$%&*?";
        final String all = upper + lower + digits + symbols;

        char[] password = new char[16];
        password[0] = upper.charAt(SECURE_RANDOM.nextInt(upper.length()));
        password[1] = lower.charAt(SECURE_RANDOM.nextInt(lower.length()));
        password[2] = digits.charAt(SECURE_RANDOM.nextInt(digits.length()));
        password[3] = symbols.charAt(SECURE_RANDOM.nextInt(symbols.length()));

        for (int i = 4; i < password.length; i++) {
            password[i] = all.charAt(SECURE_RANDOM.nextInt(all.length()));
        }

        // Securely shuffle the generated characters.
        for (int i = password.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        System.out.println("Generated password: " + new String(password));
        System.out.println("Copy it to a safe place. Do not share it.");
        clear(password);
    }

    private static void viewLoginActivity() {
        System.out.println("\n--- Login Activity (current session only) ---");
        if (LOGIN_ACTIVITY.isEmpty()) {
            System.out.println("No login attempts recorded yet.");
            return;
        }
        for (String activity : LOGIN_ACTIVITY) {
            System.out.println(activity);
        }
    }

    private static byte[] hashPassword(char[] password, byte[] salt)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, HASH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean constantTimeEquals(byte[] first, byte[] second) {
        if (first == null || second == null) return false;
        int difference = first.length ^ second.length;
        int length = Math.min(first.length, second.length);
        for (int i = 0; i < length; i++) {
            difference |= first[i] ^ second[i];
        }
        return difference == 0;
    }

    private static void clear(char[] value) {
        if (value != null) {
            java.util.Arrays.fill(value, '\0');
        }
    }
}
