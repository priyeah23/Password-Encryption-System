# Password Encryption System

**Project tagline:** Privacy by Design, Security by Purpose.

## About the Project

The Password Encryption System is a Java console application designed to demonstrate password security and basic user authentication. It uses password hashing to protect stored passwords and provides tools for checking password strength and generating strong passwords.

## Technologies Used

- Java
- Java Cryptography Architecture
- PBKDF2WithHmacSHA256
- SecureRandom

## Features

1. **User Registration:** Registers users after checking password strength.
2. **User Login:** Verifies a user's password against its stored hash.
3. **Password Strength Checker:** Classifies passwords as Weak, Moderate, or Strong.
4. **Password Generator:** Generates a random password.
5. **Login Activity:** Records successful and failed login attempts during the current program session.
6. **Password Hashing:** Uses a random salt and PBKDF2WithHmacSHA256 to derive a password hash.

## Requirements

- Java Development Kit (JDK)
- A command prompt or terminal

## How to Run

1. Download `PasswordEncryptionSystem.java`.
2. Open a terminal in the folder containing the file.
3. Compile the program:

   `javac PasswordEncryptionSystem.java`

4. Run the program:

   `java PasswordEncryptionSystem`

## Important Notes

- User accounts and login activity are stored in memory and are lost when the program closes.
- MySQL and JDBC integration are not implemented in this version.
- Passwords are hashed rather than stored as plain text.

## Future Enhancements

- MySQL database integration using JDBC.
- Graphical user interface.
- Persistent login activity records.
- Additional authentication and security features.

## Project Contributors

- Gopika S.
- Aparna G.
- Priyadharshini N.
- Kaviya S.
- Anukeerthana A.

