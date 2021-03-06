package com.shoheiaoki.weatherz;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity implements LocationListener{
    ListView wListView;
    LocationManager manager = null;
    private double lat = Double.NaN;
    private double lng = Double.NaN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wListView = (ListView) findViewById(R.id.wListView);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onPause() {
        if (manager != null) {
            manager.removeUpdates(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (manager != null) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        super.onResume();
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        Log.v("lat",String.valueOf(lat));
        Log.v("lng",String.valueOf(lng));
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
        if(Double.isNaN(lat) || Double.isNaN(lng)){
            Toast.makeText(this,"Turn on GPS",Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, String>() {
            String result = null;

            @Override
            protected String doInBackground(Void... params) {
                String url = "http://api.openweathermap.org/data/2.5/forecast/daily?lat="+String.valueOf(lat)+"&lon="+String.valueOf(lng)+"&cnt=10&mode=json&units=metric";
                Log.v("url",url);
                Request request = new Request.Builder()
//                        .url("http://api.openweathermap.org/data/2.5/forecast/daily?lat=35.493306&lon=139.610912&cnt=10&mode=json&units=metric")
                        .url(url)
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
        ArrayList<Weather> weathers = new ArrayList<Weather>();
        String wDt;
        String wDesc;
        String wMain;
        String wIcon;
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(jsonString).get("list").toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject weatherJSON = jsonArray.getJSONObject(i);
                long unix_dt = Long.parseLong(weatherJSON.get("dt").toString());
                Date date = new Date(unix_dt * 1000);
                SimpleDateFormat sdf = new SimpleDateFormat("E yyyy/MM/dd", Locale.ENGLISH);
                wDt = sdf.format(date);
                try {
                    JSONArray subWeatherJSONArray = new JSONArray(weatherJSON.get("weather").toString());
                    wMain = subWeatherJSONArray.getJSONObject(0).get("main").toString();
                    wDesc = subWeatherJSONArray.getJSONObject(0).get("description").toString();
                    wIcon = subWeatherJSONArray.getJSONObject(0).get("icon").toString();
                    Weather weather = new Weather();
                    int resId = getResources().getIdentifier("a" + wIcon, "drawable", getPackageName());
                    weather.setImage(BitmapFactory.decodeResource(getResources(), resId));

                    weather.setDesc(wDesc);
                    weather.setDate(wDt);
                    weather.setIcon(wIcon);
                    weathers.add(weather);
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + weatherJSON.get("weather").toString() + "\"");
                }
            }

            setListView(weathers);

        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + jsonString + "\"");
        }
    }

    protected void setListView(ArrayList<Weather> weathers) {
        WeatherAdapter adapter = new WeatherAdapter(this, 0, weathers);
        wListView.setAdapter(adapter);
    }

    public class WeatherAdapter extends ArrayAdapter<Weather> {
        LayoutInflater layoutInflater;

        public WeatherAdapter(Context context, int viewResourceId, ArrayList<Weather> weathers) {
            super(context, viewResourceId, weathers);
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.row, null);
            }
            Weather weather = getItem(position);
            ImageView wImageView = (ImageView) convertView.findViewById(R.id.wImageView);
            TextView wDescView = (TextView) convertView.findViewById(R.id.descText);
            TextView wDtView = (TextView) convertView.findViewById(R.id.dateText);
            wImageView.setImageBitmap(weather.getImage());
            wDescView.setText(weather.getDesc());
            wDtView.setText(weather.getDate());

            return convertView;
        }
    }

    public class Weather {
        private Bitmap image;
        private String desc;
        private String date;
        private String icon;

        public Bitmap getImage() {
            return this.image;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }

        public String getDesc() {
            return this.desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getDate() {
            return this.date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getIcon() {
            return this.icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    @Override
    public void onProviderDisabled(String provider){

    }

    @Override
    public void onProviderEnabled(String provider){
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        switch(status){
            case LocationProvider.AVAILABLE:
                Log.v("Status","AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("Status","OUT_OF_SERVICE");
                break;
            case  LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("Status","TEMPORARILY_UNAVAILABLE");
                break;

        }
    }

}

