/*
 * Copyright (C) 2017-2022 blunden
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
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearMessageListenerService extends WearableListenerService {
    private static final String TAG = "DndSyncListener";
    private static final String DND_SYNC_MODE = "/wear-dnd-sync";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(DND_SYNC_MODE)) {
            // Read the received DND mode and convert it back to an integer
            int newMode = Integer.parseInt(new String(messageEvent.getData()));

            Log.d(TAG, "Received new DND mode " + newMode + " from source " + messageEvent.getSourceNodeId());

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Check if the notification policy access has been granted for the app
            // This is needed to set modes that affect Do Not Disturb in Android N or later
            if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                // Avoid unnecessarily triggering extra DND mode change events
                if (newMode == mNotificationManager.getCurrentInterruptionFilter()) {
                    return;
                }

                if (newMode < 1) {
                    Log.i(TAG, "Received an invalid notification interruption filter: " + newMode);
                    return;
                }

                // Wear OS's DND modes behave unexpectedly so toggle Alarms Only or Off instead
                if (newMode != NotificationManager.INTERRUPTION_FILTER_ALL) {
                    newMode = NotificationManager.INTERRUPTION_FILTER_ALARMS;
                }

                Log.d(TAG, "Attempting to set adjusted DND mode " + newMode);
                mNotificationManager.setInterruptionFilter(newMode);
            } else {
                Log.d(TAG, "App is not allowed to change Do Not Disturb mode without applying workaround");
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
