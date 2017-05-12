package se.blunden.donotdisturbsync;

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

            Log.d(TAG, "Apps are not allowed to change Do Not Disturb mode on Android Wear at present");
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
