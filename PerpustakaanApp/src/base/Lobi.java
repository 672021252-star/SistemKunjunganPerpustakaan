package base;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import javax.sound.sampled.*;
import javax.swing.*;
import model.DatabaseConnection;
import model.CekPengguna;
import org.json.JSONObject;

public class Lobi extends javax.swing.JFrame {
    
    private JTextField inputNim;
    private JLabel background;
    private String templateName;
    private int inputX = 600, inputY = 600, inputWidth = 400, inputHeight = 60; // default

    public Lobi() {
        setTitle("Lobi Perpustakaan O. Notohamidjojo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1600, 1137);
        setLocationRelativeTo(null);
        setLayout(null);

        //  Load template dari config.properties 
        loadTemplate();

        // background
        ImageIcon bgIcon = new ImageIcon(getClass().getResource("/design/" + templateName + ".jpg"));
        Image scaled = bgIcon.getImage().getScaledInstance(1600, 1137, Image.SCALE_SMOOTH);
        background = new JLabel(new ImageIcon(scaled));
        background.setBounds(0, 0, 1600, 1137);
        background.setLayout(null);

        // input field
        inputNim = new JTextField();
        inputNim.setFont(new Font("Poppins", Font.BOLD, 28));
        inputNim.setHorizontalAlignment(JTextField.CENTER);
        inputNim.setBorder(null);
        inputNim.setOpaque(false);
        inputNim.setBounds(inputX, inputY, inputWidth, inputHeight);
        background.add(inputNim);

        //  event ENTER 
        inputNim.addActionListener(e -> simpanKunjungan());

        // tambahkan background ke frame 
        add(background);
        setVisible(true);
    }
    
    private void loadTemplate() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            templateName = prop.getProperty("template", "lobi");

            // posisi input field sesuai template
            switch (templateName.toLowerCase()) {
                case "serial":
                case "sirkulasi":
                case "referensi":
                    inputX = 600; inputY = 600; inputWidth = 400; inputHeight = 60;
                    break;
                default:
                    inputX = 600; inputY = 600; inputWidth = 400; inputHeight = 60;
            }
        } catch (Exception e) {
            e.printStackTrace();
            templateName = "lobi";
        }
    }
    
    private void simpanKunjungan() {
    String nimNip = inputNim.getText().trim();
    if (nimNip.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan NIM/NIP terlebih dahulu!");
        return;
    }
    
        //  Ambil data dari backend OPAC
        JSONObject data = CekPengguna.getDataPengguna(nimNip);
        String nama = data.optString("name", "Pengguna Tidak Ditemukan");
        String status = data.optString("Status", "Unkown");
        
        System.out.println("Nama: " + nama);
        System.out.println("Status: " + status);

    try (Connection conn = DatabaseConnection.getConnection()) {

        //  Cek apakah pengunjung sudah terdaftar
        PreparedStatement checkStmt = conn.prepareStatement("SELECT poin FROM pengunjung WHERE nim_nip=?");
        checkStmt.setString(1, nimNip);
        ResultSet rs = checkStmt.executeQuery();

        boolean isRegistered = rs.next();

        //  Cek apakah sudah tercatat di titik layanan ini hari ini
        PreparedStatement checkVisit = conn.prepareStatement("""
            SELECT COUNT(*) FROM kunjungan 
            WHERE nim_nip = ? AND titik_layanan = ? AND DATE(tanggal) = CURDATE()
        """);
        checkVisit.setString(1, nimNip);
        checkVisit.setString(2, templateName);
        ResultSet visitCheck = checkVisit.executeQuery();
        visitCheck.next();

        boolean alreadyVisited = visitCheck.getInt(1) > 0;

        if (alreadyVisited) {
            // Sudah tercatat hari ini di titik yang sama → tidak disimpan
            JOptionPane.showMessageDialog(this, 
                "⚠️ Anda sudah tercatat di titik layanan '" + templateName + "' hari ini.\n" +
                "Silakan kunjungi titik layanan lain.");
            inputNim.setText("");
            return;
        }

        //  Simpan kunjungan baru (boleh di titik layanan berbeda)
        PreparedStatement logStmt = conn.prepareStatement(
                "INSERT INTO kunjungan (nim_nip, titik_layanan) VALUES (?, ?)");
        logStmt.setString(1, nimNip);
        logStmt.setString(2, templateName);
        logStmt.executeUpdate();

        //  Tambah poin hanya jika kunjungan berhasil disimpan
        if (isRegistered) {
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE pengunjung SET poin = poin + 1 WHERE nim_nip=?");
            updateStmt.setString(1, nimNip);
            updateStmt.executeUpdate();
        } else {
            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO pengunjung (nim_nip, poin) VALUES (?, 1)");
            insertStmt.setString(1, nimNip);
            insertStmt.executeUpdate();
        }

        //  Mainkan suara bell
        playBell();

        //  Popup sukses
        JOptionPane.showMessageDialog(this,
            "✅ Kunjungan berhasil dicatat! +1 poin");

        //  Reset input dan delay 10 detik
        inputNim.setText("");
        inputNim.setEnabled(false);
        new javax.swing.Timer(10000, evt -> inputNim.setEnabled(true)) {{
            setRepeats(false);
        }}.start();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "❌ Terjadi kesalahan: " + e.getMessage());
    }
}
    
    private void playBell() {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(getClass().getResource("/sound/Bell.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) clip.close();
            });
        } catch (Exception e) {
            System.out.println("Gagal memainkan suara: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField2.setText("jTextField2");
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 610, 460, 70));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/design/lobi.jpg"))); // NOI18N
        jLabel1.setText("jLabel1");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 1170));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Lobi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Lobi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Lobi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Lobi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Lobi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}