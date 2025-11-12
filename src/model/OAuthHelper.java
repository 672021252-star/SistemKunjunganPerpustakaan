package model;

import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class OAuthHelper {

    private static final String CLIENT_ID = "e1b61dcc-adee-4089-bbb8-26162f23b32f";
    private static final String CLIENT_SECRET = "b3d4065f-ee37-490e-b75a-bdb45d5f6b15";

    public static String getAccessToken() {
        try {
            URL url = new URL("https://opac.uksw.edu/api/v1/oauth/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            // Header 
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Accept", "application/json");

            // Encode parameter agar aman
            String body = "grant_type=" + URLEncoder.encode("client_credentials", "UTF-8")
                    + "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
                    + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8");

            // Kirim request
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = body.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            int status = con.getResponseCode();
            System.out.println("Token response code: " + status);

            InputStream is = (status == 200) ? con.getInputStream() : con.getErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            System.out.println("Token response: " + response);

            if (status == 200) {
                JSONObject json = new JSONObject(response.toString());
                return json.getString("access_token");
            } else {
                System.err.println("Gagal mendapatkan token: " + response);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
