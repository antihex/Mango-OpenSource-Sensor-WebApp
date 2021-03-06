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
package com.serotonin.mango.rt.dataSource.http;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.rt.dataImage.SetPointSource;
import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.DataSourceRT;
import com.serotonin.mango.rt.dataSource.DataSourceUtils;
import com.serotonin.mango.rt.dataSource.NoMatchException;
import com.serotonin.mango.rt.dataSource.PollingDataSource;
import com.serotonin.mango.vo.dataSource.http.HttpRetrieverDataSourceVO;
import com.serotonin.web.http.HttpUtils;
import com.serotonin.web.i18n.LocalizableException;
import com.serotonin.web.i18n.LocalizableMessage;
//DHOU 07/17/2013
import com.serotonin.mango.util.timeout.TimeoutTask;
import com.serotonin.timer.FixedRateTrigger;

/**
 * @author Matthew Lohbihler
 */
public class HttpRetrieverDataSourceRT extends PollingDataSource {
    private static final int READ_LIMIT = 1024 * 1024; // One MB

    public static final int DATA_RETRIEVAL_FAILURE_EVENT = 1;
    public static final int PARSE_EXCEPTION_EVENT = 2;

    private final HttpRetrieverDataSourceVO vo;

    public HttpRetrieverDataSourceRT(HttpRetrieverDataSourceVO vo) {
        super(vo);
        setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
        this.vo = vo;
    }

    @Override
    public void removeDataPoint(DataPointRT dataPoint) {
        returnToNormal(PARSE_EXCEPTION_EVENT, System.currentTimeMillis());
        super.removeDataPoint(dataPoint);
    }

    @Override
    public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source) {
        // no op
    }

    // DHOU 07/17/2013
    private static  long delay = 0;
    @Override
    public void beginPolling() {
System.out.println(vo.getUrl()+": connection is delayed for "+delay+" milli seconds.");
        timerTask = new TimeoutTask(new FixedRateTrigger(delay, pollingPeriodMillis), this);
         // okay, we are connecting to the QuickControl Panel via http, which has the IP 192.168.1.102, and is slow. 
         // so add 60 seconds of delay for the forthcoming one!
         String url = vo.getUrl();
         if (url.indexOf("http://192.168.1.102")!=-1) 
            delay += 60000;
    }

    @Override
    protected void doPoll(long time) {
        String data;
        try {
            data = getData(vo.getUrl(), vo.getTimeoutSeconds(), vo.getRetries());
        }
        catch (Exception e) {
            LocalizableMessage lm;
            if (e instanceof LocalizableException)
                lm = ((LocalizableException) e).getLocalizableMessage();
            else
                lm = new LocalizableMessage("event.httpRetriever.retrievalError", vo.getUrl(), e.getMessage());
            raiseEvent(DATA_RETRIEVAL_FAILURE_EVENT, time, true, lm);
            return;
        }

        // If we made it this far, everything is good.
        returnToNormal(DATA_RETRIEVAL_FAILURE_EVENT, time);

        // We have the data. Now run the regex.
        LocalizableMessage parseErrorMessage = null;
        for (DataPointRT dp : dataPoints) {
            HttpRetrieverPointLocatorRT locator = dp.getPointLocator();

            try {
                // Get the value
                MangoValue value = DataSourceUtils.getValue(locator.getValuePattern(), data, locator.getDataTypeId(),
                        locator.getBinary0Value(), dp.getVO().getTextRenderer(), locator.getValueFormat(), dp.getVO()
                                .getName());

                // Get the time.
                long valueTime = DataSourceUtils.getValueTime(time, locator.getTimePattern(), data,
                        locator.getTimeFormat(), dp.getVO().getName());

                // Save the new value
                dp.updatePointValue(new PointValueTime(value, valueTime));
            }
            catch (NoMatchException e) {
                if (!locator.isIgnoreIfMissing()) {
                    if (parseErrorMessage == null)
                        parseErrorMessage = e.getLocalizableMessage();
                }
            }
            catch (LocalizableException e) {
                if (parseErrorMessage == null)
                    parseErrorMessage = e.getLocalizableMessage();
            }
        }

        if (parseErrorMessage != null)
            raiseEvent(PARSE_EXCEPTION_EVENT, time, false, parseErrorMessage);
        else
            returnToNormal(PARSE_EXCEPTION_EVENT, time);
    }

    public static String getData(String url, int timeoutSeconds, int retries) throws LocalizableException {
        // Try to get the data.
        String data;
        while (true) {
            HttpClient client = Common.getHttpClient(timeoutSeconds * 1000);
            GetMethod method = null;
            LocalizableMessage message;

            try {
                method = new GetMethod(url);
                int responseCode = client.executeMethod(method);
                if (responseCode == HttpStatus.SC_OK) {
// DHOU: fix 03/22/2013
                    //data = HttpUtils.readResponseBody(method, READ_LIMIT);
                    data = readResponseBody(method);
                    break;
                }
                message = new LocalizableMessage("event.http.response", url, responseCode);
            }
            catch (Exception e) {
                message = DataSourceRT.getExceptionMessage(e);
            }
            finally {
                if (method != null)
                    method.releaseConnection();
            }

            if (retries <= 0)
                throw new LocalizableException(message);
            retries--;

            // Take a little break instead of trying again immediately.
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // no op
            }
        }

        return data;
    }

// DHOU: fix 03/22/2013
private static String readResponseBody(GetMethod method)
{
     StringBuilder sb = new StringBuilder();

     try {
         InputStream stream = method.getResponseBodyAsStream();

         Reader reader = new InputStreamReader(stream);

         int read;
         char[] cbuf = new char[2048];
         read = reader.read(cbuf);
         while (read!=-1 &&
               !(read == 4 && (int)cbuf[0]==4)) // ASCII code 4 represents ``end of transmit.''
         {
             sb.append(cbuf, 0, read);

             read = reader.read(cbuf);
         }
         stream.close();
        } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }

     return sb.toString();
}
}
