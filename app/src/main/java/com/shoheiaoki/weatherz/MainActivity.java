package com.shoheiaoki.weatherz;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {
    ListView wListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wListView = (ListView) findViewById(R.id.wListView);
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

    protected void parseJSON(String jsonString) {
        List<HashMap<String,String>> items = new ArrayList<>();
        String wDt;
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(jsonString).get("list").toString());
            String wMain;
            String wDesc=null;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject weatherJSON = jsonArray.getJSONObject(i);
                long unix_dt = Long.parseLong(weatherJSON.get("dt").toString());
                Date date = new Date(unix_dt * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("E yyyy/MM/dd", Locale.ENGLISH);
                wDt = sdf.format(date);
                try {
                    HashMap<String,String> map = new HashMap<>();
                    JSONArray subWeatherJSONArray = new JSONArray(weatherJSON.get("weather").toString());
                    wMain = subWeatherJSONArray.getJSONObject(0).get("main").toString();
                    wDesc = subWeatherJSONArray.getJSONObject(0).get("description").toString();
                    map.put("date",wDt);
                    map.put("weather",wMain);
                    map.put("description",wDesc);
                    items.add(map);
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + weatherJSON.get("weather").toString() + "\"");
                }
            }

            setListView(items);

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + jsonString + "\"");
        }
    }

    protected void setListView(List<HashMap<String,String>> items) {
//        Log.v("hoge",items.toString());
        String[] from = {"description","date"};
        int[] to = {android.R.id.text1,android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, items,android.R.layout.simple_list_item_2, from,to);
        wListView.setAdapter(adapter);
    }

}
