package com.example.wujianghua.androidscreenshot.screenshot.bean;


import java.util.ArrayList;
import java.util.List;

/**
 * <br> Description: todo(这里用一句话描述这个方法的作用)
 * <br> Author:      wujianghua
 * <br> Date:        2018/7/4 17:45
 */
public class LineInfo {
    private List<PointInfo> pointList;
    private LineType lineType;

    public enum LineType {
        NormalLine,
        MosaicLine
    }

    public LineInfo(LineType type) {
        pointList = new ArrayList<>();
        lineType = type;
    }

    public void addPoint(PointInfo point) {
        pointList.add(point);
    }

    public List<PointInfo> getPointList() {
        return pointList;
    }

    public LineType getLineType() {
        return lineType;
    }
}
