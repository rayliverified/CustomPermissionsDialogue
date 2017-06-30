package stream.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MessagingReceiver extends BroadcastReceiver {
    private final String TAG = "MessagingReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        abortBroadcast();
    }
}
