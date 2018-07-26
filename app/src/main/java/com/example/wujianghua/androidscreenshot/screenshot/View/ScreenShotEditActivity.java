package com.example.wujianghua.androidscreenshot.screenshot.View;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.example.wujianghua.androidscreenshot.R;
import com.example.wujianghua.androidscreenshot.UIUtil;
import com.example.wujianghua.androidscreenshot.screenshot.bean.LineInfo;
import com.example.wujianghua.androidscreenshot.screenshot.widget.PaintEditImageView;
import com.example.wujianghua.androidscreenshot.screenshotservice.constant.ScreenShotServiceConstant;

import java.lang.reflect.Method;


/**
 * <br> ClassName:   ScreenShotEditActivity
 * <br> Description:  截图编辑页
 * <br>
 * <br> Author:      wujianghua
 * <br> Date:        2018/7/4 18:37
 */
@Route(path = ScreenShotServiceConstant.SCREEN_SHOT_EDIT_ACTIVITY)
public class ScreenShotEditActivity extends AppCompatActivity implements View.OnClickListener {

    private PaintEditImageView mPaintEditImageView;
    private String mScreenImagePath;
    private TextView mTvLine;
    private TextView mTvMosaic;
    private int mImageHeight;
    private int mImageWidth;
    private final int FEEDBACK_REQUEST_CODE = 10;
    private LinearLayout mLlRootLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.operatingbiz_activity_screen_shot_edit);
        init();
        initView();
    }


    private void init() {
        mScreenImagePath = getIntent().getStringExtra("ARG1");
    }

    private void initView() {
        mLlRootLayout = findViewById(R.id.ll_root_layout);
        mPaintEditImageView = findViewById(R.id.paintEditImageView);
        mTvLine = findViewById(R.id.tv_line);
        mTvMosaic = findViewById(R.id.tv_mosaic);
        mTvMosaic.setOnClickListener(this);
        mTvLine.setOnClickListener(this);
        mTvLine.setSelected(true);
        findViewById(R.id.tv_withdraw).setOnClickListener(this);
        findViewById(R.id.tv_next).setOnClickListener(this);
        mLlRootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Point realScreenSize = getRealScreenSize();
                Rect rect = new Rect();
                mLlRootLayout.getLocalVisibleRect(rect);
                mImageHeight = rect.bottom - rect.top - UIUtil.dp2px(ScreenShotEditActivity.this, 90);
                mImageWidth = (int) (mImageHeight * 1.0f / realScreenSize.y * realScreenSize.x);
                mPaintEditImageView.getLayoutParams().height = mImageHeight;
                mPaintEditImageView.getLayoutParams().width = mImageWidth;
                if (!TextUtils.isEmpty(mScreenImagePath)) {
                    Glide.with(ScreenShotEditActivity.this).load(mScreenImagePath).into(mPaintEditImageView);
                }
                //移除监听
                if (Build.VERSION.SDK_INT >= 16) {
                    mLlRootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mLlRootLayout.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FEEDBACK_REQUEST_CODE && resultCode == RESULT_OK) {
            finish();
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_withdraw) {
            onRevert();
        } else if (id == R.id.tv_mosaic) {
            onDrawMosaic();
        } else if (id == R.id.tv_line) {
            onDrawLine();
        } else if (id == R.id.tv_next) {
            onNext();
        }
    }


    /**
     * <br> Description: 获取屏幕分辨率
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 15:35
     */
    private Point getRealScreenSize() {
        Point screenSize = null;
        try {
            screenSize = new Point();
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display defaultDisplay = windowManager.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                defaultDisplay.getRealSize(screenSize);
            } else {
                try {
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    screenSize.set(
                            (Integer) mGetRawW.invoke(defaultDisplay),
                            (Integer) mGetRawH.invoke(defaultDisplay)
                    );
                } catch (Exception e) {
                    screenSize.set(defaultDisplay.getWidth(), defaultDisplay.getHeight());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenSize;
    }

    private void onDrawLine() {
        mPaintEditImageView.setLineType(LineInfo.LineType.NormalLine);
        mTvLine.setSelected(true);
        mTvMosaic.setSelected(false);
    }

    private void onDrawMosaic() {
        mPaintEditImageView.setLineType(LineInfo.LineType.MosaicLine);
        mTvMosaic.setSelected(true);
        mTvLine.setSelected(false);
    }

    private void onRevert() {
        mPaintEditImageView.withDrawLastLine();
    }

    private void onNext() {
        mPaintEditImageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = mPaintEditImageView.getDrawingCache();
        /*保存图片，在setDrawingCacheEnabled(false)以后该图片会被释放掉*/
        /*mScreenShotPresenter.saveEditScreenImage(StorageUtils.getCacheDirectory(ScreenShotEditActivity.this).getPath(),
                "screenFile_" + System.currentTimeMillis() + ".png", Bitmap.createBitmap(bitmap));*/
        mPaintEditImageView.setDrawingCacheEnabled(false);
    }
}
