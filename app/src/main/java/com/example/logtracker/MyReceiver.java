package com.example.logtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private static long entryTime = 0;
    private static long exitTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            Log.e(TAG, "onReceive: GeofencingEvent is null");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        String notificationTitle = "";
        String notificationBody = "";

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                entryTime = System.currentTimeMillis();
                notificationTitle = "Entered Geofence";
                notificationBody = "Entry time: " + formatTime(entryTime);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                notificationTitle = "Dwelling in Geofence";
                notificationBody = "Entered at: " + formatTime(entryTime);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                exitTime = System.currentTimeMillis();
                long durationInMillis = exitTime - entryTime;
                notificationTitle = "Exited Geofence";
                notificationBody = "Entry time: " + formatTime(entryTime) +
                        "\nExit time: " + formatTime(exitTime) +
                        "\nDuration: " + formatDuration(durationInMillis);
                break;
        }

        Toast.makeText(context, notificationTitle, Toast.LENGTH_SHORT).show();
        notificationHelper.sendHighPriorityNotification(notificationTitle, notificationBody, MapsActivity.class);
    }

    private String formatTime(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timeInMillis));
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
}