package xyz.pinaki.android.backgroundjob;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by pinaki on 8/5/17.
 */

public class BackgroundJobService extends JobService {
    private static final String TAG = "pinaki-" + BackgroundJobService.class.getSimpleName();
    HandlerThread t = new HandlerThread("BackgroundJobServiceThread");
    Messenger messengerToMain;
    @Override
    public void onCreate() {
        super.onCreate();
        t.start();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
        t.quit();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        messengerToMain = intent.getParcelableExtra(MainActivity.MESSENGER_INTENT_KEY);
        Log.i(TAG, "Service onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        // start a new thread ?
        sendMessageBack(params.getJobId(), "onStartJob start thread");
        Handler h = new Handler(t.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                // handle the message
                Log.i(TAG, "Thread ID: " +  Thread.currentThread().getId());
                sendMessageBack(params.getJobId(), "onStartJob finished job in thread");
                jobFinished(params, true);
                return false;
            }
        });
        Message m =  Message.obtain();
        h.sendMessage(m);
        return true;
    }

    void sendMessageBack(int jobId, String data) {
        if (messengerToMain == null) {
            Log.i(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }
        Message m = Message.obtain();
        m.what = jobId;
        m.obj = data;
        try {
            messengerToMain.send(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessageBack(params.getJobId(), "onStopJob");
        t.quitSafely();
        return false;
    }
}
