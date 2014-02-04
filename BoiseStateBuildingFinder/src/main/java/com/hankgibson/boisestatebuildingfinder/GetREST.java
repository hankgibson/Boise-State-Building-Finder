package com.hankgibson.boisestatebuildingfinder;

import android.os.AsyncTask;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;

public class GetREST extends AsyncTask<String, Void, JSONArray>
{
    final String resourceURI = "http://www.jeromeschools.org/bsubuildings.json";
    public JSONArray mResult;

    @Override
    protected JSONArray doInBackground(String... params)
    {
        try
        {
            String url = resourceURI + params[0];

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
            HttpConnectionParams.setSoTimeout(httpParams, 30000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-type", "application/json");
            ResponseHandler responseHandler = new BasicResponseHandler();
            String response = (String) httpClient.execute(httpGet, responseHandler);

            if (response != null)
            {
                mResult = new JSONArray(response);
            }
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }

        return mResult;
    }
}