import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductDisplayPage extends JFrame {

    JTable table;
    DefaultTableModel model;
    Connection con;

    public ProductDisplayPage() {
        setTitle("Product Display");
        setSize(900, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Product Information", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[] { "Product ID", "Product Name", "Category", "Price", "Stock Quantity", "Description" }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton btnSearch = new JButton("Search by Name");
        JButton btnSearchCategory = new JButton("Search by Category");
        JButton btnSearchId = new JButton("Search by ID");
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnSearchCategory);
        buttonPanel.add(btnSearchId);
        add(buttonPanel, BorderLayout.SOUTH);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        } else {
            loadProducts();
        }

        btnSearch.addActionListener(e -> {
            try {
                if (con == null)
                    return;
                String keyword = JOptionPane.showInputDialog(this, "Enter product name to search:");
                if (keyword == null || keyword.trim().isEmpty())
                    return;

                model.setRowCount(0);
                PreparedStatement ps = con.prepareStatement(
                        "SELECT product_id, product_name, category, price, stock_quantity, description FROM Products WHERE LOWER(product_name) LIKE LOWER(?) ORDER BY product_id");
                ps.setString(1, "%" + keyword.trim() + "%");
                ResultSet rs = ps.executeQuery();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    addRowFromResultSet(rs);
                }
                if (!found) {
                    JOptionPane.showMessageDialog(this, "No products found matching: " + keyword);
                    loadProducts();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Search Error: " + ex.getMessage());
            }
        });

        btnSearchCategory.addActionListener(e -> {
            try {
                if (con == null)
                    return;
                String category = JOptionPane.showInputDialog(this, "Enter category to search:");
                if (category == null || category.trim().isEmpty())
                    return;

                model.setRowCount(0);
                PreparedStatement ps = con.prepareStatement(
                        "SELECT product_id, product_name, category, price, stock_quantity, description FROM Products WHERE LOWER(category) LIKE LOWER(?) ORDER BY product_id");
                ps.setString(1, "%" + category.trim() + "%");
                ResultSet rs = ps.executeQuery();

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    addRowFromResultSet(rs);
                }
                if (!found) {
                    JOptionPane.showMessageDialog(this, "No products found in category: " + category);
                    loadProducts();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Search Error: " + ex.getMessage());
            }
        });

        btnSearchId.addActionListener(e -> {
            try {
                if (con == null)
                    return;
                String idText = JOptionPane.showInputDialog(this, "Enter Product ID:");
                if (idText == null || idText.trim().isEmpty())
                    return;
                int id = Integer.parseInt(idText.trim());

                model.setRowCount(0);
                PreparedStatement ps = con.prepareStatement(
                        "SELECT product_id, product_name, category, price, stock_quantity, description FROM Products WHERE product_id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    addRowFromResultSet(rs);
                } else {
                    JOptionPane.showMessageDialog(this, "No product found with ID: " + id);
                    loadProducts();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric ID.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Search Error: " + ex.getMessage());
            }
        });

        setVisible(true);
    }

    void addRowFromResultSet(ResultSet rs) throws Exception {
        String desc = "";
        Object descObj = rs.getObject("description");
        if (descObj != null) {
            if (descObj instanceof Clob) {
                Clob clob = (Clob) descObj;
                desc = clob.getSubString(1, (int) clob.length());
            } else {
                desc = descObj.toString();
            }
        }
        model.addRow(new Object[] {
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity"),
                desc
        });
    }

    void loadProducts() {
        try {
            if (con == null)
                return;
            model.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT product_id, product_name, category, price, stock_quantity, description FROM Products ORDER BY product_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                addRowFromResultSet(rs);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductDisplayPage());
    }
}
