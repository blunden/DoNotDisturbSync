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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class DNDStatusChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "DndStatusReceiver";

    private Context mContext;
    private SharedPreferences mPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            Log.d(TAG, "Received a RINGER_MODE_CHANGED");

            if (mPreferences.getBoolean("use_ringer_mode", false)) {
                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

                int deviceRingerMode = audioManager.getRingerMode();

                Log.d(TAG, "Current ringer mode is: " + deviceRingerMode);

                if (deviceRingerMode != AudioManager.RINGER_MODE_SILENT) {
                    // Save the "normal" non-silent mode to avoid forcing audible ring tones on the watch
                    saveNormalRingerMode(deviceRingerMode);
                }
                sendRingerMode(deviceRingerMode);
            } else {
                // Read and send the Interruption filter status instead (which controls the DND mode)
                // Syncing the actual ringer mode has other unintended consequences and doesn't match
                // what AW 1.5 synced.
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                int deviceDndMode = mNotificationManager.getCurrentInterruptionFilter();

                Log.d(TAG, "Current DND mode is: " + deviceDndMode);
                sendDndMode(deviceDndMode);
            }
        }

        if (intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
            Log.d(TAG, "Received an INTERRUPTION_FILTER_CHANGED");

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int deviceDndMode = mNotificationManager.getCurrentInterruptionFilter();

            Log.d(TAG, "Current DND mode is: " + deviceDndMode);
            sendDndMode(deviceDndMode);
        }
    }

    private void sendRingerMode(int ringerMode) {
        // Build the Intent needed to start the WearMessageSenderService
        Intent wearMessageSenderServiceIntent = new Intent(mContext, WearMessageSenderService.class);
        wearMessageSenderServiceIntent.setAction(WearMessageSenderService.ACTION_SEND_MESSAGE);
        wearMessageSenderServiceIntent.putExtra(WearMessageSenderService.EXTRA_RINGER_MODE, String.valueOf(ringerMode));

        mContext.startService(wearMessageSenderServiceIntent);
    }

    private void sendDndMode(int dndMode) {
        // Build the Intent needed to start the WearMessageSenderService
        Intent wearMessageSenderServiceIntent = new Intent(mContext, WearMessageSenderService.class);
        wearMessageSenderServiceIntent.setAction(WearMessageSenderService.ACTION_SEND_MESSAGE);
        wearMessageSenderServiceIntent.putExtra(WearMessageSenderService.EXTRA_DND_MODE, String.valueOf(dndMode));

        mContext.startService(wearMessageSenderServiceIntent);
    }

    private void saveNormalRingerMode(int ringerMode) {
        mPreferences.edit().putInt("normalMode", ringerMode).apply();
    }
}
