package model;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/perpus";
    private static final String USER = "root";  // ganti kalau MySQL kamu pakai user lain
    private static final String PASS = "OscarR12";  // password MySQL 

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
