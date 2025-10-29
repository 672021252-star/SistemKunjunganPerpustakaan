package model;

import java.io.*;
import java.net.*;
import org.json.*;
    
public class CekPengguna {

    public static JSONObject getDataPengguna(String cardnumber) {
        try {
            String token = OAuthHelper.getToken();
            if (token == null) {
                System.out.println("Token gagal diperoleh dari OPAC.");
                return new JSONObject("{\"name\":\"Data tidak ditemukan di OPAC\",\"status\":\"Unknown\"}");
            }

            URL url = new URL("https://opac.uksw.edu/api/v1/patrons?cardnumber=" + cardnumber);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                JSONArray arr = new JSONArray(sb.toString());
                if (arr.length() > 0) return arr.getJSONObject(0);
                return new JSONObject("{\"name\":\"Tidak ditemukan\",\"status\":\"Invalid\"}");
            } else {
                System.out.println("Gagal ambil data dari OPAC. Kode: " + responseCode);
                return new JSONObject("{\"name\":\"Data tidak ditemukan di OPAC\",\"status\":\"Unknown\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject("{\"name\":\"Error koneksi ke OPAC\",\"status\":\"Unknown\"}");
        }
    }
}
