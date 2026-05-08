import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerRegistrationForm extends JFrame {

    JTextField txtName, txtEmail, txtPhone;
    JPasswordField txtPassword;
    JTextArea txtAddress;
    JTable table;
    DefaultTableModel model;
    Connection con;

    public CustomerRegistrationForm() {
        setTitle("Customer Registration");
        setSize(900, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtPassword = new JPasswordField();
        txtAddress = new JTextArea();
        txtAddress.setLineWrap(true);
        JScrollPane addressScrollPane = new JScrollPane(txtAddress);

        formPanel.add(new JLabel("Name"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Email"));
        formPanel.add(txtEmail);
        formPanel.add(new JLabel("Phone"));
        formPanel.add(txtPhone);
        formPanel.add(new JLabel("Password"));
        formPanel.add(txtPassword);
        formPanel.add(new JLabel("Address"));
        formPanel.add(addressScrollPane);

        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Email", "Phone", "Password", "Address"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Customer Records", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(formPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton btnRegister = new JButton("Register / Insert");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSelect = new JButton("Select");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSelect);
        buttonPanel.add(btnClear);
        add(buttonPanel, BorderLayout.SOUTH);

        // Uses the DBConnection utility as per reference
        try {
            con = DBConnection.getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
            } else {
                loadTable();
            }
        } catch (Exception ex) {
            System.err.println("Database connection error: " + ex.getMessage());
        }

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtName.setText(model.getValueAt(row, 1) != null ? model.getValueAt(row, 1).toString() : "");
                    txtEmail.setText(model.getValueAt(row, 2) != null ? model.getValueAt(row, 2).toString() : "");
                    txtPhone.setText(model.getValueAt(row, 3) != null ? model.getValueAt(row, 3).toString() : "");
                    txtPassword.setText(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
                    txtAddress.setText(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");
                }
            }
        });

        btnRegister.addActionListener(e -> {
            try {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Customers (name, email, phone, password, address) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, txtName.getText());
                ps.setString(2, txtEmail.getText());
                ps.setString(3, txtPhone.getText());
                ps.setString(4, new String(txtPassword.getPassword()));
                ps.setString(5, txtAddress.getText());
                ps.executeUpdate();
                loadTable();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Insert Error: " + ex.getMessage());
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a record from the table to update.");
                return;
            }
            try {
                if (con == null) return;
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE Customers SET name = ?, email = ?, phone = ?, password = ?, address = ? WHERE customer_id = ?");
                ps.setString(1, txtName.getText());
                ps.setString(2, txtEmail.getText());
                ps.setString(3, txtPhone.getText());
                ps.setString(4, new String(txtPassword.getPassword()));
                ps.setString(5, txtAddress.getText());
                ps.setInt(6, id);
                ps.executeUpdate();
                loadTable();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select a record from the table to delete.");
                return;
            }
            try {
                if (con == null) return;
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                PreparedStatement ps = con.prepareStatement("DELETE FROM Customers WHERE customer_id = ?");
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage());
            }
        });

        btnSelect.addActionListener(e -> {
            try {
                if (con == null) return;
                String idText = JOptionPane.showInputDialog(this, "Enter Customer ID:");
                if (idText == null || idText.trim().isEmpty()) {
                    return;
                }
                int id = Integer.parseInt(idText.trim());

                PreparedStatement ps = con.prepareStatement(
                        "SELECT customer_id, name, email, phone, password, address FROM Customers WHERE customer_id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "ID: " + rs.getInt("customer_id") +
                            "\nName: " + rs.getString("name") +
                            "\nEmail: " + rs.getString("email") +
                            "\nPhone: " + rs.getString("phone") +
                            "\nAddress: " + rs.getString("address"));
                } else {
                    JOptionPane.showMessageDialog(this, "Customer not found.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Select Error: " + ex.getMessage());
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
                    "SELECT customer_id, name, email, phone, password, address FROM Customers ORDER BY customer_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("password"),
                        rs.getString("address")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Load Error: " + e.getMessage());
        }
    }

    void clearFields() {
        txtName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtPassword.setText("");
        txtAddress.setText("");
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerRegistrationForm());
    }
}
