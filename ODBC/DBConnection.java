import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    // Updated based on your SQL*Plus screenshot
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
    private static final String USER = "system";
    private static final String PASSWORD = "system";

    public static Connection getConnection() {
        Connection con = null;
        try {
            // Load Oracle JDBC Driver (Requires ojdbc.jar in classpath)
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.err.println("DB Connection Error: " + e.getMessage());
        }
        return con;
    }
}
