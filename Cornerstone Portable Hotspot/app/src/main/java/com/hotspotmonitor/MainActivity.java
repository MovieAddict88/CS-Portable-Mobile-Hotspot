package com.hotspotmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        // Start monitoring service
        Intent serviceIntent = new Intent(this, MonitoringService.class);
        startService(serviceIntent);

        statusText.setText("🚀 Hotspot Monitor Started!\nMonitoring your hotspot connections...");
    }
}
