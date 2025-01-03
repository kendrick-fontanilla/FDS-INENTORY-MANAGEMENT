package assetmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AssetManagementSystem {

    private static DefaultTableModel tableModel;
    private static JTable assetTable;

    // Connect to SQLite database
    private static void connectToDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            // Ensure tables exist (categories, statuses, departments)
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    // Filter assets based on input
private static void filterAssets(String nameFilter, Category categoryFilter, Status statusFilter, Department departmentFilter) {
    try {
        Connection connection = DatabaseConnection.getConnection();
        
        String query = 
            "SELECT assets.id, assets.name, categories.name AS category, " +
            "statuses.status_name AS status, departments.department_name AS department " +
            "FROM assets " +
            "JOIN categories ON assets.category_id = categories.id " +
            "JOIN statuses ON assets.status_id = statuses.id " +
            "JOIN departments ON assets.department_id = departments.id WHERE 1=1";
        
        if (!nameFilter.isEmpty()) {
            query += " AND assets.name LIKE ?";
        }
        if (categoryFilter != null) {
            query += " AND assets.category_id = ?";
        }
        if (statusFilter != null) {
            query += " AND assets.status_id = ?";
        }
        if (departmentFilter != null) {
            query += " AND assets.department_id = ?";
        }

        PreparedStatement pstmt = connection.prepareStatement(query);

        int index = 1;
        if (!nameFilter.isEmpty()) {
            pstmt.setString(index++, "%" + nameFilter + "%");
        }
        if (categoryFilter != null) {
            pstmt.setInt(index++, categoryFilter.getId());
        }
        if (statusFilter != null) {
            pstmt.setInt(index++, statusFilter.getId());
        }
        if (departmentFilter != null) {
            pstmt.setInt(index++, departmentFilter.getId());
        }

        ResultSet rs = pstmt.executeQuery();

        tableModel.setRowCount(0); // Clear table
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("status"),
                rs.getString("department"),
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error filtering assets: " + e.getMessage());
    }
}

    
    
    
    private static void showUpdateAssetDialog() {
    int selectedRow = assetTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(null, "Please select an asset to update.");
        return;
    }

    // Get current values from the selected row
    int id = (int) tableModel.getValueAt(selectedRow, 0);
    String currentName = (String) tableModel.getValueAt(selectedRow, 1);
    String currentCategory = (String) tableModel.getValueAt(selectedRow, 2);
    String currentStatus = (String) tableModel.getValueAt(selectedRow, 3);
    String currentDepartment = (String) tableModel.getValueAt(selectedRow, 4);

    // Dialog components
    JTextField nameField = new JTextField(currentName, 15);
    JComboBox<Category> categoryBox = new JComboBox<>();
    JComboBox<Status> statusBox = new JComboBox<>();
    JComboBox<Department> departmentBox = new JComboBox<>();

    // Load combo box data and set the current selection
    loadCategories(categoryBox);
    loadStatuses(statusBox);
    loadDepartments(departmentBox);

    setSelectedItem(categoryBox, currentCategory);
    setSelectedItem(statusBox, currentStatus);
    setSelectedItem(departmentBox, currentDepartment);

    // Create panel for the dialog
    JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
    panel.add(new JLabel("Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Category:"));
    panel.add(categoryBox);
    panel.add(new JLabel("Status:"));
    panel.add(statusBox);
    panel.add(new JLabel("Department:"));
    panel.add(departmentBox);

    // Show dialog
    int result = JOptionPane.showConfirmDialog(null, panel, "Update Asset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
        String newName = nameField.getText();
        Category selectedCategory = (Category) categoryBox.getSelectedItem();
        Status selectedStatus = (Status) statusBox.getSelectedItem();
        Department selectedDepartment = (Department) departmentBox.getSelectedItem();

        // Validate input
        if (newName.isEmpty() || selectedCategory == null || selectedStatus == null || selectedDepartment == null) {
            JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update only if there are changes
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                "UPDATE assets SET name = ?, category_id = ?, status_id = ?, department_id = ? WHERE id = ?");

            pstmt.setString(1, newName);
            pstmt.setInt(2, selectedCategory.getId());
            pstmt.setInt(3, selectedStatus.getId());
            pstmt.setInt(4, selectedDepartment.getId());
            pstmt.setInt(5, id);

            pstmt.executeUpdate();
            loadAssets();
            JOptionPane.showMessageDialog(null, "Asset updated successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating asset: " + e.getMessage());
        }
    }
}

// Helper to set selected item in JComboBox
private static <T> void setSelectedItem(JComboBox<T> comboBox, String value) {
    for (int i = 0; i < comboBox.getItemCount(); i++) {
        T item = comboBox.getItemAt(i);
        if (item.toString().equals(value)) {
            comboBox.setSelectedIndex(i);
            break;
        }
    }
}
    
    
    // Add asset
    private static void addAsset(String name, int categoryId, int statusId, int departmentId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO assets (name, category_id, status_id, department_id) VALUES (?, ?, ?, ?)");
            pstmt.setString(1, name);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, statusId);
            pstmt.setInt(4, departmentId);

            pstmt.executeUpdate();
            loadAssets();
            JOptionPane.showMessageDialog(null, "Asset added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding asset: " + e.getMessage());
        }
    }
    
    private static void deleteSelectedAsset() {
    int selectedRow = assetTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(null, "Please select an asset to delete.");
        return;
    }

    // Get asset ID from the selected row
    int assetId = (int) tableModel.getValueAt(selectedRow, 0);

    // Confirm deletion
    int confirm = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to delete this asset?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement("DELETE FROM assets WHERE id = ?");
            pstmt.setInt(1, assetId);

            pstmt.executeUpdate();
            loadAssets(); // Reload the assets to reflect changes
            JOptionPane.showMessageDialog(null, "Asset deleted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error deleting asset: " + e.getMessage());
        }
    }
}
    
    
    
    
    // Load assets into the table
    private static void loadAssets() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            tableModel.setRowCount(0); // Clear table
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT assets.id, assets.name, categories.name AS category, " +
                "statuses.status_name AS status, departments.department_name AS department " +
                "FROM assets " +
                "JOIN categories ON assets.category_id = categories.id " +
                "JOIN statuses ON assets.status_id = statuses.id " +
                "JOIN departments ON assets.department_id = departments.id");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("status"),
                    rs.getString("department"),
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading assets: " + e.getMessage());
        }
    }

    // Show Add Asset Dialog
    private static void showAddAssetDialog() {
        // Create components for dialog
        JTextField nameField = new JTextField(15);
        JComboBox<Category> categoryBox = new JComboBox<>();
        JComboBox<Status> statusBox = new JComboBox<>();
        JComboBox<Department> departmentBox = new JComboBox<>();

        // Load data into combo boxes
        loadCategories(categoryBox);
        loadStatuses(statusBox);
        loadDepartments(departmentBox);

        // Create a panel for the form
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryBox);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Department:"));
        panel.add(departmentBox);
        
        
        
        
        
        
        

        // Show dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "Add Asset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            Category selectedCategory = (Category) categoryBox.getSelectedItem();
            Status selectedStatus = (Status) statusBox.getSelectedItem();
            Department selectedDepartment = (Department) departmentBox.getSelectedItem();

            if (name.isEmpty() || selectedCategory == null || selectedStatus == null || selectedDepartment == null) {
                JOptionPane.showMessageDialog(null, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } else {
                addAsset(name, selectedCategory.getId(), selectedStatus.getId(), selectedDepartment.getId());
            }
        }
    }

    // Load categories into JComboBox
    private static void loadCategories(JComboBox<Category> categoryBox) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM categories");
            categoryBox.removeAllItems(); // Clear existing items
            while (rs.next()) {
                categoryBox.addItem(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading categories: " + e.getMessage());
        }
    }

    private static void loadStatuses(JComboBox<Status> statusBox) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM statuses");
            statusBox.removeAllItems(); // Clear existing items
            while (rs.next()) {
                statusBox.addItem(new Status(rs.getInt("id"), rs.getString("status_name")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading statuses: " + e.getMessage());
        }
    }

    private static void loadDepartments(JComboBox<Department> departmentBox) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM departments");
            departmentBox.removeAllItems(); // Clear existing items
            while (rs.next()) {
                departmentBox.addItem(new Department(rs.getInt("id"), rs.getString("department_name")));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading departments: " + e.getMessage());
        }
    }

    // Show Asset Management System
    public static void showAssetManagementSystem() {
        connectToDatabase(); // Ensure database connection is initialized

        // Set up GUI
        JFrame frame = new JFrame("Company Asset Management");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Status", "Department"}, 0);
        assetTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(assetTable);

        // Buttons
        JButton addButton = new JButton("Add Asset");

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);

        // Add listeners
        addButton.addActionListener(e -> showAddAssetDialog());

        // Load assets and set up frame
        loadAssets();
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        
        JButton editButton = new JButton("Edit Asset");

// Add to button panel
        buttonPanel.add(editButton);

// Add listener to the Edit Asset button
        editButton.addActionListener(e -> showUpdateAssetDialog());
        
        JButton deleteButton = new JButton("Delete Asset");

// Add to button panel
        buttonPanel.add(deleteButton);

// Add listener to the Delete Asset button
        deleteButton.addActionListener(e -> deleteSelectedAsset());
        
        
        // FILTERING 
        
        // Create filter components
JTextField nameFilterField = new JTextField(15);
JComboBox<Category> categoryFilterBox = new JComboBox<>();
JComboBox<Status> statusFilterBox = new JComboBox<>();
JComboBox<Department> departmentFilterBox = new JComboBox<>();

// Load data into combo boxes
loadCategories(categoryFilterBox);
loadStatuses(statusFilterBox);
loadDepartments(departmentFilterBox);

// Create a panel for filters
JPanel filterPanel = new JPanel(new GridLayout(2, 4, 10, 10));
filterPanel.add(new JLabel("Name:"));
filterPanel.add(nameFilterField);
filterPanel.add(new JLabel("Category:"));
filterPanel.add(categoryFilterBox);
filterPanel.add(new JLabel("Status:"));
filterPanel.add(statusFilterBox);
filterPanel.add(new JLabel("Department:"));
filterPanel.add(departmentFilterBox);

// Add a Filter button
JButton filterButton = new JButton("Filter");

// Add an ActionListener to the Filter button
filterButton.addActionListener(e -> {
    String nameFilter = nameFilterField.getText();
    Category selectedCategory = (Category) categoryFilterBox.getSelectedItem();
    Status selectedStatus = (Status) statusFilterBox.getSelectedItem();
    Department selectedDepartment = (Department) departmentFilterBox.getSelectedItem();
    filterAssets(nameFilter, selectedCategory, selectedStatus, selectedDepartment);
});

// Add a Reset button to clear filters
JButton resetButton = new JButton("Reset");
resetButton.addActionListener(e -> {
    nameFilterField.setText("");
    categoryFilterBox.setSelectedIndex(-1);
    statusFilterBox.setSelectedIndex(-1);
    departmentFilterBox.setSelectedIndex(-1);
    loadAssets(); // Reload all assets
});

// Add filter controls to the frame
JPanel topPanel = new JPanel(new BorderLayout());
topPanel.add(filterPanel, BorderLayout.CENTER);

JPanel filterButtonPanel = new JPanel();
filterButtonPanel.add(filterButton);
filterButtonPanel.add(resetButton);
topPanel.add(filterButtonPanel, BorderLayout.SOUTH);

frame.add(topPanel, BorderLayout.NORTH);

        
        
        
    }
}

// Category helper class
class Category {
    private final int id;
    private final String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}

class Status {
    private final int id;
    private final String name;

    public Status(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}

class Department {
    private final int id;
    private final String name;

    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
