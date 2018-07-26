package com.example.wujianghua.androidscreenshot.screenshot.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.wujianghua.androidscreenshot.screenshot.bean.LineInfo;
import com.example.wujianghua.androidscreenshot.screenshot.bean.PointInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * <br> ClassName:   PaintEditImageView
 * <br> Description:  图片编辑控件
 * <br>
 * <br> Author:      wujianghua
 * <br> Date:        2018/7/2 11:57
 */
public class PaintEditImageView extends android.support.v7.widget.AppCompatImageView {
    private Drawable drawable;
    private Bitmap bitmap;

    private List<LineInfo> lineList; // 线条列表
    private LineInfo currentLine; // 当前线条
    private LineInfo.LineType currentLineType = LineInfo.LineType.NormalLine; // 当前线条类型
    private Paint normalPaint; //线条画笔
    private Paint mosaicPaint; //马赛克
    private static final float NORMAL_LINE_STROKE = 8.0f; //线条粗细
    private static final int MOSAIC_CELL_LENGTH = 30; // 马赛克每个大小30*30像素，共三行
    private boolean mosaics[][]; // 马赛克绘制中用于记录某个马赛克格子的数值是否计算过
    private int mosaicRows; // 马赛克行数
    private int mosaicColumns; // 马赛克列数


    public PaintEditImageView(Context context) {
        this(context, null);
    }

    public PaintEditImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintEditImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lineList = new ArrayList<>();
        normalPaint = new Paint();
        mosaicPaint = new Paint();
        normalPaint.setColor(Color.RED);
        normalPaint.setStrokeWidth(NORMAL_LINE_STROKE);
    }


    /**
     * <br> Description: 设置线条类型
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 17:06
     */
    public void setLineType(LineInfo.LineType type) {
        currentLineType = type;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentLine = new LineInfo(currentLineType);
                currentLine.addPoint(new PointInfo(xPos, yPos));
                lineList.add(currentLine);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                currentLine.addPoint(new PointInfo(xPos, yPos));
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                currentLine.addPoint(new PointInfo(xPos, yPos));
                invalidate();
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mosaicRows; i++) {
            for (int j = 0; j < mosaicColumns; j++) {
                mosaics[i][j] = false;
            }
        }

        for (LineInfo lineinfo : lineList) {
            if (lineinfo.getLineType() == LineInfo.LineType.NormalLine) {
                drawNormalLine(canvas, lineinfo);
            } else if (lineinfo.getLineType() == LineInfo.LineType.MosaicLine) {
                drawMosaicLine(canvas, lineinfo);
            }
        }
    }

    /**
     * <br> Description: 绘制马赛克线条
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 17:06
     */
    private void drawMosaicLine(Canvas canvas, LineInfo lineinfo) {
        if (null == bitmap) {
            initMosaic();
        }

        if (null == bitmap) {
            return;
        }

        for (PointInfo pointInfo : lineinfo.getPointList()) {
            // 对每一个点，填充所在的小格子以及上下两个格子（如果有上下格子）
            int currentRow = (int) ((pointInfo.y - 1) / MOSAIC_CELL_LENGTH);
            int currentCol = (int) ((pointInfo.x - 1) / MOSAIC_CELL_LENGTH);

            fillMosaicCell(canvas, currentRow, currentCol);
            fillMosaicCell(canvas, currentRow - 1, currentCol);
            fillMosaicCell(canvas, currentRow + 1, currentCol);
        }
    }

    /**
     * <br> Description: 初始化马赛克绘制相关
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 17:07
     */
    private void initMosaic() {
        drawable = getDrawable();

        try {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return;
        }

        mosaicColumns = (int) Math.ceil(bitmap.getWidth() / MOSAIC_CELL_LENGTH);
        mosaicRows = (int) Math.ceil(bitmap.getHeight() / MOSAIC_CELL_LENGTH);
        mosaics = new boolean[mosaicRows][mosaicColumns];
    }

    /**
     * <br> Description: 填充一个马赛克格子
     *
     * @param cavas
     * @param row   马赛克格子行
     * @param col   马赛克格子列
     *              <br> Author:      wujianghua
     *              <br> Date:        2018/7/4 17:07
     */
    private void fillMosaicCell(Canvas cavas, int row, int col) {
        if (row >= 0 && row < mosaicRows && col >= 0 && col < mosaicColumns) {
            if (!mosaics[row][col]) {

                mosaicPaint.setColor(bitmap.getPixel(col * MOSAIC_CELL_LENGTH, row * MOSAIC_CELL_LENGTH));
                cavas.drawRect(col * MOSAIC_CELL_LENGTH, row * MOSAIC_CELL_LENGTH,
                        (col + 1) * MOSAIC_CELL_LENGTH, (row + 1) * MOSAIC_CELL_LENGTH, mosaicPaint);
                mosaics[row][col] = true;
            }
        }
    }

    /**
     * <br> Description: 绘制普通线条
     *
     * @param canvas
     * @param lineinfo <br> Author:      wujianghua
     *                 <br> Date:        2018/7/4 17:07
     */
    private void drawNormalLine(Canvas canvas, LineInfo lineinfo) {
        if (lineinfo.getPointList().size() <= 1) {
            return;
        }

        for (int i = 0; i < lineinfo.getPointList().size() - 1; i++) {
            PointInfo startPoint = lineinfo.getPointList().get(i);
            PointInfo endPoint = lineinfo.getPointList().get(i + 1);

            canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, normalPaint);
        }
    }

    /**
     * <br> Description: 删除最后添加的线
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 17:07
     */
    public void withDrawLastLine() {
        if (lineList.size() > 0) {
            lineList.remove(lineList.size() - 1);
            invalidate();
        }
    }

    /**
     * <br> Description: 判断是否可以继续撤销
     * <br> Author:      wujianghua
     * <br> Date:        2018/7/4 17:08
     */
    public boolean canStillWithdraw() {
        return lineList.size() > 0;
    }
}
