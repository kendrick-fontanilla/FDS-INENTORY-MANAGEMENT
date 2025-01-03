package assetmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class StatusManagementSystem {

    private static DefaultTableModel tableModel;
    private static JTable statusTable;

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

    // Add status
    private static void addStatus(String statusName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO statuses (status_name) VALUES (?)");
            pstmt.setString(1, statusName);
            pstmt.executeUpdate();
            loadStatuses();
            JOptionPane.showMessageDialog(null, "Status added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding status: " + e.getMessage());
        }
    }

    // Update status
    private static void updateStatus(int id, String statusName) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                    "UPDATE statuses SET status_name = ? WHERE id = ?");
            pstmt.setString(1, statusName);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            loadStatuses();
            JOptionPane.showMessageDialog(null, "Status updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating status: " + e.getMessage());
        }
    }

    // Delete status
    private static void deleteStatus(int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM statuses WHERE id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            loadStatuses();
            JOptionPane.showMessageDialog(null, "Status deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting status: " + e.getMessage());
        }
    }

    // Load statuses into the table
    private static void loadStatuses() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            tableModel.setRowCount(0); // Clear table
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM statuses");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("status_name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading statuses: " + e.getMessage());
        }
    }

    // Show Status Management System
    public static void showStatusManagementSystem() {
        connectToDatabase(); // Ensure database connection is initialized

        // Set up GUI
        JFrame frame = new JFrame("Status Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Status"}, 0);
        statusTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(statusTable);

        // Buttons
        JButton addButton = new JButton("Add Status");
        JButton updateButton = new JButton("Update Status");
        JButton deleteButton = new JButton("Delete Status");

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Add listeners
        addButton.addActionListener(e -> {
            String statusName = showInputDialogWithCancel(frame, "Enter the new status name:");
            if (statusName != null && !statusName.trim().isEmpty()) {
                addStatus(statusName);
            } else if (statusName != null) {
                JOptionPane.showMessageDialog(null, "Status name cannot be empty.");
            }
        });

        updateButton.addActionListener(e -> {
            int selectedRow = statusTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "No status selected.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String statusName = showInputDialogWithCancel(frame, "Enter the new status name:");
            if (statusName != null && !statusName.trim().isEmpty()) {
                updateStatus(id, statusName);
            } else if (statusName != null) {
                JOptionPane.showMessageDialog(null, "Status name cannot be empty.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = statusTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "No status selected.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            deleteStatus(id);
        });

        // Load statuses and set up frame
        loadStatuses();
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
