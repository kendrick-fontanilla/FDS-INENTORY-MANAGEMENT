package assetmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class DepartmentManagementScreen {

    private static DefaultTableModel tableModel;
    private static JTable departmentTable;

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

    // Add department
    private static void addDepartment(String departmentName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO departments (department_name) VALUES (?)");
            pstmt.setString(1, departmentName);
            pstmt.executeUpdate();
            loadDepartments();
            JOptionPane.showMessageDialog(null, "Department added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding department: " + e.getMessage());
        }
    }

    // Update department
    private static void updateDepartment(int id, String departmentName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "UPDATE departments SET department_name = ? WHERE id = ?");
            pstmt.setString(1, departmentName);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            loadDepartments();
            JOptionPane.showMessageDialog(null, "Department updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating department: " + e.getMessage());
        }
    }

    // Delete department
    private static void deleteDepartment(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM departments WHERE id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            loadDepartments();
            JOptionPane.showMessageDialog(null, "Department deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting department: " + e.getMessage());
        }
    }

    // Load departments into the table
    private static void loadDepartments() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            tableModel.setRowCount(0); // Clear table
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM departments");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("department_name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading departments: " + e.getMessage());
        }
    }

    // Show Department Management System
    public static void showDepartmentManagementScreen() {
        connectToDatabase(); // Ensure database connection is initialized

        // Set up GUI
        JFrame frame = new JFrame("Department Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Department Name"}, 0);
        departmentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(departmentTable);

        // Buttons
        JButton addButton = new JButton("Add Department");
        JButton updateButton = new JButton("Update Department");
        JButton deleteButton = new JButton("Delete Department");

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Add listeners
        addButton.addActionListener(e -> {
            String departmentName = showInputDialogWithCancel(frame, "Enter the new department name:");
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                addDepartment(departmentName);
            } else if (departmentName != null) {
                JOptionPane.showMessageDialog(null, "Department name cannot be empty.");
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = departmentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "No department selected.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String departmentName = showInputDialogWithCancel(frame, "Enter the new department name:");
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                updateDepartment(id, departmentName);
            } else if (departmentName != null) {
                JOptionPane.showMessageDialog(null, "Department name cannot be empty.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = departmentTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "No department selected.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            deleteDepartment(id);
        });

        // Load departments and set up frame
        loadDepartments();
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
