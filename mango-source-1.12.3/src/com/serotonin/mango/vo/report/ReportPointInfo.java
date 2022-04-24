/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.vo.report;

import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.view.text.TextRenderer;

/**
 * @author Matthew Lohbihler
 */
public class ReportPointInfo {
    // public enum ChartType {
    //         LINE,
    //         SCATTER
    //     }

    private int reportPointId;
    private String deviceName;
    private String pointName;
    private int dataType;
    private MangoValue startValue;
    private TextRenderer textRenderer;
    private String colour;
    private boolean consolidatedChart;

    private String xLabel;
    private String yLabel;
    private String title;
    // private ChartType type;
    private String type;
    private double yReference;

    public void setChartType(String newType) {
        this.type = newType;
    }

    public void setType(String newType) {
        this.type = newType;
    }

    public String getChartType() {
        return this.type;
    }

    // public void setChartType(ChartType newType) {
    //     this.type = newType;
    // }

    // public ChartType getChartType() {
    //     return this.type;
    // }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newLabel) {
        this.title = newLabel;
    }

    public String getXLabel() {
        return xLabel;
    }

    public void setXLabel(String newLabel) {
        this.xLabel = newLabel;
    }

    public String getYLabel() {
        return yLabel;
    }

    public void setYLabel(String newLabel) {
        this.yLabel = newLabel;
    }

    public double getYReference() {
        return yReference;
    }

    public void setYReference(double newReference) {
        this.yReference = newReference;
    }

    public String getExtendedName() {
        return deviceName + " - " + pointName;
    }

    public int getReportPointId() {
        return reportPointId;
    }

    public void setReportPointId(int reportPointId) {
        this.reportPointId = reportPointId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public MangoValue getStartValue() {
        return startValue;
    }

    public void setStartValue(MangoValue startValue) {
        this.startValue = startValue;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public void setTextRenderer(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public boolean isConsolidatedChart() {
        return consolidatedChart;
    }

    public void setConsolidatedChart(boolean consolidatedChart) {
        this.consolidatedChart = consolidatedChart;
    }
}
