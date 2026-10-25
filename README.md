# 🔐 Password Encryption System

**Privacy by Design. Security by Purpose.**

## Project Overview

The Password Encryption System is a Java-based desktop application developed to demonstrate secure password management and user authentication. It uses Java Swing to provide a graphical user interface and MySQL to store user-related information. JDBC is used to establish communication between the Java application and the MySQL database.

## Features

- **User Registration:** Allows new users to create accounts by providing the required details.
- **Secure Login:** Authenticates users using their registered credentials.
- **Password Strength Checker:** Evaluates password strength based on its characteristics.
- **Random Password Generator:** Generates random passwords to help users create stronger passwords.
- **Password Hashing:** Uses PBKDF2-HMAC-SHA256 with a unique salt to derive password hashes instead of storing passwords as plain text.
- **MySQL Database Integration:** Uses JDBC to connect the Java application with the database.
- **Login Activity Tracking:** Maintains records of user registration and login activity.

## Technologies Used

- **Java (JDK 11 or later):** Core programming language used to develop the application.
- **Java Swing:** Used to create the graphical user interface.
- **MySQL:** Used for database storage and management.
- **JDBC (MySQL Connector/J):** Enables communication between Java and MySQL.
- **PBKDF2-HMAC-SHA256:** Used for password hashing and secure password verification.

## System Requirements

- JDK 11 or later
- MySQL Server
- MySQL Connector/J JDBC driver
- A Java development environment such as Eclipse, IntelliJ IDEA, or NetBeans

## Database

The application is designed to use MySQL tables for storing user details, password-related records, and login activity. Database configuration and JDBC connectivity are required for database operations.

## Project Objective

The main objective of this project is to understand password security, user authentication, password hashing, Java GUI development, and database connectivity. It demonstrates how different technologies can be combined to build a password management application.

## Future Enhancements

- Password reset and recovery functionality
- Email verification during registration
- Improved login security and input validation
- Enhanced graphical user interface
- Additional security testing and error handling

## Project Status

**Academic Project**

This project is developed for educational purposes to demonstrate Java programming, password security concepts, and MySQL database integration. Successful execution and database connectivity depend on the local development environment and configuration.


