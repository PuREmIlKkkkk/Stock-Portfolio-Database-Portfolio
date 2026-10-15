// Selected excerpt from a two-person database application project.
// This file contains the stock price prediction module I implemented.
// Original implementation logic is preserved.

package cs.toronto.edu.models;

import java.sql.*;
import java.util.*;

public class StockPrediction {
    private Connection conn;

    public StockPrediction(Connection conn) {
        this.conn = conn;
    }

    /**
     * Get historical close prices for a stock.
     */
    private List<double[]> getHistoricalPrices(String symbol, int days) {
        List<double[]> prices = new ArrayList<>();

        String sql =
                "SELECT timestamp, close FROM stocks " +
                "WHERE symbol = ? " +
                "ORDER BY timestamp DESC " +
                "LIMIT ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, symbol.toUpperCase());
            ps.setInt(2, days);

            ResultSet rs = ps.executeQuery();

            int index = 0;
            while (rs.next()) {
                prices.add(
                    new double[]{
                        index++,
                        rs.getDouble("close")
                    }
                );
            }
        } catch (SQLException e) {
            System.err.println(
                "Error fetching historical prices: " +
                e.getMessage()
            );
        }

        Collections.reverse(prices);
        return prices;
    }

    /**
     * Simple Moving Average prediction.
     */
    public double[] predictSMA(
        String symbol,
        int windowSize,
        int futureDays
    ) {
        List<double[]> historical =
            getHistoricalPrices(symbol, windowSize * 2);

        if (historical.size() < windowSize) {
            System.out.println(
                "Not enough historical data for prediction."
            );
            return new double[0];
        }

        double[] predictions =
            new double[futureDays];

        List<Double> recentPrices =
            new ArrayList<>();

        for (
            int i = Math.max(
                0,
                historical.size() - windowSize
            );
            i < historical.size();
            i++
        ) {
            recentPrices.add(
                historical.get(i)[1]
            );
        }

        for (int i = 0; i < futureDays; i++) {
            double sum = 0;

            for (double price : recentPrices) {
                sum += price;
            }

            double prediction =
                sum / recentPrices.size();

            predictions[i] = prediction;

            recentPrices.remove(0);
            recentPrices.add(prediction);
        }

        return predictions;
    }

    /**
     * Linear Regression prediction.
     */
    public double[] predictLinearRegression(
        String symbol,
        int historyDays,
        int futureDays
    ) {
        List<double[]> historical =
            getHistoricalPrices(symbol, historyDays);

        if (historical.size() < 2) {
            System.out.println(
                "Not enough historical data for prediction."
            );
            return new double[0];
        }

        int n = historical.size();

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = historical.get(i)[1];

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope =
            (n * sumXY - sumX * sumY)
            /
            (n * sumX2 - sumX * sumX);

        double intercept =
            (sumY - slope * sumX)
            / n;

        double[] predictions =
            new double[futureDays];

        for (int i = 0; i < futureDays; i++) {
            predictions[i] =
                intercept + slope * (n + i);
        }

        return predictions;
    }

    /**
     * Exponential Moving Average prediction.
     */
    public double[] predictEMA(
        String symbol,
        int windowSize,
        int futureDays
    ) {
        List<double[]> historical =
            getHistoricalPrices(symbol, windowSize * 2);

        if (historical.size() < windowSize) {
            System.out.println(
                "Not enough historical data for prediction."
            );
            return new double[0];
        }

        double multiplier =
            2.0 / (windowSize + 1);

        double ema = 0;

        for (
            int i = 0;
            i < windowSize
                && i < historical.size();
            i++
        ) {
            ema += historical.get(i)[1];
        }

        ema /= windowSize;

        for (
            int i = windowSize;
            i < historical.size();
            i++
        ) {
            ema =
                (historical.get(i)[1] - ema)
                * multiplier
                + ema;
        }

        double[] predictions =
            new double[futureDays];

        double lastPrice =
            historical
                .get(historical.size() - 1)[1];

        for (int i = 0; i < futureDays; i++) {
            predictions[i] =
                ema
                + (lastPrice - ema)
                * (1 - multiplier * i);

            if (predictions[i] < 0) {
                predictions[i] = ema;
            }
        }

        return predictions;
    }

    /**
     * Compare multiple prediction methods.
     */
    public void comparePredictionMethods(
        String symbol,
        int futureDays
    ) {
        double[] sma =
            predictSMA(
                symbol,
                10,
                futureDays
            );

        double[] ema =
            predictEMA(
                symbol,
                10,
                futureDays
            );

        double[] linear =
            predictLinearRegression(
                symbol,
                30,
                futureDays
            );

        if (
            sma.length == 0
            || ema.length == 0
            || linear.length == 0
        ) {
            System.out.println(
                "Insufficient data for comparison."
            );
            return;
        }

        System.out.printf(
            "%-10s %-15s %-15s %-15s%n",
            "Day",
            "SMA",
            "EMA",
            "Linear"
        );

        for (
            int i = 0;
            i < futureDays;
            i++
        ) {
            System.out.printf(
                "Day +%-5d $%-14.2f $%-14.2f $%-14.2f%n",
                i + 1,
                sma[i],
                ema[i],
                linear[i]
            );
        }

        System.out.println(
            "\n=== Method Comparison Summary ==="
        );

        System.out.printf(
            "%-15s %-15s %-15s%n",
            "Method",
            "Final Price",
            "Avg Price"
        );

        System.out.printf(
            "%-15s $%-14.2f $%-14.2f%n",
            "SMA",
            sma[futureDays - 1],
            average(sma)
        );

        System.out.printf(
            "%-15s $%-14.2f $%-14.2f%n",
            "EMA",
            ema[futureDays - 1],
            average(ema)
        );

        System.out.printf(
            "%-15s $%-14.2f $%-14.2f%n",
            "Linear",
            linear[futureDays - 1],
            average(linear)
        );
    }

    private double average(double[] arr) {
        double sum = 0;

        for (double v : arr) {
            sum += v;
        }

        return sum / arr.length;
    }
}
