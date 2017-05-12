package se.blunden.donotdisturbsync;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearMessageListenerService extends WearableListenerService {
    private static final String TAG = "DndSyncListener";
    private static final String DND_SYNC_PREFIX = "/wear-dnd-sync";

    // Toggle between sending the ringer mode or the interrupt filter mode
    private static final boolean SEND_RINGER_MODE = true;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(DND_SYNC_PREFIX)) {
            // Read the received ringer or dnd mode and convert it back to an integer
            int newMode = Integer.parseInt(new String(messageEvent.getData()));

            if (SEND_RINGER_MODE) {
                Log.d(TAG, "Received new ringer mode " + newMode + " from source " + messageEvent.getSourceNodeId());
            } else {
                Log.d(TAG, "Received new dnd mode " + newMode + " from source " + messageEvent.getSourceNodeId());
            }

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check if the notification policy access has been granted for the app
            // This is needed to set modes that affect Do Not Disturb in Android N
            if (mNotificationManager.isNotificationPolicyAccessGranted() || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (SEND_RINGER_MODE) {
                    Log.d(TAG, "Attempting to set ringer mode " + newMode);

                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    if (newMode == AudioManager.RINGER_MODE_SILENT) {
                        audioManager.setRingerMode(newMode);
                    } else {
                        // Set the saved "normal" value
                        audioManager.setRingerMode(getNormalRingerMode());
                    }
                } else {
                    Log.d(TAG, "Attempting to set dnd mode " + newMode);

                    mNotificationManager.setInterruptionFilter(newMode);
                }
            } else {
                Log.i(TAG, "Unable to set new ringer mode due to lack of permissions on device");
                Log.i(TAG, "Launching permissions settings activity on the device");
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private int getNormalRingerMode() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        return sharedPreferences.getInt("normalMode", 2);
    }
}
