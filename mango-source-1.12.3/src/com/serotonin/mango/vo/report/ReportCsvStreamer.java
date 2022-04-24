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

import java.io.PrintWriter;
import java.util.ResourceBundle;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.serotonin.mango.view.export.CsvWriter;
import com.serotonin.mango.view.text.TextRenderer;
import com.serotonin.web.i18n.I18NUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Matthew Lohbihler
 */

/**
 * @author2 (Horizontal CSV) Lucas Amana
 */

public class ReportCsvStreamer implements ReportDataStreamHandler {
    private final PrintWriter out;
    ResourceBundle bundle;

    // Working fields
    private TextRenderer textRenderer;
    private ArrayList<ArrayList<String>> reportData = new ArrayList<ArrayList<String>>();

    //variable to store the current point name
    String name;
    private int indexToAddPoint;//a new Point Name may be added on an nth column, with Time, Rendered, and Annotation corresponding to n + 1, 2, 3, 4

    //if not all sensors share the same point counts, then the rest of the data must be filled with null values
    //we must use this to prevent indexing out of bounds of shorter arrays
    int longestPointCount;

    private final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
    private final CsvWriter csvWriter = new CsvWriter();

    public ReportCsvStreamer(PrintWriter out, ResourceBundle bundle) {
        this.out = out;
        this.bundle = bundle;
    }

    //startPoint is called every time the current point being filled in changes to another sensor
    //  this never changes to an already recorded sensor
    public void startPoint(ReportPointInfo pointInfo) {
        name = pointInfo.getExtendedName();
        textRenderer = pointInfo.getTextRenderer();
        indexToAddPoint = reportData.size();//indexToAddPoint tells where to fill data, this one corresponds to the newly created columns
        addNewColumnSet();//add 5 new columns for the newly introduced point name
        
    }

    //point data is used to append Name, Time, Value, Rendered, and Annotation values to the currently selected point by ReportDao
    public void pointData(ReportDataValue rdv) {
        //this fills in every Name, Time, Value, Rendered, and Annotation columns
        //  this is filled in at the appropriate place according to when name / indexToAddPoint was generated
        //  name / indexToAddPoint are generated in startPoint
        reportData.get(indexToAddPoint).add(name);
        reportData.get(indexToAddPoint + 1).add(dtf.print(new DateTime(rdv.getTime())));
        if (rdv.getValue() == null){
            reportData.get(indexToAddPoint + 2).add(null);
            reportData.get(indexToAddPoint + 3).add(null);
        }
        else {
            reportData.get(indexToAddPoint + 2).add(rdv.getValue().toString());
            reportData.get(indexToAddPoint + 3).add(textRenderer.getText(rdv.getValue(), TextRenderer.HINT_FULL));
        }
        reportData.get(indexToAddPoint + 4).add(rdv.getAnnotation());
    }

    public void addNewColumnSet()
    {
        //adds the 5 new columns: Name, Time, Value, Rendered, and Annotation columns
        reportData.add(new ArrayList<String>(Arrays.asList(I18NUtils.getMessage(bundle, "reports.pointName"))));
        reportData.add(new ArrayList<String>(Arrays.asList(I18NUtils.getMessage(bundle, "common.time"))));
        reportData.add(new ArrayList<String>(Arrays.asList(I18NUtils.getMessage(bundle, "common.value"))));
        reportData.add(new ArrayList<String>(Arrays.asList(I18NUtils.getMessage(bundle, "reports.rendered"))));
        reportData.add(new ArrayList<String>(Arrays.asList(I18NUtils.getMessage(bundle, "common.annotation"))));
    }

    public void printAllData()
    {
        //if report had notihg in it, this preserves the original behavior before this feature was implemented
        if(reportData.size() == 0)
        {
            addNewColumnSet();
        }

        //this allows us to loop through all the data
        // this is done by finding the highest data count
        findLongestCol();

        //printout row, used for appending all the rows to the csv file
        String[] row = new String[reportData.size()];
        for(int i = 0; i < longestPointCount; i++)
        {
            for(int j = 0; j < reportData.size(); j++)
            {
                //if we start exceeding existing data, then the printout row must be null there
                if(i < reportData.get(j).size())
                {
                    row[j] = reportData.get(j).get(i);
                }
                else
                {
                    row[j] = null;
                }
            }
            out.write(csvWriter.encodeRow(row));
        }
    }

    //used to find the highest number of data stored within the columns
    public void findLongestCol()
    {
        longestPointCount = 0;
        for(int i = 0; i < reportData.size(); i++)
        {
            if(reportData.get(i).size() > longestPointCount)
            {
                longestPointCount = reportData.get(i).size();
            }
        }
    }

    public void done() {
        printAllData();
        out.flush();
        out.close();
    }
}
