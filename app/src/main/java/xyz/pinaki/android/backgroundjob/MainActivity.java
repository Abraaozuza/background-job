package xyz.pinaki.android.backgroundjob;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * https://code.tutsplus.com/tutorials/using-the-jobscheduler-api-on-android-lollipop--cms-23562
 * https://medium.com/google-developers/scheduling-jobs-like-a-pro-with-jobscheduler-286ef8510129
 * http://www.vogella.com/tutorials/AndroidTaskScheduling/article.html
 * https://developer.android.com/topic/performance/scheduling.html#js
 * https://github.com/romannurik/muzei
 * https://blog.hypertrack.com/2016/12/01/scheduling-tasks-in-android-made-easy/
 * https://github.com/evernote/android-job
 * http://www.vogella.com/tutorials/AndroidServices/article.html
 */
public class MainActivity extends AppCompatActivity {
    public static final String MESSENGER_INTENT_KEY = "MESSENGER_INTENT_KEY";
    private static final String TAG = "pinaki-" + MainActivity.class.getSimpleName();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i(TAG, "what: " + msg.what + ", obj: " + msg.obj + ", Thread:" + Thread.currentThread().getId());
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent startServiceIntent = new Intent(this, BackgroundJobService.class);
        Messenger messengerIncoming = new Messenger(handler);
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
        startService(startServiceIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllJobs();
    }

    @Override
    public void onResume() {
        super.onResume();
        scheduleJob();
    }

    public void scheduleJob() {
        int jobId = 1;
        ComponentName mServiceComponent = new ComponentName(getPackageName(), BackgroundJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent);
        builder.setMinimumLatency(1000); // min latency in millisec
        builder.setOverrideDeadline(10 * 1000); // max 10 sec in deadline
        // set periodic does not start untl after 5 mins ??
//        builder.setPeriodic(3000L);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // needs unmetered wifi network
        Log.i(TAG, "Scheduling Job");
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    public void cancelAllJobs() {
        Log.i(TAG, "Cancelling Jobs");
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, BackgroundJobService.class));
        super.onStop();
    }
}
