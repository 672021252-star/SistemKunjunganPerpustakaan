package model;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Pengunjung {

    public static int getIdPengunjung(String nim) {
        try (Connection con = DatabaseConnection.getDatabaseConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT id_pengunjung FROM pengunjung WHERE nim_nip=?");
            ps.setString(1, nim);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_pengunjung");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void tambahPoin(String nim) {
        try (Connection con = DatabaseConnection.getDatabaseConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE pengunjung SET poin = poin + 1 WHERE nim_nip=?");
            ps.setString(1, nim);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
