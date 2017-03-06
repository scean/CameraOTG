package com.serenegiant.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Apple on 2017/3/5.
 */

public class BootupReceiver extends BroadcastReceiver {

    private final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_BOOT.equals(intent.getAction())){
            Log.i("FlowWindowService","onReceive BOOT_COMPLETED");
            final Intent intentService = new Intent(context, FlowWindowService.class);
            context.startService(intentService);
        }
    }
}