package ru.meefik.linuxdeploy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class ExecService extends JobIntentService {

    public static final String TAG = "linuxDeployLog";

    public static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ExecService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final String cmd = intent.getStringExtra("cmd");
        final String args = intent.getStringExtra("args");
        Thread thread = new Thread(() -> {
            switch (cmd) {
                case "telnetd":
                    EnvUtils.telnetd(getBaseContext(), args);
                    break;
                case "httpd":
                    EnvUtils.httpd(getBaseContext(), args);
                    break;
                default:
                    // PrefStore.showNotification(getBaseContext(), null);
                    if (EnvUtils.cli(getApplicationContext(), cmd, args)) {
                        if (cmd.contains("start")) {
                            if (PrefStore.isNotification(this)) {
                                startService(new Intent(this, ForegroundService.class));
                            } else {
                                Log.i(TAG, "notification is not enable");
                            }
                        } else if (cmd.contains("stop")) {
                            if (PrefStore.isNotification(this)) {
                                stopService(new Intent(this, ForegroundService.class));
                            }
                        }
                    } else {
                        Log.e(TAG, "cmd exec failed");
                    }
            }
        });
        thread.start();
    }
}
