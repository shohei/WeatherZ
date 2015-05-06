package com.shoheiaoki.weatherz;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            doGetWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void doGetWeather() {
        new AsyncTask<Void, Void, String>() {
            String result = null;

            @Override
            protected String doInBackground(Void... params) {
                Request request = new Request.Builder()
                        .url("http://api.openweathermap.org/data/2.5/forecast/daily?lat=35.493306&lon=139.610912&cnt=10&mode=json&units=metric")
                        .get()
                        .build();
                OkHttpClient client = new OkHttpClient();
                try {
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                parseJSON(result);
            }
        }.execute();
    }

    protected void parseJSON(String jsonString){
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(jsonString).get("list").toString());
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject weatherJSON = jsonArray.getJSONObject(i);
                long unix_dt  = Long.parseLong(weatherJSON.get("dt").toString());
                Date date = new Date(unix_dt*1000);
                SimpleDateFormat sdf = new SimpleDateFormat("E yyyy/MM/dd", Locale.ENGLISH);
                String dt = sdf.format(date);
                Log.v("date",dt);
            }
        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + jsonString + "\"");
        }
    }

    protected void setListView(){

    }

}
