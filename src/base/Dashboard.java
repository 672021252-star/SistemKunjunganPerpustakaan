package base;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.DatabaseConnection;

public class Dashboard extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> filterLayanan;
    private JComboBox<String> filterUrut;

    public Dashboard() {
        setTitle("Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === PANEL ATAS: FILTER ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Filter titik layanan
        filterLayanan = new JComboBox<>(new String[]{"Semua", "lobi", "serial", "sirkulasi", "referensi"});
        topPanel.add(new JLabel("Titik Layanan:"));
        topPanel.add(filterLayanan);

        // Filter urutan poin
        filterUrut = new JComboBox<>(new String[]{"Default", "Poin Terbanyak"});
        topPanel.add(new JLabel("Urutkan:"));
        topPanel.add(filterUrut);

        // Tombol tampilkan data
        JButton btnRefresh = new JButton("Tampilkan Data");
        topPanel.add(btnRefresh);
        
        // Tombol tampilkan tamu
        JButton btnTamu = new JButton("👥 Tampilkan Tamu");
        topPanel.add(btnTamu);
        btnTamu.addActionListener(e -> loadDataTamu());

        // Tombol hapus data terpilih
        JButton btnHapus = new JButton("🗑️ Hapus");
        btnHapus.setBackground(new Color(220, 53, 69));
        btnHapus.setForeground(Color.WHITE);
        topPanel.add(btnHapus);

        add(topPanel, BorderLayout.NORTH);

        // === TABEL ===
        model = new DefaultTableModel(new String[]{"NIM/NIP", "Tanggal", "Titik Layanan", "Poin"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // === AKSI ===
        btnRefresh.addActionListener(e -> loadData());
        btnHapus.addActionListener(e -> hapusDataTerpilih());

        // Load awal
        loadData();

        setVisible(true);
    }

    // === METHOD UTAMA: LOAD DATA ===
    private void loadData() {
        model.setRowCount(0); // bersihkan tabel

        String titik = (String) filterLayanan.getSelectedItem();
        String urut = (String) filterUrut.getSelectedItem();

        String sql = """
            SELECT p.nim_nip,
                   p.poin,
                   k.titik_layanan,
                   MAX(k.tanggal) AS tanggal_terakhir
            FROM pengunjung p
            JOIN kunjungan k ON p.nim_nip = k.nim_nip
        """;

        if (!titik.equals("Semua")) {
            sql += " WHERE k.titik_layanan = ?";
        }

        sql += " GROUP BY p.nim_nip, p.poin, k.titik_layanan";

        if ("Poin Terbanyak".equals(urut)) {
            sql += " ORDER BY p.poin DESC";
        } else {
            sql += " ORDER BY tanggal_terakhir DESC";
        }


        try (Connection conn = DatabaseConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (!titik.equals("Semua")) {
                stmt.setString(1, titik);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("nim_nip"),
                    rs.getTimestamp("tanggal_terakhir"),
                    rs.getString("titik_layanan"),
                    rs.getInt("poin")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Gagal memuat data: " + e.getMessage());
        }
    }

    // === METHOD TAMBAHAN: HAPUS DATA TERPILIH ===
    private void hapusDataTerpilih() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Pilih baris data yang ingin dihapus terlebih dahulu!");
            return;
        }

        String nim_nip = model.getValueAt(selectedRow, 0).toString();

        int konfirmasi = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin menghapus semua data kunjungan untuk NIM/NIP: " + nim_nip + "?",
            "Konfirmasi Hapus Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (konfirmasi == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getDatabaseConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM kunjungan WHERE nim_nip = ?")) {

                stmt.setString(1, nim_nip);
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "✅ Data dengan NIM/NIP " + nim_nip + " berhasil dihapus!");
                } else {
                    JOptionPane.showMessageDialog(this, "⚠️ Tidak ditemukan data untuk NIM/NIP tersebut.");
                }

                // Refresh tabel setelah hapus
                loadData();

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Terjadi kesalahan saat menghapus data:\n" + e.getMessage(),
                    "Error Database",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
        // === METHOD TAMBAHAN: LOAD DATA TAMU ===
    private void loadDataTamu() {
        model.setRowCount(0); // hapus data lama
        model.setColumnCount(0); // hapus kolom lama

        // Buat kolom khusus tamu
        model.addColumn("ID");
        model.addColumn("Nama");
        model.addColumn("Status");
        model.addColumn("Tanggal");

        String sql = "SELECT id, nama, status, tanggal FROM tamu ORDER BY tanggal DESC";

        try (Connection conn = DatabaseConnection.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("status"),
                    rs.getTimestamp("tanggal")
                });
            }

            JOptionPane.showMessageDialog(this, "✅ Data tamu berhasil dimuat!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Gagal memuat data tamu: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard());
    }
}
