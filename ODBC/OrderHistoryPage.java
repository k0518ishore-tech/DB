import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrderHistoryPage extends JFrame {

    JTextField txtCustomerId;
    JTable orderTable, itemTable;
    DefaultTableModel orderModel, itemModel;
    Connection con;

    public OrderHistoryPage() {
        setTitle("Order History");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top panel - Customer ID input
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topPanel.add(new JLabel("Customer ID:"));
        txtCustomerId = new JTextField(10);
        topPanel.add(txtCustomerId);
        JButton btnSearch = new JButton("View Orders");
        topPanel.add(btnSearch);
        add(topPanel, BorderLayout.NORTH);

        // Orders table (top half)
        orderModel = new DefaultTableModel(
                new String[] { "Order ID", "Customer ID", "Order Date", "Total Amount", "Order Status" }, 0);
        orderTable = new JTable(orderModel);
        JScrollPane orderScroll = new JScrollPane(orderTable);

        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.add(new JLabel("  Orders", SwingConstants.LEFT), BorderLayout.NORTH);
        orderPanel.add(orderScroll, BorderLayout.CENTER);

        // Order Items table (bottom half)
        itemModel = new DefaultTableModel(
                new String[] { "Order Item ID", "Order ID", "Product ID", "Quantity", "Price" }, 0);
        itemTable = new JTable(itemModel);
        JScrollPane itemScroll = new JScrollPane(itemTable);

        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.add(new JLabel("  Order Items (click an order above to view)"), BorderLayout.NORTH);
        itemPanel.add(itemScroll, BorderLayout.CENTER);

        // Split pane for orders and items
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, orderPanel, itemPanel);
        splitPane.setDividerLocation(220);
        add(splitPane, BorderLayout.CENTER);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        }

        // Search orders by customer ID
        btnSearch.addActionListener(e -> {
            try {
                if (con == null) return;
                String idText = txtCustomerId.getText().trim();
                if (idText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter a Customer ID.");
                    return;
                }
                int customerId = Integer.parseInt(idText);
                loadOrders(customerId);
                itemModel.setRowCount(0);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric Customer ID.");
            }
        });

        // Click on order row to show its items
        orderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = orderTable.getSelectedRow();
                if (row >= 0) {
                    int orderId = Integer.parseInt(orderModel.getValueAt(row, 0).toString());
                    loadOrderItems(orderId);
                }
            }
        });

        setVisible(true);
    }

    void loadOrders(int customerId) {
        try {
            if (con == null) return;
            orderModel.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT order_id, customer_id, order_date, total_amount, order_status FROM orders WHERE customer_id = ? ORDER BY order_date DESC");
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            while (rs.next()) {
                found = true;
                orderModel.addRow(new Object[] {
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("order_status")
                });
            }
            if (!found) {
                JOptionPane.showMessageDialog(this, "No orders found for Customer ID: " + customerId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    void loadOrderItems(int orderId) {
        try {
            if (con == null) return;
            itemModel.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT order_item_id, order_id, product_id, quantity, price FROM order_items WHERE order_id = ? ORDER BY order_item_id");
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemModel.addRow(new Object[] {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OrderHistoryPage());
    }
}
