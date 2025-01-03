package assetmanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.table.DefaultTableModel;
public class MainScreen {

    private JFrame frame;
    private JPanel dashboardPanel;

    public static void showMainScreen(String loggedUser) {
        new MainScreen(loggedUser);
    }

    private MainScreen(String loggedUser) {
        // Create the main frame
        frame = new JFrame("Asset Management System - Main Screen");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Display logged-in user info at the top
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setBackground(new Color(230, 230, 250));
        JLabel welcomeLabel = new JLabel("Welcome, " + loggedUser + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userInfoPanel.add(welcomeLabel);
        frame.add(userInfoPanel, BorderLayout.NORTH);

        // Dashboard Panel
        dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new GridLayout(2, 2, 15, 15)); // 2x2 grid layout for stats and actions
        frame.add(dashboardPanel, BorderLayout.CENTER);

        // Initialize dashboard
        refreshDashboard();

        // Set up the menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu assetsMenu = new JMenu("Assets");

        JMenuItem listAssetsItem = new JMenuItem("Assets");
        listAssetsItem.addActionListener(e -> AssetManagementSystem.showAssetManagementSystem());

        JMenuItem categoriesItem = new JMenuItem("Categories");
        categoriesItem.addActionListener(e -> CategoriesManagementScreen.showCategoriesManagementScreen());

        JMenuItem assetStatusItem = new JMenuItem("Status");
        assetStatusItem.addActionListener(e -> StatusManagementSystem.showStatusManagementSystem());

        assetsMenu.add(listAssetsItem);
        assetsMenu.add(categoriesItem);
        assetsMenu.add(assetStatusItem);

        JMenu companyMenu = new JMenu("Company");
        JMenuItem departmentsItem = new JMenuItem("Departments");
        departmentsItem.addActionListener(e -> DepartmentManagementScreen.showDepartmentManagementScreen());

        JMenuItem usersItem = new JMenuItem("Users");
        usersItem.addActionListener(e -> UserManagementScreen.showUserManagementScreen());

        companyMenu.add(departmentsItem);
        companyMenu.add(usersItem);

        menuBar.add(assetsMenu);
        menuBar.add(companyMenu);

        // Set the menu bar to the frame
        frame.setJMenuBar(menuBar);

        // Footer
        JPanel footerPanel = FooterUtils.createFooterPanel();
        frame.add(footerPanel, BorderLayout.SOUTH);

        // Set up auto-refresh for the dashboard
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshDashboard();
            }
        }, 0, 5000); // Refresh every 5 seconds

        // Show the frame
        frame.setVisible(true);
    }
    
    
    public class FooterUtils {

    // Method to create and return a footer panel
    public static JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(245, 245, 245));

        JLabel footerLabel = new JLabel("Stephanie Asset Management System © 2024");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        footerPanel.add(footerLabel);
        return footerPanel;
    }
}

    private void refreshDashboard() {
        dashboardPanel.removeAll();

        JPanel assetsCard = createClickableStatCard("Total Assets", getTotalAssetsCount(), new Color(135, 206, 250));
        JPanel categoriesCard = createClickableStatCard("Good Condition", getTotalCategoriesCount(), new Color(144, 238, 144));
        JPanel statusesCard = createClickableStatCard("Damaged", getTotalStatusesCount(), new Color(255, 182, 193));
        JPanel departmentsCard = createClickableStatCard("Maintenance", getTotalDepartmentsCount(), new Color(255, 215, 0));

        dashboardPanel.add(assetsCard);
        dashboardPanel.add(categoriesCard);
        dashboardPanel.add(statusesCard);
        dashboardPanel.add(departmentsCard);

        dashboardPanel.revalidate();
        dashboardPanel.repaint();
    }

    private JPanel createClickableStatCard(String title, int count, Color backgroundColor) {
    JPanel card = new JPanel();
    card.setLayout(new BorderLayout());
    card.setBackground(backgroundColor);
    card.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    JLabel titleLabel = new JLabel(title, JLabel.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

    JLabel countLabel = new JLabel(String.valueOf(count), JLabel.CENTER);
    countLabel.setFont(new Font("Arial", Font.BOLD, 24));

    // Add mouse listener for click events
    card.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (title.equals("Total Assets")) {
                AssetManagementSystem.showAssetManagementSystem();
            } else {
                openEmptyForm(title);
            }
        }
    });

    card.add(titleLabel, BorderLayout.NORTH);
    card.add(countLabel, BorderLayout.CENTER);

    return card;
}

    private void openEmptyForm(String title) {
    JFrame formFrame = new JFrame(title);
    formFrame.setSize(400, 300);
    formFrame.setLayout(new BorderLayout());

    // Create table for displaying data
    JTable dataTable = new JTable();
    DefaultTableModel tableModel = new DefaultTableModel();
    tableModel.setColumnIdentifiers(new Object[]{"Name", "Department"});
    dataTable.setModel(tableModel);

    // Fetch data based on the title
    fetchDataForDashboard(title, tableModel);

    // Add table to scroll pane
    JScrollPane scrollPane = new JScrollPane(dataTable);
    formFrame.add(scrollPane, BorderLayout.CENTER);

    // Show the form
    formFrame.setVisible(true);
}

private void fetchDataForDashboard(String title, DefaultTableModel tableModel) {
    String query = null;

    switch (title) {
        case "Total Assets":
            query = """
                    SELECT assets.name, departments.department_name 
                    FROM assets 
                    JOIN statuses ON assets.status_id = statuses.id 
                    JOIN departments ON assets.department_id = departments.id 
                    """;
            break;
        case "Good Condition":
            query = """
                    SELECT assets.name, departments.department_name 
                    FROM assets 
                    JOIN statuses ON assets.status_id = statuses.id 
                    JOIN departments ON assets.department_id = departments.id 
                    WHERE LOWER(statuses.status_name) IN ('ok', 'available', 'new')
                    """;
            break;
        case "Damaged":
            query = """
                    SELECT assets.name, departments.department_name 
                    FROM assets 
                    JOIN statuses ON assets.status_id = statuses.id 
                    JOIN departments ON assets.department_id = departments.id 
                    WHERE statuses.status_name = 'Damaged'
                    """;
            break;
        case "Maintenance":
            query = """
                    SELECT assets.name, departments.department_name 
                    FROM assets 
                    JOIN statuses ON assets.status_id = statuses.id 
                    JOIN departments ON assets.department_id = departments.id 
                    WHERE statuses.status_name = 'Maintenance'
                    """;
            break;
    }

    if (query != null) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("name");
                String department = rs.getString("department_name");
                tableModel.addRow(new Object[]{name, department});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching data: " + e.getMessage());
        }
    }
}

    private int getTotalAssetsCount() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM assets");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching total assets: " + e.getMessage());
        }
        return 0;
    }

    private int getTotalCategoriesCount() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) " +
                "FROM assets " +
                "JOIN statuses ON assets.status_id = statuses.id " +
                "WHERE LOWER(statuses.status_name) IN ('ok', 'available', 'new')"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching total categories: " + e.getMessage());
        }
        return 0;
    }

    private int getTotalStatusesCount() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) " +
                "FROM assets " +
                "JOIN statuses ON assets.status_id = statuses.id " +
                "WHERE statuses.status_name IN ('Damaged')"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching total statuses: " + e.getMessage());
        }
        return 0;
    }

    private int getTotalDepartmentsCount() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) " +
                "FROM assets " +
                "JOIN statuses ON assets.status_id = statuses.id " +
                "WHERE statuses.status_name IN ('Maintenance')"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error fetching total departments: " + e.getMessage());
        }
        return 0;
    }
}