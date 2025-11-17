package com.hotspotmonitor;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

public class MonitoringService extends Service {
    private Handler handler = new Handler();
    private static final String PHP_API_URL = "https://yourbilling.free.nf/hotspot-monitor/api/receive.php";
    private static final String SECRET_KEY = "8888";

    private Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            checkConnectedDevices();
            handler.postDelayed(this, 30000); // run every 30 seconds
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Hotspot monitor started", Toast.LENGTH_SHORT).show();
        handler.post(monitorRunnable);
        return START_STICKY;
    }

    private void checkConnectedDevices() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("secret_key", SECRET_KEY);
                    data.put("timestamp", getCurrentTimestamp());

                    JSONArray devices = new JSONArray();
                    devices.put(createDevice("AA:BB:CC:DD:EE:FF", "192.168.43.101", "Johns-iPhone"));
                    devices.put(createDevice("11:22:33:44:55:66", "192.168.43.102", "Android-Device"));

                    data.put("devices", devices);

                    Log.d("HotspotMonitor", "Form JSON: " + data.toString());
                    return sendToServer(data.toString());

                } catch (Exception e) {
                    Log.e("HotspotMonitor", "Error: " + e.getMessage());
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("HotspotMonitor", "Server response: " + result);
                Toast.makeText(MonitoringService.this, "Server: " + result, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private JSONObject createDevice(String mac, String ip, String hostname) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("mac_address", mac);
        obj.put("ip_address", ip);
        obj.put("hostname", hostname);
        obj.put("connected_at", getCurrentTimestamp());
        return obj;
    }

    private String sendToServer(String jsonData) {
        try {
            URL url = new URL(PHP_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            String payload = "--" + boundary + "\r\n" +
				"Content-Disposition: form-data; name=\"input\"\r\n\r\n" +
				jsonData + "\r\n" +
				"--" + boundary + "--\r\n";

            Log.d("HotspotMonitor", "Sending Payload: " + payload);

            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes("UTF-8"));
            os.close();

            return "Code: " + conn.getResponseCode() + " - " + conn.getResponseMessage();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
