import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerRegisterOnly extends JFrame {

    JTextField txtName, txtEmail, txtPhone;
    JPasswordField txtPassword;
    JTextArea txtAddress;
    JTable table;
    DefaultTableModel model;
    Connection con;

    public CustomerRegisterOnly() {
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
                new String[] { "ID", "Name", "Email", "Phone", "Password", "Address" }, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Customer Records", SwingConstants.CENTER), BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        add(formPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton btnRegister = new JButton("Register");
        buttonPanel.add(btnRegister);
        add(buttonPanel, BorderLayout.SOUTH);

        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed.");
        } else {
            loadTable();
        }

        btnRegister.addActionListener(e -> {
            try {
                if (con == null)
                    return;

                String name = txtName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                String password = new String(txtPassword.getPassword()).trim();
                String address = txtAddress.getText().trim();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                    return;
                }

                String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
                if (!email.matches(emailRegex)) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid email address ");
                    return;
                }

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Customers (name, email, phone, password, address) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, password);
                ps.setString(5, address);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Customer registered successfully!");
                loadTable();
                clearFields();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Registration Error: " + ex.getMessage());
            }
        });

        setVisible(true);
    }

    void loadTable() {
        try {
            if (con == null)
                return;
            model.setRowCount(0);
            PreparedStatement ps = con.prepareStatement(
                    "SELECT customer_id, name, email, phone, password, address FROM Customers ORDER BY customer_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[] {
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerRegisterOnly());
    }
}
