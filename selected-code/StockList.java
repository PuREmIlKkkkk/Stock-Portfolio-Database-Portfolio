// Selected excerpt from a two-person database application project.
// This file contains the stock-list management and access-control module I implemented.
// Original implementation logic is preserved; package declaration omitted for display.

import java.sql.*;

public class StockList {
    private static final String LIST_TABLE_NAME = "StockLists";
    private static final String ITEM_TABLE_NAME = "StockListItems";

    private final Connection conn;

    public StockList(Connection conn) {
        this.conn = conn;
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        String sqlCreateList =
            "CREATE TABLE IF NOT EXISTS " + LIST_TABLE_NAME + " (" +
            "  listID SERIAL PRIMARY KEY, " +
            "  listName VARCHAR(100) NOT NULL, " +
            "  ownerID INT NOT NULL REFERENCES Users(userID) ON DELETE CASCADE, " +
            "  visibility VARCHAR(20) NOT NULL " +
            "      CHECK (visibility IN ('PRIVATE','FRIENDS','PUBLIC'))" +
            ");";

        String sqlCreateItems =
            "CREATE TABLE IF NOT EXISTS " + ITEM_TABLE_NAME + " (" +
            "  listID INT NOT NULL REFERENCES " + LIST_TABLE_NAME +
            "(listID) ON DELETE CASCADE, " +
            "  symbol VARCHAR(10) NOT NULL, " +
            "  shares INT NOT NULL, " +
            "  PRIMARY KEY (listID, symbol) " +
            ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlCreateList);
            stmt.executeUpdate(sqlCreateItems);
        } catch (SQLException e) {
            System.err.println(
                "Error creating stock list tables: " + e.getMessage()
            );
        }
    }

    public int createStockList(
        int ownerID,
        String listName,
        String visibility
    ) {
        String sql =
            "INSERT INTO " + LIST_TABLE_NAME +
            " (listName, ownerID, visibility) VALUES (?, ?, ?)";

        try (
            PreparedStatement ps =
                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, listName);
            ps.setInt(2, ownerID);
            ps.setString(3, visibility);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int listID = rs.getInt(1);
                    System.out.printf(
                        "Created stock list [%d] %s (%s)%n",
                        listID,
                        listName,
                        visibility
                    );
                    return listID;
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error creating stock list: " + e.getMessage()
            );
        }

        return -1;
    }

    public void addStockToListAsOwner(
        int currentUserID,
        int listID,
        String symbol,
        int shares
    ) {
        if (!isOwner(listID, currentUserID)) {
            return;
        }

        String sql =
            "INSERT INTO " + ITEM_TABLE_NAME +
            " (listID, symbol, shares) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT (listID, symbol) " +
            "DO UPDATE SET shares = EXCLUDED.shares;";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listID);
            ps.setString(2, symbol);
            ps.setInt(3, shares);
            ps.executeUpdate();

            System.out.printf(
                "Added/updated %s (%d shares) in list %d%n",
                symbol,
                shares,
                listID
            );
        } catch (SQLException e) {
            System.err.println(
                "Error adding stock to list: " + e.getMessage()
            );
        }
    }

    public void listAccessibleLists(int currentUserID) {
        String sql =
            "SELECT sl.listID, sl.listName, sl.visibility, " +
            "       u.username AS ownerName " +
            "FROM " + LIST_TABLE_NAME + " sl " +
            "JOIN Users u ON sl.ownerID = u.userID " +
            "WHERE sl.ownerID = ? " +
            "   OR sl.visibility = 'PUBLIC' " +
            "   OR (sl.visibility = 'FRIENDS' AND EXISTS ( " +
            "        SELECT 1 FROM Friends f " +
            "        WHERE (f.user1 = sl.ownerID AND f.user2 = ?) " +
            "           OR (f.user2 = sl.ownerID AND f.user1 = ?) " +
            "   )) " +
            "ORDER BY sl.listID;";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserID);
            ps.setInt(2, currentUserID);
            ps.setInt(3, currentUserID);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("ID\tName\tOwner\tVisibility");

                while (rs.next()) {
                    int id = rs.getInt("listID");
                    String name = rs.getString("listName");
                    String owner = rs.getString("ownerName");
                    String visibility = rs.getString("visibility");

                    System.out.printf(
                        "%d\t%s\t%s\t%s%n",
                        id,
                        name,
                        owner,
                        visibility
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error listing stock lists: " + e.getMessage()
            );
        }
    }

    public void showListContents(int listID) {
        String sql =
            "SELECT symbol, shares " +
            "FROM " + ITEM_TABLE_NAME +
            " WHERE listID = ? " +
            "ORDER BY symbol;";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listID);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Symbol\tShares");

                while (rs.next()) {
                    System.out.printf(
                        "%s\t%d%n",
                        rs.getString("symbol"),
                        rs.getInt("shares")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "Error showing stock list contents: " + e.getMessage()
            );
        }
    }

    public void removeStockFromListAsOwner(
        int currentUserID,
        int listID,
        String symbol
    ) {
        if (!isOwner(listID, currentUserID)) {
            return;
        }

        String sql =
            "DELETE FROM " + ITEM_TABLE_NAME +
            " WHERE listID = ? AND symbol = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listID);
            ps.setString(2, symbol);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.printf(
                    "Removed %s from list %d%n",
                    symbol,
                    listID
                );
            } else {
                System.out.println(
                    "That stock was not found in this list."
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error removing stock from list: " + e.getMessage()
            );
        }
    }

    public void deleteStockListAsOwner(
        int currentUserID,
        int listID
    ) {
        if (!isOwner(listID, currentUserID)) {
            return;
        }

        String sql =
            "DELETE FROM " + LIST_TABLE_NAME +
            " WHERE listID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listID);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(
                    "Stocklist " + listID +
                    " deleted successfully (items removed via CASCADE)."
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error deleting stocklist: " + e.getMessage()
            );
        }
    }

    private boolean isOwner(
        int listID,
        int currentUserID
    ) {
        String sql =
            "SELECT ownerID, listName FROM " +
            LIST_TABLE_NAME +
            " WHERE listID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, listID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(
                        "No stocklist found with ID " + listID
                    );
                    return false;
                }

                int ownerID = rs.getInt("ownerID");
                String listName = rs.getString("listName");

                if (ownerID != currentUserID) {
                    System.out.println(
                        "You are not the owner of list [" +
                        listID + "] " + listName
                    );
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.err.println(
                "Error checking owner: " + e.getMessage()
            );
            return false;
        }
    }
}
