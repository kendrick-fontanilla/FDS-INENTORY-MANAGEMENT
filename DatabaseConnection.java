
package assetmanagementsystem;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Singleton connection instance
    private static Connection connection;

    // Private constructor to prevent instantiation
    private DatabaseConnection() {}

    // Method to get the database connection
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Establish connection to SQLite database
                connection = DriverManager.getConnection("jdbc:sqlite:company_assets.db");
                System.out.println("Connected to the database.");
            } catch (SQLException e) {
                System.out.println("Database connection error: " + e.getMessage());
            }
        }
        return connection;
    }
    
    
    // Method to close the database connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing database connection: " + e.getMessage());
        }
    }

  
}