package com.cxwl.monitor;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by William on 2018/8/7.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MonitorService extends JobService {
    private String TAG = "";
    private Timer activityTimer;
    private ActivityManager activityManager;
    private Context mContext;
    private static final String PACKAGE_NAME = "com.cxwl.hurry.newdoorlock";
    private String Monitor_Broadcard = "com.androidex.lockaxial.MONITOR_ACTION";
    private boolean isPullTime = false;

    private int count = 0;
    private int kJobId = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x01) {
//                mContext.sendBroadcast(new Intent(Monitor_Broadcard));
//                mHandler.sendEmptyMessageDelayed(0x01, 1 * 1000);
            }
        }
    };

    private Runnable startMain = new Runnable() {
        @Override
        public void run() {
            try {
                showMsg("发送打开指令" + count);
                Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MonitorService.this.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        Log.i("xiao_", "监控服务初始化" + myFmt.format(new Date()));
        TAG = UUID.randomUUID().toString();
        mContext = this;

//        mHandler.sendEmptyMessage(0x01);
        initCheckTopActivity();
    }

    private void initCheckTopActivity() {
        if (activityTimer != null) {
            activityTimer.cancel();
            activityTimer = null;
        }
        activityTimer = new Timer();
        activityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (activityManager == null) {
                    activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                }
                ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
                if (!cn.getPackageName().equals(PACKAGE_NAME)) {
                    showMsg("未打开门禁" + count);
                    if (!isPullTime) {
                        showMsg("倒计时进入门禁：15s");
                        mHandler.postDelayed(startMain, 15 * 1000);
                        isPullTime = true;
                    }
                } else {
                    showMsg("已经打开门禁" + count);
                    mHandler.removeCallbacks(startMain);
                    isPullTime = false;
                }
            }
        }, 0, 2 * 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (activityTimer != null) {
            activityTimer.cancel();
            activityTimer = null;
        }
        mHandler.removeMessages(0x01);
    }

    private void showMsg(String msg) {
        Log.i("Monitor_" + TAG, msg);
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i("Monitor_", "执行了onStartJob方法");
//        boolean isLocalServiceWork = isServiceWork(this, "com.example.myapplication.service.MyJobService");
        boolean isRemoteServiceWork = isServiceWork(this, "com.cxwl.monitor.MonitorService");
//        if(!isLocalServiceWork||!isRemoteServiceWork){
        if (!isRemoteServiceWork) {
//            this.startService(new Intent(this,MyJobService.class));
            this.startService(new Intent(this, MonitorService.class));
//            Toast.makeText(this, "进程启动", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("Monitor_", "执行了onStopJob方法");
        scheduleJob(getJobInfo());
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Monitor_", "jobService启动");
        scheduleJob(getJobInfo());

        return START_NOT_STICKY;
    }

    //将任务作业发送到作业调度中去
    public void scheduleJob(JobInfo t) {
        Log.i("Monitor_", "调度job");
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(t);
    }

    public JobInfo getJobInfo() {
        JobInfo.Builder builder = new JobInfo.Builder(kJobId++, new ComponentName(this, MonitorService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        //间隔100毫秒
        builder.setPeriodic(100);
        return builder.build();
    }

    // 判断服务是否正在运行
    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
}
