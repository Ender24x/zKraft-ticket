package dev.ender.etoken;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private Connection connection;
    private final EToken plugin;

    public DatabaseManager(EToken plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            // Verifica e crea la cartella del plugin se non esiste
            File databaseFile = new File(plugin.getDataFolder(), "gettone.db");
            if (!databaseFile.getParentFile().exists()) {
                databaseFile.getParentFile().mkdirs();
            }

            // Connessione al database SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS assegni (player TEXT, amount REAL)")) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createAssegno(String player, double amount) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO assegni (player, amount) VALUES (?, ?)")) {
            stmt.setString(1, player);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean riscattareAssegno(String player, double amount) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT amount FROM assegni WHERE player = ? AND amount >= ?")) {
            stmt.setString(1, player);
            stmt.setDouble(2, amount);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                try (PreparedStatement deleteStmt = connection.prepareStatement(
                        "DELETE FROM assegni WHERE player = ? AND amount = ?")) {
                    deleteStmt.setString(1, player);
                    deleteStmt.setDouble(2, amount);
                    deleteStmt.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean convertiAssegnoInGettoni(String player, double amount) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT amount FROM assegni WHERE player = ? AND amount >= ?")) {
            stmt.setString(1, player);
            stmt.setDouble(2, amount);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double currentAmount = rs.getDouble("amount");
                if (currentAmount > amount) {

                    try (PreparedStatement updateStmt = connection.prepareStatement(
                            "UPDATE assegni SET amount = amount - ? WHERE player = ? AND amount = ?")) {
                        updateStmt.setDouble(1, amount);
                        updateStmt.setString(2, player);
                        updateStmt.setDouble(3, currentAmount);
                        updateStmt.executeUpdate();
                    }
                } else {

                    try (PreparedStatement deleteStmt = connection.prepareStatement(
                            "DELETE FROM assegni WHERE player = ? AND amount = ?")) {
                        deleteStmt.setString(1, player);
                        deleteStmt.setDouble(2, currentAmount);
                        deleteStmt.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public boolean removeAssegno(String player, double amount) {
        double totalAmount = getTotalAssegni(player);

        if (totalAmount <= 0) {
            return false;
        }

        // Determina la somma da rimuovere
        double amountToRemove = Math.min(amount, totalAmount);

        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE assegni SET amount = amount - ? WHERE player = ? AND amount >= ?")) {
            stmt.setDouble(1, amountToRemove);
            stmt.setString(2, player);
            stmt.setDouble(3, amountToRemove);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private double getTotalAssegni(String player) {
        double total = 0;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT SUM(amount) as total FROM assegni WHERE player = ?")) {
            stmt.setString(1, player);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public void aggiungiGettoni(String player, double amount) {
        createAssegno(player, amount); // Aggiunge l'importo specificato al saldo del giocatore
    }

    public double getSaldo(String player) {
        double saldo = 0;
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT SUM(amount) as total FROM assegni WHERE player = ?")) {
            stmt.setString(1, player);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                saldo = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saldo;
    }
}
