
package com.bj4.dustcleaner.fragments;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bj4.dustcleaner.R;

public class MainFragment extends Fragment {

    private static final String TAG = "QQQQ";

    private static final boolean DEBUG = true;

    private Button mClean;

    private TextView mInfo;

    private Handler mHandler = new Handler();

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mInfo = (TextView)rootView.findViewById(R.id.info);
        mClean = (Button)rootView.findViewById(R.id.clean);
        mClean.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        clean();
                    }
                }).start();
            }
        });

        return rootView;
    }

    public synchronized void clean() {
        ActivityManager am = (ActivityManager)getActivity().getSystemService(
                Activity.ACTIVITY_SERVICE);
        String globalInfo = "";
        List<RunningAppProcessInfo> runningProcessList = am.getRunningAppProcesses();
        List<RunningServiceInfo> runningServiceList = am.getRunningServices(200);
        List<RunningTaskInfo> runningTaskList = am.getRunningTasks(200);
        MemoryInfo memInfo = new MemoryInfo();
        am.getMemoryInfo(memInfo);
        int totalTask = runningProcessList.size() + runningServiceList.size()
                + runningTaskList.size();
        long totalAvailableMemory = memInfo.totalMem / (1024 * 1024);
        globalInfo += "total task: " + totalTask + ", available memory: " + totalAvailableMemory
                + "\n";
        String processInfo = "";
        String resultInfo = "";
        for (RunningAppProcessInfo info : runningProcessList) {
            android.os.Debug.MemoryInfo[] mii = am.getProcessMemoryInfo(new int[] {
                info.pid
            });
            processInfo = "package: " + info.processName + ", \nprocess memory: "
                    + mii[0].getTotalPss();
            am.killBackgroundProcesses(info.processName);
            am.getMemoryInfo(memInfo);
            resultInfo = "\nAvaliable size: " + (memInfo.availMem / (1024 * 1024));
            final String result = globalInfo + processInfo + resultInfo;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mInfo.setText(result);
                }
            });
        }

        for (RunningServiceInfo info : runningServiceList) {
            android.os.Debug.MemoryInfo[] mii = am.getProcessMemoryInfo(new int[] {
                info.pid
            });
            processInfo = "package: " + info.process + ", \nprocess memory: "
                    + mii[0].getTotalPss();
            am.getMemoryInfo(memInfo);
            am.killBackgroundProcesses(info.process);
            resultInfo = "\nAvaliable size: " + (memInfo.availMem / (1024 * 1024));
            final String result = globalInfo + processInfo + resultInfo;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mInfo.setText(result);
                }
            });
        }

        for (RunningTaskInfo info : runningTaskList) {
            processInfo = "package: " + info.topActivity.getPackageName();
            am.getMemoryInfo(memInfo);
            am.killBackgroundProcesses(info.topActivity.getPackageName());
            resultInfo = "\nAvaliable size: " + (memInfo.availMem / (1024 * 1024));
            final String result = globalInfo + processInfo + resultInfo;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mInfo.setText(result);
                }
            });
        }
    }
}
