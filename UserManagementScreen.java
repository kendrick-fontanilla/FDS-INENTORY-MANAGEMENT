
package assetmanagementsystem;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class UserManagementScreen {

    //private static Connection connection;

    // Method to connect to the database
    private static void connectToDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            if (connection == null) {
                
                Statement stmt = connection.createStatement();
                
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
            System.exit(1);
        }
    }

    // Method to show the User Management Screen
    public static void showUserManagementScreen() {
        connectToDatabase(); // Ensure database connection

        // Create frame for user management
        JFrame userFrame = new JFrame("Manage Users");
        userFrame.setSize(600, 400);
        userFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a JTable to display users
        JTable userTable = new JTable();
        DefaultTableModel userTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Password"}, 0);
        userTable.setModel(userTableModel);

        // Load users into the table
        loadUsers(userTableModel);

        // Button to edit user information
        JButton editButton = new JButton("Edit User");
        editButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (int) userTable.getValueAt(selectedRow, 0);
                String username = (String) userTable.getValueAt(selectedRow, 1);
                String password = (String) userTable.getValueAt(selectedRow, 2);
                editUser(userId, username, password, userTableModel);
            }
        });

        // Button to add new user
        JButton addButton = new JButton("Add User");
        addButton.addActionListener(e -> addUser(userTableModel));

        // Layout for the user management screen
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(editButton);
        buttonPanel.add(addButton);

        userPanel.add(buttonPanel, BorderLayout.SOUTH);

        userFrame.add(userPanel);
        userFrame.setVisible(true);
    }

    // Method to load users into the JTable
    private static void loadUsers(DefaultTableModel userTableModel) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                userTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage());
        }
    }

    // Method to add a new user
    private static void addUser(DefaultTableModel userTableModel) {
        // Create a dialog to enter username and password
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new GridLayout(2, 2));
        addPanel.add(new JLabel("Username:"));
        addPanel.add(usernameField);
        addPanel.add(new JLabel("Password:"));
        addPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(null, addPanel, "Add New User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            insertUser(username, password, userTableModel);
        }
    }

    // Method to insert a new user into the database
    private static void insertUser(String username, String password, DefaultTableModel userTableModel) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            // Reload users after inserting
            userTableModel.setRowCount(0); // Clear table
            loadUsers(userTableModel);
            JOptionPane.showMessageDialog(null, "User added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding user: " + e.getMessage());
        }
    }

    // Method to edit user information
    private static void editUser(int userId, String username, String password, DefaultTableModel userTableModel) {
        // Create a dialog to edit user details
        JTextField usernameField = new JTextField(username);
        JTextField passwordField = new JTextField(password);

        JPanel editPanel = new JPanel();
        editPanel.setLayout(new GridLayout(2, 2));
        editPanel.add(new JLabel("Username:"));
        editPanel.add(usernameField);
        editPanel.add(new JLabel("Password:"));
        editPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(null, editPanel, "Edit User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newUsername = usernameField.getText();
            String newPassword = passwordField.getText();
            updateUser(userId, newUsername, newPassword, userTableModel);
        }
    }

    // Method to update user information in the database
    private static void updateUser(int userId, String username, String password, DefaultTableModel userTableModel) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "UPDATE users SET username = ?, password = ? WHERE id = ?");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();

            // Reload users after update
            userTableModel.setRowCount(0); // Clear table
            loadUsers(userTableModel);
            JOptionPane.showMessageDialog(null, "User updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating user: " + e.getMessage());
        }
    }
}