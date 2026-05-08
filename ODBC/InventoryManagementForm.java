import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryManagementForm extends JFrame {

    JTextField txtCustomerId, txtProductId, txtQuantity;
    JTable productTable;
    DefaultTableModel productModel;
    Connection con;

    public InventoryManagementForm() {
        setTitle("Product Inventory Management");
        setSize(950, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Place Order"));

        txtCustomerId = new JTextField();
        txtProductId = new JTextField();
        txtQuantity = new JTextField();

        formPanel.add(new JLabel("Customer ID"));
        formPanel.add(txtCustomerId);
        formPanel.add(new JLabel("Product ID"));
        formPanel.add(txtProductId);
        formPanel.add(new JLabel("Quantity"));
        formPanel.add(txtQuantity);

        JButton btnPlaceOrder = new JButton("Place Order");
        JButton btnClear = new JButton("Clear");
        formPanel.add(btnPlaceOrder);
        formPanel.add(btnClear);

        add(formPanel, BorderLayout.WEST);

        productModel = new DefaultTableModel(
                new String[] { "Product ID", "Product Name", "Category", "Price", "Stock Quantity" }, 0);
        productTable = new JTable(productModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Product Inventory (Stock)", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.CENTER);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        } else {
            loadInventory();
        }

        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = productTable.getSelectedRow();
                if (row >= 0) {
                    txtProductId.setText(productModel.getValueAt(row, 0).toString());
                }
            }
        });

        btnPlaceOrder.addActionListener(e -> {
            try {
                if (con == null)
                    return;

                String custIdText = txtCustomerId.getText().trim();
                String prodIdText = txtProductId.getText().trim();
                String qtyText = txtQuantity.getText().trim();

                if (custIdText.isEmpty() || prodIdText.isEmpty() || qtyText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                    return;
                }

                int customerId = Integer.parseInt(custIdText);
                int productId = Integer.parseInt(prodIdText);
                int quantity = Integer.parseInt(qtyText);

                PreparedStatement checkStock = con.prepareStatement(
                        "SELECT stock_quantity, price FROM Products WHERE product_id = ?");
                checkStock.setInt(1, productId);
                ResultSet stockRs = checkStock.executeQuery();

                if (!stockRs.next()) {
                    JOptionPane.showMessageDialog(this, "Product not found.");
                    return;
                }

                int currentStock = stockRs.getInt("stock_quantity");
                double unitPrice = stockRs.getDouble("price");

                if (quantity > currentStock) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + currentStock);
                    return;
                }

                double totalAmount = unitPrice * quantity;

                PreparedStatement orderPs = con.prepareStatement(
                        "INSERT INTO orders (customer_id, order_date, total_amount, order_status) VALUES (?, CURRENT_TIMESTAMP, ?, 'Placed')",
                        new String[] { "ORDER_ID" });
                orderPs.setInt(1, customerId);
                orderPs.setDouble(2, totalAmount);
                orderPs.executeUpdate();

                ResultSet generatedKeys = orderPs.getGeneratedKeys();
                int orderId = 0;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                }

                PreparedStatement itemPs = con.prepareStatement(
                        "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)");
                itemPs.setInt(1, orderId);
                itemPs.setInt(2, productId);
                itemPs.setInt(3, quantity);
                itemPs.setDouble(4, unitPrice);
                itemPs.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Order placed successfully!\nOrder ID: " + orderId +
                                "\nTotal Amount: " + totalAmount +
                                "\nStock updated automatically by trigger.");

                loadInventory();
                clearFields();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Order Error: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> clearFields());

        setVisible(true);
    }

    void loadInventory() {
        try {
            if (con == null)
                return;
            productModel.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT product_id, product_name, category, price, stock_quantity FROM Products ORDER BY product_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                productModel.addRow(new Object[] {
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    void clearFields() {
        txtCustomerId.setText("");
        txtProductId.setText("");
        txtQuantity.setText("");
        productTable.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryManagementForm());
    }
}
