import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrderItemsDisplay extends JFrame {

    JTextField txtCustomerId;
    JTable table;
    DefaultTableModel model;
    Connection con;

    public OrderItemsDisplay() {
        setTitle("Order Items by Customer");
        setSize(900, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.add(new JLabel("Customer ID:"));
        txtCustomerId = new JTextField(10);
        topPanel.add(txtCustomerId);
        JButton btnSearch = new JButton("View Order Items");
        topPanel.add(btnSearch);
        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[] { "Order Item ID", "Order ID", "Product ID", "Quantity", "Price" }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        } else {
            loadAllOrders();
        }

        btnSearch.addActionListener(e -> {
            try {
                if (con == null) return;
                String idText = txtCustomerId.getText().trim();
                if (idText.isEmpty()) {
                    loadAllOrders();
                    return;
                }
                int customerId = Integer.parseInt(idText);
                loadOrderItems(customerId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric Customer ID.");
            }
        });

        setVisible(true);
    }

    void loadAllOrders() {
        try {
            if (con == null) return;
            model.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT oi.order_item_id, oi.order_id, oi.product_id, oi.quantity, oi.price " +
                    "FROM order_items oi JOIN orders o ON oi.order_id = o.order_id " +
                    "ORDER BY oi.order_id, oi.order_item_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("order_item_id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    void loadOrderItems(int customerId) {
        try {
            if (con == null) return;
            model.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT oi.order_item_id, oi.order_id, oi.product_id, oi.quantity, oi.price " +
                    "FROM order_items oi JOIN orders o ON oi.order_id = o.order_id " +
                    "WHERE o.customer_id = ? ORDER BY oi.order_id, oi.order_item_id");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                model.addRow(new Object[] {
                        rs.getInt("order_item_id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                });
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No order items found for Customer ID: " + customerId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderItemsDisplay());
    }
}
