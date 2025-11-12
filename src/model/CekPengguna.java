package model;

import java.io.*;
import java.net.*;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CekPengguna {
    public static void main(String[] args) {
        //JSONObject data = CekPengguna.getDataPengguna("692021091");
        //System.out.println("Hasil: " + data.toString());
    }

    public static JSONObject getDataPengguna(String nimNip) {
        try {
            // Ambil access token dari OAuthHelper
            String token = OAuthHelper.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("error", "Gagal mendapatkan token dari OPAC");
                return error;
            }

            // Koneksi ke API OPAC
            String urlStr = "https://opac.uksw.edu/api/v1/patrons?cardnumber=" + URLEncoder.encode(nimNip, "UTF-8");
            System.out.println("Request URL: " + urlStr);

            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + token);
            con.setRequestProperty("Accept", "application/json");

            int status = con.getResponseCode();
            System.out.println("Response Code: " + status);

            InputStream inputStream = (status == 200) ? con.getInputStream() : con.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            //System.out.println("Response body: " + response.toString());

            // Parsing hasil JSON dari OPAC
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(response.toString());

            if (status == 200) {
                JSONObject result = new JSONObject();

                if (parsed instanceof JSONArray) {
                    JSONArray dataArray = (JSONArray) parsed;
                    if (!dataArray.isEmpty()) {
                        org.json.simple.JSONObject pengguna = (org.json.simple.JSONObject) dataArray.get(0);

                        String firstname = pengguna.containsKey("firstname") && pengguna.get("firstname") != null
                                ? pengguna.get("firstname").toString()
                                : "";
                        String middlename = pengguna.containsKey("middlename") && pengguna.get("middlename") != null
                                ? pengguna.get("middlename").toString()
                                : "";
                        String surname = pengguna.containsKey("surname") && pengguna.get("surname") != null
                                ? pengguna.get("surname").toString()
                                : "";

                        // Gabungkan nama lengkap dengan spasi rapi
                        String fullname = (firstname + " " + middlename + " " + surname).trim().replaceAll("\\s+", " ");

                        result.put("firstname", firstname);
                        result.put("middlename", middlename);
                        result.put("surname", surname);
                        result.put("fullname", fullname);

                        System.out.println("Response body:");
                        System.out.println("Firstname: " + firstname);
                        System.out.println("Middlename: " + middlename);
                        System.out.println("Surname: " + surname);
                        System.out.println("Fullname: " + fullname);

                        return result;
                    } else {
                        JSONObject error = new JSONObject();
                        error.put("error", "Data tidak ditemukan di OPAC");
                        return error;
                    }
                }
            } else if (status == 403) {
                JSONObject error = new JSONObject();
                error.put("error", "Akses ke OPAC ditolak (403)");
                return error;
            } else {
                JSONObject error = new JSONObject();
                error.put("error", "Gagal ambil data dari OPAC. Kode: " + status);
                return error;
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("error", "Terjadi kesalahan saat mengambil data dari OPAC: " + e.getMessage());
            return error;
        }

        return null;
    }
}
