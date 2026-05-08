import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminProductDashboard extends JFrame {

    JTextField txtName, txtCategory, txtPrice, txtStock;
    JTextArea txtDescription;
    JTable table;
    DefaultTableModel model;
    Connection con;
    int selectedProductId = -1;

    public AdminProductDashboard() {
        setTitle("Admin Dashboard - Product Management");
        setSize(950, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtName = new JTextField();
        txtCategory = new JTextField();
        txtPrice = new JTextField();
        txtStock = new JTextField();
        txtDescription = new JTextArea();
        txtDescription.setLineWrap(true);
        JScrollPane descScrollPane = new JScrollPane(txtDescription);

        formPanel.add(new JLabel("Product Name"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Category"));
        formPanel.add(txtCategory);
        formPanel.add(new JLabel("Price"));
        formPanel.add(txtPrice);
        formPanel.add(new JLabel("Stock Quantity"));
        formPanel.add(txtStock);
        formPanel.add(new JLabel("Description"));
        formPanel.add(descScrollPane);

        model = new DefaultTableModel(
                new String[] { "Product ID", "Product Name", "Category", "Price", "Stock Qty", "Description" }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Product Records", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(formPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        add(buttonPanel, BorderLayout.SOUTH);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        } else {
            loadTable();
        }

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedProductId = Integer.parseInt(model.getValueAt(row, 0).toString());
                    txtName.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                    txtCategory.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    txtPrice.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    txtStock.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                    txtDescription.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
                }
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Products (product_name, category, price, stock_quantity, description) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, txtName.getText());
                ps.setString(2, txtCategory.getText());
                ps.setDouble(3, Double.parseDouble(txtPrice.getText()));
                ps.setInt(4, Integer.parseInt(txtStock.getText()));
                ps.setString(5, txtDescription.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                loadTable();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Add Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            if (selectedProductId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product from the table to update.");
                return;
            }
            try {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE Products SET product_name = ?, category = ?, price = ?, stock_quantity = ?, description = ? WHERE product_id = ?");
                ps.setString(1, txtName.getText());
                ps.setString(2, txtCategory.getText());
                ps.setDouble(3, Double.parseDouble(txtPrice.getText()));
                ps.setInt(4, Integer.parseInt(txtStock.getText()));
                ps.setString(5, txtDescription.getText());
                ps.setInt(6, selectedProductId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                loadTable();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            if (selectedProductId == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product from the table to delete.");
                return;
            }
            try {
                if (con == null) return;
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                PreparedStatement ps = con.prepareStatement("DELETE FROM Products WHERE product_id = ?");
                ps.setInt(1, selectedProductId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                loadTable();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> clearFields());

        setVisible(true);
    }

    void loadTable() {
        try {
            if (con == null) return;
            model.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT product_id, product_name, category, price, stock_quantity, description FROM Products ORDER BY product_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
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
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    void clearFields() {
        txtName.setText("");
        txtCategory.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        txtDescription.setText("");
        selectedProductId = -1;
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminProductDashboard());
    }
}
