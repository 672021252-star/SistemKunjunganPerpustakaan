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
        
        //label sambutan
        JLabel labelSambutan = new JLabel("Selamat datang di Perpustakaan O. Notohamidjojo", SwingConstants.CENTER);
        labelSambutan.setFont(new Font("Poppins", Font.BOLD, 26));
        labelSambutan.setForeground(Color.WHITE); // ganti warna sesuai background
        labelSambutan.setHorizontalAlignment(SwingConstants.CENTER);
        labelSambutan.setBounds(inputX - 150, inputY + 80, inputWidth + 300, 100);
        background.add(labelSambutan);
        
        // event ketika NIM diinput
        inputNim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String nimNip = inputNim.getText().trim();
                
                if (nimNip.length() >= 9) { // hindari spam
                    new Thread(() -> {
                        try {
                            JSONObject data = CekPengguna.getDataPengguna(nimNip);
                            String fullname = data.optString("fullname", "");

                            SwingUtilities.invokeLater(() -> {
                                if (!fullname.isEmpty()) {
                                    labelSambutan.setText(
                                            "<html><div style='text-align:center;'>"
                                            + "Selamat datang di Perpustakaan O. Notohamidjojo<br>"
                                            + "<b>" + fullname + "!!</b>"
                                            + "</div></html>"
                                    );
                                } else if (data.has("error")) {
                                    labelSambutan.setText("<html><center>Data tidak ditemukan</center></html>");
                                } else {
                                    labelSambutan.setText("");
                                }
                            });
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(() -> {
                                labelSambutan.setText("<html><center>Gagal memuat data</center></html>");
                            });
                        }
                    }).start();
                } else {
                    labelSambutan.setText("");
                }
            }
        });

        //  event ENTER 
        inputNim.addActionListener(e -> {
            String input = inputNim.getText().trim();
            
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan NIM/NIP atau Nama Tamu!");
                return;
            }
            
            // Jika input berupa angka -> pengunjung internal
            if (input.matches("\\d{9,}")) {
                simpanKunjungan(input);
            } else {
                // Jika input huruf -> tamu luar
                simpanTamu(input);
            }
        });


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
    
    private void simpanKunjungan(String nimNip) {
        try (Connection conn = DatabaseConnection.getDatabaseConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "❌ Gagal terhubung ke database!");
                return;
            }

            // Ambil data pengguna dari API OPAC
            JSONObject data = CekPengguna.getDataPengguna(nimNip);
            String nama = data.optString("fullname", "Pengguna Tidak Ditemukan");

            // Cek apakah sudah tercatat hari ini
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
                JOptionPane.showMessageDialog(this, "⚠️ Anda sudah tercatat hari ini.");
                inputNim.setText("");
                return;
            }

            // Simpan ke tabel kunjungan
            PreparedStatement logStmt = conn.prepareStatement(
                    "INSERT INTO kunjungan (nim_nip, titik_layanan, tanggal) VALUES (?, ?, NOW())");
            logStmt.setString(1, nimNip);
            logStmt.setString(2, templateName);
            logStmt.executeUpdate();

            // Tambah poin di tabel pengunjung
            PreparedStatement update = conn.prepareStatement(
                    "INSERT INTO pengunjung (nim_nip, poin) VALUES (?, 1) ON DUPLICATE KEY UPDATE poin = poin + 1");
            update.setString(1, nimNip);
            update.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Kunjungan berhasil dicatat! +1 poin\nSelamat datang, " + nama);
            playBell();

            resetInput();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Terjadi kesalahan: " + e.getMessage());
        }
    }
    
    private void simpanTamu(String nama) {
        try (Connection conn = DatabaseConnection.getDatabaseConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "❌ Gagal terhubung ke database!");
                return;
            }

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO tamu (nama, status, tanggal) VALUES (?, 'tamu', NOW())"
            );
            stmt.setString(1, nama);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Terima kasih, " + nama + "! Data tamu berhasil dicatat.");
            playBell();
            resetInput();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Gagal menyimpan data tamu: " + e.getMessage());
        }
    }
    
    private void resetInput() {
        inputNim.setText("");
        inputNim.setEnabled(false);
        new javax.swing.Timer(8000, evt -> inputNim.setEnabled(true)) {{
            setRepeats(false);
        }}.start();
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