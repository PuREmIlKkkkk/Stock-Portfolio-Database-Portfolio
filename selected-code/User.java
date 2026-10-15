// Selected excerpt from a two-person database application project.
// This file contains the user registration and authentication module I implemented.
// Original implementation logic is preserved; package declaration omitted for display.

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class User {
    private static final String TABLE_NAME = "Users";
    public int userID;
    public String username;
    private Connection conn;

    public User(Connection conn) {
        this.conn = conn;
        createTableIfNotExists();
    }

    // Creates the Users table if it doesn't exist
    private void createTableIfNotExists() {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                                + "userID SERIAL PRIMARY KEY, "
                                + "username VARCHAR(100) UNIQUE NOT NULL, "
                                + "hashedPassword VARCHAR(100) NOT NULL"
                                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlCreateTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean register(String username, String hashedPassword) {
        String sqlInsert = "INSERT INTO " + TABLE_NAME + " (username, hashedPassword) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            System.out.println("User registered successfully.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        String sqlSelect = "SELECT * FROM " + TABLE_NAME + " WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("hashedPassword");

                if (BCrypt.checkpw(password, storedHash)) {
                    System.out.println("Authentication successful.");
                    this.username = rs.getString("username");
                    this.userID = rs.getInt("userID");
                    return true;
                } else {
                    System.out.println("Invalid credentials.");
                    return false;
                }
            } else {
                System.out.println("User not found");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameById(int userID) {
        String sql = "SELECT username FROM " + TABLE_NAME + " WHERE userID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            System.err.println("Error getting username: " + e.getMessage());
        }
        return null;
    }

    public int getUserId(String username) {
        int userID = -1;
        String sqlSelect = "SELECT * FROM " + TABLE_NAME + " WHERE username = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userID = rs.getInt("userid");
            }

            return userID;
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return -1;
        }
    }
}
