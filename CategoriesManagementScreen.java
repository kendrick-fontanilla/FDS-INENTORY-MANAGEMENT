package assetmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class CategoriesManagementScreen {

    private static DefaultTableModel tableModel;
    private static JTable categoryTable;

    // Connect to SQLite database
    private static void connectToDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
            System.exit(1);
        }
    }

    // Add category
    private static void addCategory(String categoryName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO categories (name) VALUES (?)");
            pstmt.setString(1, categoryName);
            pstmt.executeUpdate();
            loadCategories();
            JOptionPane.showMessageDialog(null, "Category added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding category: " + e.getMessage());
        }
    }

    // Update category
    private static void updateCategory(int id, String categoryName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "UPDATE categories SET name = ? WHERE id = ?");
            pstmt.setString(1, categoryName);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            loadCategories();
            JOptionPane.showMessageDialog(null, "Category updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating category: " + e.getMessage());
        }
    }

    // Delete category
    private static void deleteCategory(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM categories WHERE id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            loadCategories();
            JOptionPane.showMessageDialog(null, "Category deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting category: " + e.getMessage());
        }
    }

    // Load categories into the table
    private static void loadCategories() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            tableModel.setRowCount(0); // Clear table
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM categories");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading categories: " + e.getMessage());
        }
    }

    // Show Category Management System
    public static void showCategoriesManagementScreen() {
        connectToDatabase(); // Ensure database connection is initialized

        // Set up GUI
        JFrame frame = new JFrame("Category Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Category Name"}, 0);
        categoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(categoryTable);

        // Buttons
        JButton addButton = new JButton("Add Category");
        JButton updateButton = new JButton("Update Category");
        JButton deleteButton = new JButton("Delete Category");

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Add listeners
        addButton.addActionListener(e -> {
            String categoryName = showInputDialogWithCancel(frame, "Enter the new category name:");
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                addCategory(categoryName);
            } else if (categoryName != null) {
                JOptionPane.showMessageDialog(null, "Category name cannot be empty.");
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "No category selected.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String categoryName = showInputDialogWithCancel(frame, "Enter the new category name:");
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                updateCategory(id, categoryName);
            } else if (categoryName != null) {
                JOptionPane.showMessageDialog(null, "Category name cannot be empty.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "No category selected.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            deleteCategory(id);
        });

        // Load categories and set up frame
        loadCategories();
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    // Show input dialog with OK and Cancel options
    private static String showInputDialogWithCancel(Component parentComponent, String message) {
        JTextField textField = new JTextField(20);
        JPanel panel = new JPanel();
        panel.add(new JLabel(message));
        panel.add(textField);

        // Show JOptionPane with custom buttons (OK and Cancel)
        int option = JOptionPane.showOptionDialog(parentComponent, panel, "Input",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, null, null);

        // If Cancel is selected, return null to cancel the operation
        if (option == JOptionPane.CANCEL_OPTION) {
            return null;
        }

        return textField.getText();
    }
}
