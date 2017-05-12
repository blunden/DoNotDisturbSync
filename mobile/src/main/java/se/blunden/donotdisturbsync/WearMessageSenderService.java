package se.blunden.donotdisturbsync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WearMessageSenderService extends IntentService {
    private static final String TAG = "DndMessageSender";
    private static final String DND_SYNC_PREFIX = "/wear-dnd-sync";
    private static final long CONNECTION_TIME_OUT = 10;

    static final String ACTION_SEND_MESSAGE = "se.blunden.donotdisturbsync.action.SEND_MESSAGE";
    static final String EXTRA_RINGER_MODE = "se.blunden.donotdisturbsync.extra.RINGER_MODE";
    static final String EXTRA_DND_MODE = "se.blunden.donotdisturbsync.extra.DND_MODE";

    GoogleApiClient mGoogleApiClient;

    public WearMessageSenderService() {
        super("WearMessageSenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_MESSAGE.equals(action)) {
                final String deviceRingerMode = intent.getStringExtra(EXTRA_RINGER_MODE);
                final String deviceDndMode = intent.getStringExtra(EXTRA_DND_MODE);

                if (deviceRingerMode != null) {
                    handleActionSendMessage(deviceRingerMode);
                } else if (deviceDndMode != null) {
                    handleActionSendMessage(deviceDndMode);
                }
            }
        }
    }

    /**
     * Handle the message sending action in the provided background thread.
     */
    private void handleActionSendMessage(String mode) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        if (!(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())) {
            mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT, TimeUnit.SECONDS);
        }

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        // Send the Do Not Disturb mode message to all devices (nodes)
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), DND_SYNC_PREFIX, mode.getBytes()).await();

            if (!result.getStatus().isSuccess()){
                Log.e(TAG, "Failed to send message to " + node.getDisplayName());
            } else {
                Log.i(TAG, "Successfully sent message " + mode + " to " + node.getDisplayName());
            }
        }

        //mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
}
