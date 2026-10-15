// Selected excerpt from a two-person database application project.
// This file contains the historical stock data loading and statistics module I implemented.
// Original implementation logic is preserved.

package cs.toronto.edu.utils;

import java.sql.*;
import java.io.*;

public class HistoricalDataLoader {
    private Connection conn;

    public HistoricalDataLoader(Connection conn) {
        this.conn = conn;
    }

    public int loadFromCSV(String filePath, String defaultSymbol) {
        int rowsInserted = 0;

        String sql =
                "INSERT INTO stocks " +
                "(timestamp, open, high, low, close, volume, symbol) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (symbol, timestamp) DO NOTHING";

        try (
            BufferedReader reader =
                new BufferedReader(new FileReader(filePath));
            PreparedStatement ps =
                conn.prepareStatement(sql)
        ) {
            String line;
            boolean isFirstLine = true;

            int batchSize = 1000;
            int batchCount = 0;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 6) {
                    continue;
                }

                try {
                    String timestamp = parts[0].trim();
                    float open =
                            Float.parseFloat(parts[1].trim());
                    float high =
                            Float.parseFloat(parts[2].trim());
                    float low =
                            Float.parseFloat(parts[3].trim());
                    float close =
                            Float.parseFloat(parts[4].trim());

                    int volume;

                    try {
                        volume =
                            (int) Double.parseDouble(
                                parts[5].trim()
                            );
                    } catch (NumberFormatException e) {
                        volume = 0;
                    }

                    String symbol =
                            parts.length > 6
                                ? parts[6].trim()
                                : defaultSymbol;

                    if (symbol == null || symbol.isEmpty()) {
                        symbol = defaultSymbol;
                    }

                    ps.setString(1, timestamp);
                    ps.setFloat(2, open);
                    ps.setFloat(3, high);
                    ps.setFloat(4, low);
                    ps.setFloat(5, close);
                    ps.setInt(6, volume);
                    ps.setString(7, symbol.toUpperCase());

                    ps.addBatch();

                    batchCount++;

                    if (batchCount >= batchSize) {
                        int[] results = ps.executeBatch();

                        for (int r : results) {
                            if (r > 0) {
                                rowsInserted++;
                            }
                        }

                        batchCount = 0;
                    }

                } catch (NumberFormatException e) {
                    System.err.println(
                        "Skipping malformed line: " + line
                    );
                }
            }

            if (batchCount > 0) {
                int[] results = ps.executeBatch();

                for (int r : results) {
                    if (r > 0) {
                        rowsInserted++;
                    }
                }
            }

            System.out.printf(
                "Loaded %d rows from %s%n",
                rowsInserted,
                filePath
            );

        } catch (IOException e) {
            System.err.println(
                "Error reading file: " + e.getMessage()
            );
        } catch (SQLException e) {
            System.err.println(
                "Database error: " + e.getMessage()
            );
        }

        return rowsInserted;
    }

    public int loadFromDirectory(String directoryPath) {
        int totalRows = 0;

        File dir = new File(directoryPath);

        if (!dir.isDirectory()) {
            System.err.println(
                directoryPath + " is not a directory."
            );
            return 0;
        }

        File[] files = dir.listFiles(
            (d, name) ->
                name.toLowerCase().endsWith(".csv")
        );

        if (files == null || files.length == 0) {
            System.out.println(
                "No CSV files found in " + directoryPath
            );
            return 0;
        }

        for (File file : files) {
            String symbol =
                file.getName()
                    .replaceFirst("\\.csv$", "")
                    .toUpperCase();

            totalRows += loadFromCSV(
                file.getAbsolutePath(),
                symbol
            );
        }

        System.out.printf(
            "Total rows loaded: %d%n",
            totalRows
        );

        return totalRows;
    }

    public void printStocksSummary() {
        String countSql =
                "SELECT COUNT(*) AS total, " +
                "COUNT(DISTINCT symbol) AS symbols " +
                "FROM stocks";

        String dateSql =
                "SELECT MIN(timestamp) AS earliest, " +
                "MAX(timestamp) AS latest " +
                "FROM stocks";

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs =
                stmt.executeQuery(countSql);

            if (rs.next()) {
                System.out.printf(
                    "Total records: %d | Unique symbols: %d%n",
                    rs.getInt("total"),
                    rs.getInt("symbols")
                );
            }

            rs = stmt.executeQuery(dateSql);

            if (rs.next()) {
                System.out.printf(
                    "Date range: %s to %s%n",
                    rs.getString("earliest"),
                    rs.getString("latest")
                );
            }

            String topSql =
                    "SELECT symbol, COUNT(*) AS cnt " +
                    "FROM stocks " +
                    "GROUP BY symbol " +
                    "ORDER BY cnt DESC " +
                    "LIMIT 10";

            rs = stmt.executeQuery(topSql);

            while (rs.next()) {
                System.out.printf(
                    "%s: %d records%n",
                    rs.getString("symbol"),
                    rs.getInt("cnt")
                );
            }

        } catch (SQLException e) {
            System.err.println(
                "Error getting summary: " + e.getMessage()
            );
        }
    }

    public void updateAllStatistics() {
        String symbolsSql =
                "SELECT DISTINCT symbol FROM stocks";

        try (
            Statement stmt = conn.createStatement();
            ResultSet rs =
                stmt.executeQuery(symbolsSql)
        ) {
            int count = 0;

            while (rs.next()) {
                String symbol =
                    rs.getString("symbol");

                updateStockStatistics(symbol);
                count++;

                if (count % 50 == 0) {
                    System.out.printf(
                        "Processed %d symbols...%n",
                        count
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println(
                "Error updating statistics: " +
                e.getMessage()
            );
        }
    }

    private void updateStockStatistics(
        String symbol
    ) {
        String cvSql =
                "UPDATE stocks SET cv = subq.cv " +
                "FROM (" +
                "SELECT STDDEV_SAMP(close) / " +
                "NULLIF(AVG(close), 0) AS cv " +
                "FROM stocks WHERE symbol = ?" +
                ") AS subq " +
                "WHERE stocks.symbol = ?";

        String betaSql =
                "UPDATE stocks SET beta = subq.beta " +
                "FROM (" +
                "SELECT COVAR_SAMP(" +
                "s.close, m.avg_close) / " +
                "NULLIF(VAR_SAMP(m.avg_close), 0) " +
                "AS beta " +
                "FROM stocks s " +
                "JOIN (" +
                "SELECT timestamp, " +
                "AVG(close) AS avg_close " +
                "FROM stocks " +
                "GROUP BY timestamp" +
                ") m " +
                "ON s.timestamp = m.timestamp " +
                "WHERE s.symbol = ?" +
                ") AS subq " +
                "WHERE stocks.symbol = ?";

        try (
            PreparedStatement cvPs =
                conn.prepareStatement(cvSql);
            PreparedStatement betaPs =
                conn.prepareStatement(betaSql)
        ) {
            cvPs.setString(1, symbol);
            cvPs.setString(2, symbol);
            cvPs.executeUpdate();

            betaPs.setString(1, symbol);
            betaPs.setString(2, symbol);
            betaPs.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                "Error updating statistics for " +
                symbol + ": " +
                e.getMessage()
            );
        }
    }
}
