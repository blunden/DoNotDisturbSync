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
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearMessageListenerService extends WearableListenerService {
    private static final String TAG = "DndSyncListener";
    private static final String DND_SYNC_MODE = "/wear-dnd-sync";
    private static final String DND_SYNC_SETTING = "/wear-dnd-sync-setting";

    private SharedPreferences mPreferences;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (messageEvent.getPath().equalsIgnoreCase(DND_SYNC_MODE)) {
            // Read the received ringer or dnd mode and convert it back to an integer
            int newMode = Integer.parseInt(new String(messageEvent.getData()));

            if (mPreferences.getBoolean("use_ringer_mode", true)) {
                Log.d(TAG, "Received new ringer mode " + newMode + " from source " + messageEvent.getSourceNodeId());
            } else {
                Log.d(TAG, "Received new dnd mode " + newMode + " from source " + messageEvent.getSourceNodeId());
            }

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check if the notification policy access has been granted for the app
            // This is needed to set modes that affect Do Not Disturb in Android N
            if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                if (mPreferences.getBoolean("use_ringer_mode", true)) {
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
                Log.d(TAG, "App is not allowed to change Do Not Disturb mode without applying workaround");
            }
        } else if (messageEvent.getPath().equalsIgnoreCase(DND_SYNC_SETTING)) {
            // Read the received setting and convert it back to a boolean
            boolean newUseRingerModeValue = Boolean.valueOf(new String(messageEvent.getData()));

            Log.d(TAG, "Received new useRingerMode value: " + newUseRingerModeValue + " from source " + messageEvent.getSourceNodeId());

            mPreferences.edit().putBoolean("use_ringer_mode", newUseRingerModeValue).apply();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private int getNormalRingerMode() {
        return mPreferences.getInt("normalMode", 1);
    }
}
