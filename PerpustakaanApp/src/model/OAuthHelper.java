package model;

import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class OAuthHelper {

    private static final String TOKEN_URL = "https://opac.uksw.edu/api/v1/oauth/token";
    private static final String CLIENT_ID = "e1b61dcc-adee-4089-bbb8-26162f23b32f";
    private static final String CLIENT_SECRET = "b3d4065f-ee37-490e-b75a-bdb45d5f6b15";

    public static String getToken() {
        try {
            URL url = new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String data = "grant_type=client_credentials"
                        + "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
                        + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(data.getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                JSONObject json = new JSONObject(response.toString());
                return json.getString("access_token");
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String error = br.readLine();
                System.out.println("Error Response: " + error);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
