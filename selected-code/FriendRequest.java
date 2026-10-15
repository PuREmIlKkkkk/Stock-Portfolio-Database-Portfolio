// Selected excerpt from a two-person database application project.
// This file contains the friend request and friendship management module I implemented.
// Original implementation logic is preserved; package declaration omitted for display.

import java.sql.*;

public class FriendRequest {
    private static final String REQUEST_TABLE_NAME = "Requested";
    private static final String FRIENDS_TABLE_NAME = "Friends";

    private User userService;
    private Connection conn;

    public FriendRequest(Connection conn, User userService) {
        this.conn = conn;
        this.userService = userService;
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        String sqlCreateRequestedTable =
                "CREATE TABLE IF NOT EXISTS " + REQUEST_TABLE_NAME + " ("
                + "user1 INTEGER NOT NULL REFERENCES Users(userID) ON DELETE CASCADE, "
                + "user2 INTEGER NOT NULL REFERENCES Users(userID) ON DELETE CASCADE, "
                + "PRIMARY KEY (user1, user2)"
                + ");";

        String sqlCreateFriendsTable =
                "CREATE TABLE IF NOT EXISTS " + FRIENDS_TABLE_NAME + " ("
                + "user1 INTEGER NOT NULL REFERENCES Users(userID) ON DELETE CASCADE, "
                + "user2 INTEGER NOT NULL REFERENCES Users(userID) ON DELETE CASCADE, "
                + "PRIMARY KEY (user1, user2)"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlCreateRequestedTable);
            stmt.executeUpdate(sqlCreateFriendsTable);
        } catch (SQLException e) {
            System.err.println("Error creating friend tables: " + e.getMessage());
        }
    }

    public boolean sendRequest(int fromUserID, String toUsername) {
        try {
            String sqlFindUser = "SELECT userID FROM Users WHERE username = ?";

            try (PreparedStatement findStmt = conn.prepareStatement(sqlFindUser)) {
                findStmt.setString(1, toUsername);
                ResultSet rs = findStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("User " + toUsername + " does not exists.");
                    return false;
                }

                int toUserID = rs.getInt("userID");

                if (toUserID == fromUserID) {
                    System.out.println("You cannot send a friend request to yourself.");
                    return false;
                }

                if (areAlreadyFriends(fromUserID, toUserID)) {
                    System.out.println("You are already friends with " + toUsername + ".");
                    return false;
                }

                if (requestExists(fromUserID, toUserID)) {
                    String toUserName = userService.getUsernameById(toUserID);
                    System.out.println(
                            "A friend request already exists between you and "
                            + toUserName + "."
                    );
                    return false;
                }

                String sqlInsert =
                        "INSERT INTO " + REQUEST_TABLE_NAME
                        + " (user1, user2) VALUES (?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                    insertStmt.setInt(1, fromUserID);
                    insertStmt.setInt(2, toUserID);
                    insertStmt.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error sending friend request: " + e.getMessage());
            return false;
        }
    }

    public void listIncomingRequests(int currentUserID) {
        String sqlList =
                "SELECT r.user1, u.username "
                + "FROM " + REQUEST_TABLE_NAME + " r "
                + "JOIN Users u ON r.user1 = u.userID "
                + "WHERE r.user2 = ?;";

        try (PreparedStatement stmt = conn.prepareStatement(sqlList)) {
            stmt.setInt(1, currentUserID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int fromID = rs.getInt("user1");
                String fromName = rs.getString("username");
                System.out.printf(
                        "From userID=%d, username=%s%n",
                        fromID,
                        fromName
                );
            }
        } catch (SQLException e) {
            System.err.println("Error listing incoming requests: " + e.getMessage());
        }
    }

    public boolean acceptRequest(int currentUserID, int fromUserID) {
        try {
            String sqlCheck =
                    "SELECT 1 FROM " + REQUEST_TABLE_NAME
                    + " WHERE user1 = ? AND user2 = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
                checkStmt.setInt(1, fromUserID);
                checkStmt.setInt(2, currentUserID);

                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    return false;
                }
            }

            int a = Math.min(currentUserID, fromUserID);
            int b = Math.max(currentUserID, fromUserID);

            String sqlInsertFriend =
                    "INSERT INTO " + FRIENDS_TABLE_NAME
                    + " (user1, user2) VALUES (?, ?) "
                    + "ON CONFLICT DO NOTHING;";

            try (PreparedStatement fStmt = conn.prepareStatement(sqlInsertFriend)) {
                fStmt.setInt(1, a);
                fStmt.setInt(2, b);
                fStmt.executeUpdate();
            }

            String sqlDeleteReq =
                    "DELETE FROM " + REQUEST_TABLE_NAME
                    + " WHERE user1 = ? AND user2 = ?";

            try (PreparedStatement deleteStmt = conn.prepareStatement(sqlDeleteReq)) {
                deleteStmt.setInt(1, fromUserID);
                deleteStmt.setInt(2, currentUserID);
                deleteStmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error accepting request: " + e.getMessage());
            return false;
        }
    }

    public void listFriends(int currentUserID) {
        String sql =
                "SELECT u.userID, u.username "
                + "FROM " + FRIENDS_TABLE_NAME + " f "
                + "JOIN Users u ON (u.userID = f.user1 OR u.userID = f.user2) "
                + "WHERE (? IN (f.user1, f.user2)) AND u.userID <> ?;";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserID);
            stmt.setInt(2, currentUserID);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int friendID = rs.getInt("userID");
                String username = rs.getString("username");

                System.out.printf(
                        "friendID=%d, username=%s%n",
                        friendID,
                        username
                );
            }
        } catch (SQLException e) {
            System.err.println("Error listing friends: " + e.getMessage());
        }
    }

    public void deleteFriend(int currentUserId, int friendUserId) {
        int u1 = Math.min(currentUserId, friendUserId);
        int u2 = Math.max(currentUserId, friendUserId);

        String sqlDeleteFriend =
                "DELETE FROM " + FRIENDS_TABLE_NAME
                + " WHERE user1 = ? AND user2 = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteFriend)) {
            stmt.setInt(1, u1);
            stmt.setInt(2, u2);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting friend: " + e.getMessage());
        }
    }

    private boolean areAlreadyFriends(int u1, int u2) throws SQLException {
        int a = Math.min(u1, u2);
        int b = Math.max(u1, u2);

        String sql =
                "SELECT 1 FROM " + FRIENDS_TABLE_NAME
                + " WHERE user1 = ? AND user2 = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a);
            stmt.setInt(2, b);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private boolean requestExists(int u1, int u2) throws SQLException {
        String sql =
                "SELECT 1 FROM " + REQUEST_TABLE_NAME
                + " WHERE (user1 = ? AND user2 = ?) "
                + "OR (user1 = ? AND user2 = ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, u1);
            stmt.setInt(2, u2);
            stmt.setInt(3, u2);
            stmt.setInt(4, u1);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}
