package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://10.10.7.52:3306/patron_visit";
    private static final String USER = "libapp";
    private static final String PASSWORD = "libapp_p4ssword";

    public static Connection getDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Koneksi ke database berhasil!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC Driver tidak ditemukan!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Gagal koneksi ke database!");
            e.printStackTrace();
        }
        return null;
    }
}
