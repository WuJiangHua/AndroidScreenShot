package com.example.wujianghua.androidscreenshot;

import android.content.Context;

public class UIUtil {
    /**
     * <br> Description: dp转px数值
     * <br> Author:      zhangweiqiang
     * <br> Date:        2017/8/2 11:26
     *
     * @param dp        要转换的dp值
     * @return 返回转换后的px值
     */
    public static int dp2px(Context context,float dp) {
        float scale = context.getResources()
                .getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


}
