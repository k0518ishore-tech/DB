import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProductDetailsDisplay extends JFrame {

    JTable table;
    DefaultTableModel model;
    Connection con;

    public ProductDetailsDisplay() {
        setTitle("Product Details");
        setSize(900, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Product Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[] { "Product ID", "Product Name", "Category", "Price", "Stock Quantity", "Description" }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        } else {
            loadProducts();
        }

        setVisible(true);
    }

    void loadProducts() {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductDetailsDisplay());
    }
}
