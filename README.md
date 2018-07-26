# 安卓手机全局截屏监听并弹窗，可对截图进行编辑

#使用说明


    1、开启截屏监听：注意:监听最好放好全局，即进入应用的时候去监听
    public void startListener() {
        IScreenShotService screenShotService = (IScreenShotService) ARouter.getInstance()
                .build(ScreenShotServiceConstant.SCREEN_SHOT_SERVICE_PATH)
                .navigation();
        if (screenShotService != null) {
            screenShotService.startListener();
        }
    }

    2、移除截屏监听，注意:移除监听最好放好全局，即退出应用的时候采取移除
    public void stopListener() {
        IScreenShotService screenShotService = (IScreenShotService) ARouter.getInstance()
                .build(ScreenShotServiceConstant.SCREEN_SHOT_SERVICE_PATH)
                .navigation();
        if (screenShotService != null) {
            screenShotService.stopListener();
        }
    }
   
    3、baseActivity里进行截屏广播监听和弹窗
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
    
    
