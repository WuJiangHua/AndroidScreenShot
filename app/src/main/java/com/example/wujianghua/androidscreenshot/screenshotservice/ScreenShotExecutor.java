package com.example.wujianghua.androidscreenshot.screenshotservice;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.example.wujianghua.androidscreenshot.R;
import com.example.wujianghua.androidscreenshot.UIUtil;
import com.example.wujianghua.androidscreenshot.screenshotservice.constant.ScreenShotServiceConstant;
import com.example.wujianghua.androidscreenshot.screenshotservice.service.IScreenShotService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * <br> ClassName:   ScreenShotExecutor
 * <br> Description:  截屏监听管理
 * <br>
 * <br> Author:      wujianghua
 * <br> Date:        2018/7/2 10:37
 */
@Route(path = ScreenShotServiceConstant.SCREEN_SHOT_SERVICE_PATH)
public class ScreenShotExecutor implements IScreenShotService {
    private Context mContext;
    private long mStartListenTime;
    //内部存储器内容观察者
    private MediaContentObserver mInternalObserver;
    //外部存储器内容观察者
    private MediaContentObserver mExternalObserver;
    //运行在 UI 线程的 Handler, 用于运行监听器回调
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());
    private Disposable mShowDialogDisposable;
    private AlertDialog mScreenShotDialog;

    @Override
    public void init(Context context) {
        assertInMainThread();
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        mContext = context.getApplicationContext();
        initContentObserver();
    }

    private void initContentObserver() {
        // 创建内容观察者
        mInternalObserver = new MediaContentObserver(mContext, MediaStore.Images.Media.INTERNAL_CONTENT_URI, mUiHandler);
        mExternalObserver = new MediaContentObserver(mContext, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mUiHandler);
        MediaContentObserver.OnScreenShotListener onScreenShotListener = new MediaContentObserver.OnScreenShotListener() {
            @Override
            public void onShot(final String imagePath, long dateTaken) {
                if (dateTaken < mStartListenTime) {
                    return;
                }
                //延时显示，防止有些机型读取不到图片
                mShowDialogDisposable = Observable.timer(1000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                Intent intent = new Intent("ScreenShotEvent");
                                intent.putExtra("ARG1", imagePath);
                                mContext.sendBroadcast(intent);
                            }
                        });
            }
        };
        mInternalObserver.setListener(onScreenShotListener);
        mExternalObserver.setListener(onScreenShotListener);
    }

    /**
     * <br> Description: 提示主线程执行
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/2 10:34
     */
    private static void assertInMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            String methodMsg = null;
            if (elements != null && elements.length >= 4) {
                methodMsg = elements[3].toString();
            }
            throw new IllegalStateException("Call the method must be in main thread: " + methodMsg);
        }
    }

    /**
     * <br> Description: 启动监听
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/2 10:31
     */
    @Override
    public void startListener() {
        if (mInternalObserver == null || mExternalObserver == null) {
            initContentObserver();
        }
        assertInMainThread();
        //sHasCallbackPaths.clear();
        // 记录开始监听的时间戳
        mStartListenTime = System.currentTimeMillis();

        // 注册内容观察者
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                false,
                mInternalObserver
        );
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                mExternalObserver
        );
    }

    /**
     * <br> Description: 停止监听
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/11 15:35
     */
    @Override
    public void stopListener() {
        assertInMainThread();
        // 注销内容观察者
        if (mInternalObserver != null) {
            try {
                mContext.getContentResolver().unregisterContentObserver(mInternalObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mExternalObserver != null) {
            try {
                mContext.getContentResolver().unregisterContentObserver(mExternalObserver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 清空数据
        mStartListenTime = 0;
        clear();
    }

    /**
     * <br> Description: 退出清除
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/9 11:01
     */
    private void clear() {
        if (mShowDialogDisposable != null && !mShowDialogDisposable.isDisposed()) {
            mShowDialogDisposable.dispose();
        }
        if (mInternalObserver != null) {
            mInternalObserver.setListener(null);
            mInternalObserver = null;
        }
        if (mExternalObserver != null) {
            mExternalObserver.setListener(null);
            mExternalObserver = null;
        }
    }

    @Override
    public void showDialog(Context context, String imagePath, boolean isVisible) {
        if (!isVisible) {
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasksInfo = mActivityManager.getRunningTasks(1);
            if (!tasksInfo.isEmpty()) {
                // 兼容系统截屏弹窗是activity 的情况：诺基亚某一机型
                if (!context.getClass().getName().endsWith(tasksInfo.get(0).topActivity.getClassName())) {
                    return;
                }
            }
        }

        if (mScreenShotDialog != null && mScreenShotDialog.isShowing()) {
            mScreenShotDialog.dismiss();
        }
        mScreenShotDialog = showScreenShotDialog(context, imagePath);
    }

    @Override
    public void dismissDialog() {
        if (mScreenShotDialog != null && mScreenShotDialog.isShowing()) {
            mScreenShotDialog.dismiss();
        }
        mScreenShotDialog = null;
    }

    private void trackButtonClick(Context context) {
    }

    /**
     * <br> Description: 显示截屏弹窗
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 16:13
     */
    private AlertDialog showScreenShotDialog(final Context context, final String path) {
        final AlertDialog mDialog = new AlertDialog.Builder(context, R.style.screen_shot_dialogStyle).create();
        View dialogView = View.inflate(context, R.layout.dialog_screen_shot_layout, null);
        ImageView ivScreen = dialogView.findViewById(R.id.iv_screen_shot);
        Glide.with(context).load(path).into(ivScreen);
        dialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackButtonClick(context);
                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
                ARouter.getInstance().build(ScreenShotServiceConstant.SCREEN_SHOT_EDIT_ACTIVITY).withString("ARG1", path).navigation();
            }
        });

        mDialog.show();
        mDialog.setContentView(dialogView);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.x = UIUtil.dp2px(context,15);
        lp.y = UIUtil.dp2px(context,224);
        dialogWindow.setAttributes(lp);
        //3秒后关闭弹窗
        final Disposable dismissDisposable = Observable.timer(5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (mDialog != null && mDialog.isShowing())
                            mDialog.dismiss();
                    }
                });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dismissDisposable != null && !dismissDisposable.isDisposed()) {
                    dismissDisposable.dispose();
                }
            }
        });
        return mDialog;
    }
}
