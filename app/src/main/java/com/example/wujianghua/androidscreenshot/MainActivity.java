package com.example.wujianghua.androidscreenshot;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.example.wujianghua.androidscreenshot.screenshotservice.constant.ScreenShotServiceConstant;
import com.example.wujianghua.androidscreenshot.screenshotservice.service.IScreenShotService;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mScreenShotReceiver;
    private boolean mIsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ARouter.init(this.getApplication());
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            startListener();
        }
        registerScreenShotReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListener();
        }
    }

    /***开启截屏监听：注意:监听最好放好全局，这里只是方便测试用，即进入应用的时候去监听***/
    public void startListener() {
        IScreenShotService screenShotService = (IScreenShotService) ARouter.getInstance()
                .build(ScreenShotServiceConstant.SCREEN_SHOT_SERVICE_PATH)
                .navigation();
        if (screenShotService != null) {
            screenShotService.startListener();
        }
    }

    /***移除截屏监听，注意:移除监听最好放好全局，这里只是方便测试用，即退出应用的时候采取移除***/
    public void stopListener() {
        IScreenShotService screenShotService = (IScreenShotService) ARouter.getInstance()
                .build(ScreenShotServiceConstant.SCREEN_SHOT_SERVICE_PATH)
                .navigation();
        if (screenShotService != null) {
            screenShotService.stopListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScreenShotReceiver != null) {
            unregisterReceiver(mScreenShotReceiver);
        }
        stopListener();
    }

    /**
     * <br> Description: 注册截屏广播
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/11 18:36
     */
    private void registerScreenShotReceiver() {
        mScreenShotReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String imagePath = intent.getStringExtra("ARG1");
                if (!TextUtils.isEmpty(imagePath)) {
                    IScreenShotService screenShotService = (IScreenShotService) ARouter.getInstance()
                            .build(ScreenShotServiceConstant.SCREEN_SHOT_SERVICE_PATH)
                            .navigation();
                    if (screenShotService != null && !isFinishing()) {
                        screenShotService.showDialog(MainActivity.this, imagePath, mIsVisible);
                    }
                }
            }
        };
        IntentFilter screenShotFilter = new IntentFilter();
        screenShotFilter.addAction("ScreenShotEvent");
        registerReceiver(mScreenShotReceiver, screenShotFilter);
    }

}
