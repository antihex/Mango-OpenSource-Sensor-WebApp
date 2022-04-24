package com.serotonin.mango.vo.report;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.serotonin.util.SerializationHelper;

public class ReportPointVO implements Serializable {

    private int pointId;
    private String colour;
    private String xLabel;
    private String yLabel;
    private String title;
    //private ChartType type;
    private String type;
    private float yReference;
    private boolean consolidatedChart;

    public int getPointId() {
        return pointId;
    }

    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    // public void setChartType(ChartType newType) {
    //     this.type = newType;
    // }

    // public ChartType getChartType() {
    //     return this.type;
    // }

    public void setChartType(String type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChartType() {
        return this.type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newLabel) {
        this.title = newLabel;
    }

    public String getXLabel() {
        return xLabel;
    }

    public void setxLabel(String newLabel) {
        this.xLabel = newLabel;
    }

    public String getYLabel() {
        return yLabel;
    }

    public void setyLabel(String newLabel) {
        this.yLabel = newLabel;
    }

    public float getYReference() {
        return yReference;
    }

    public void setyReference(float newReference) {
        this.yReference = newReference;
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

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        out.writeInt(pointId);
        SerializationHelper.writeSafeUTF(out, colour);
        out.writeBoolean(consolidatedChart);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            pointId = in.readInt();
            colour = SerializationHelper.readSafeUTF(in);
            consolidatedChart = true;
        }
        else if (ver == 2) {
            pointId = in.readInt();
            colour = SerializationHelper.readSafeUTF(in);
            consolidatedChart = in.readBoolean();
        }
    }
}
