/*
 * Copyright (C) 2017 blunden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.blunden.donotdisturbsync;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
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
    private static final String DND_SYNC_MODE = "/wear-dnd-sync";

    private SharedPreferences mPreferences;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (messageEvent.getPath().equalsIgnoreCase(DND_SYNC_MODE)) {
            // Read the received ringer or dnd mode and convert it back to an integer
            int newMode = Integer.parseInt(new String(messageEvent.getData()));

            if (mPreferences.getBoolean("use_ringer_mode", false)) {
                Log.d(TAG, "Received new ringer mode " + newMode + " from source " + messageEvent.getSourceNodeId());
            } else {
                Log.d(TAG, "Received new dnd mode " + newMode + " from source " + messageEvent.getSourceNodeId());
            }

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check if the notification policy access has been granted for the app
            // This is needed to set modes that affect Do Not Disturb in Android N
            if (mNotificationManager.isNotificationPolicyAccessGranted() || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (mPreferences.getBoolean("use_ringer_mode", false)) {
                    Log.d(TAG, "Attempting to set ringer mode " + newMode);

                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    if (newMode == AudioManager.RINGER_MODE_SILENT) {
                        audioManager.setRingerMode(newMode);
                    } else {
                        // Set the saved "normal" value
                        audioManager.setRingerMode(getNormalRingerMode());
                    }
                } else {
                    // Android Wear's DND modes behave unexpectedly so toggle user preferred mode or off instead
                    if (newMode != NotificationManager.INTERRUPTION_FILTER_ALL) {
                        newMode = Integer.parseInt(mPreferences.getString("preferred_phone_dnd_mode",
                                String.valueOf(NotificationManager.INTERRUPTION_FILTER_ALARMS)));
                    } else {
                        newMode = NotificationManager.INTERRUPTION_FILTER_ALL;
                    }

                    Log.d(TAG, "Attempting to set adjusted dnd mode " + newMode);
                    mNotificationManager.setInterruptionFilter(newMode);
                }
            } else {
                Log.i(TAG, "Unable to set new ringer mode due to lack of permissions on device");
                Log.i(TAG, "Launching permissions settings activity on the device");
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    // Some devices may not have this activity it seems
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Failed to open the Do Not Disturb access settings");
                }
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private int getNormalRingerMode() {
        return mPreferences.getInt("normalMode", AudioManager.RINGER_MODE_NORMAL);
    }
}
