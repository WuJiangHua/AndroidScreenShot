package com.example.wujianghua.androidscreenshot.screenshotservice.service;

import android.content.Context;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * <br> ClassName:   IScreenShotService
 * <br> Description: 截屏服务
 * <br>
 * <br> Author:      wujianghua
 * <br> Date:        2018/7/11 10:47
 */
public interface IScreenShotService extends IProvider {
    /**
     *<br> Description: 开始监听
     *<br> Author:      wujianghua
     *<br> Date:        2018/7/11 10:48
     */
    void startListener();
    /**
     *<br> Description: 停止监听
     *<br> Author:      wujianghua
     *<br> Date:        2018/7/11 10:48
     */
    void stopListener();
    /**
     *<br> Description: 显示弹窗
     *<br> Author:      wujianghua
     *<br> Date:        2018/7/11 11:38
     */
    void showDialog(Context context, String path, boolean isVisible);
    /**
     *<br> Description: 关闭弹窗
     *<br> Author:      wujianghua
     *<br> Date:        2018/7/11 11:38
     */
    void dismissDialog();
}
