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

import android.service.notification.NotificationListenerService;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A {@link NotificationListenerService} service needed to listen for DND state changes.
 */
public class DNDSyncNotificationService extends NotificationListenerService {
    private static final String TAG = "DNDSyncNotificationService";
    private static final String DND_SYNC_MODE = "/wear-dnd-sync";
    private static final String DND_SYNC_CAPABILITY = "wear-dnd-sync";

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "Service listener connected to notification manager");
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "Service listener disconnected from the notification manager");
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        super.onInterruptionFilterChanged(interruptionFilter);
        Log.d(TAG, "Interruption filter changed to " + interruptionFilter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendDNDSyncMessage(interruptionFilter);
            }
        }).start();
    }

    private void sendDNDSyncMessage(int dndMode) {
        Log.i(TAG, "Syncing new DND mode " + dndMode +" to nearby paired devices");

        // Search for compatible devices
        CapabilityInfo capabilityInfo;
        try {
            capabilityInfo = Tasks.await(Wearable.getCapabilityClient(this)
                    .getCapability(DND_SYNC_CAPABILITY, CapabilityClient.FILTER_REACHABLE));
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException when searching for compatible reachable devices");
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            Log.e(TAG, "ExecutionException when searching for compatible reachable devices");
            e.printStackTrace();
            return;
        }

        // Send the DND Sync message to all reachable devices with the app installed,
        // i.e. an app with a matching capability string defined.
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        if (connectedNodes.isEmpty()) {
            Log.i(TAG, "No device with DND sync capability found");
        } else {
            for (Node node : connectedNodes) {
                if (node.isNearby()) {
                    Wearable.getMessageClient(this)
                            .sendMessage(node.getId(), DND_SYNC_MODE, String.valueOf(dndMode).getBytes())
                            .addOnSuccessListener(new OnSuccessListener<Integer>() {
                                @Override
                                public void onSuccess(Integer integer) {
                                    Log.d(TAG, "Successfully sent sync message to device with id: " + node.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Failed to send sync message to device with id: " + node.getId());
                                }
                            });
                }
            }
        }
    }
}
