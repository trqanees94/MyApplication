package com.example.tariq.myapplication;

import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import data.Weather;

public class MainActivity extends AppCompatActivity {

    ListView lvWeather;
    EditText editText;
     Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         lvWeather = (ListView)findViewById(R.id.lvWeather);

        editText = (EditText) findViewById(R.id.edittext);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
              //  Toast msg = Toast.makeText(getBaseContext(),str,Toast.LENGTH_LONG);
              //  msg.show();


                String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", str);
                String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL) );
                new JSONTask().execute(endpoint);
            }
        });

    }

     public class JSONTask extends AsyncTask<String , String, List<Weather> >{
                @Override
                protected List<Weather> doInBackground(String...params){
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;

                    try{
                        URL url = new URL(params[0]);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.connect();

                        InputStream stream= connection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(stream));
                        StringBuffer buffer = new StringBuffer();

                        String line ="";
                        while ((line=reader.readLine())!= null){
                            buffer.append(line);
                        }

                        String finalJson = buffer.toString();
                        Weather weather = new Weather();

                        try {
                            JSONObject parentObject = new JSONObject(finalJson);
                            JSONObject queryResults = parentObject.optJSONObject("query");

                            List<Weather> weatherList = new ArrayList<>();

                            weather.setLocation(queryResults.optJSONObject("results").optJSONObject("channel").optString("title"));
                            weather.setSunrise(queryResults.optJSONObject("results").optJSONObject("channel").optJSONObject("astronomy").optString("sunrise"));
                            weather.setSunset(queryResults.optJSONObject("results").optJSONObject("channel").optJSONObject("astronomy").optString("sunset"));
                            weather.setTemperature(queryResults.optJSONObject("results").optJSONObject("channel").optJSONObject("item").optJSONObject("condition").optString("temp"));
                            weather.setDegree(queryResults.optJSONObject("results").optJSONObject("channel").optJSONObject("units").optString("temperature"));

                            weatherList.add(weather);

                            return weatherList;
                        } catch (JSONException e){
                            e.printStackTrace();}

                    } catch (MalformedURLException e){
                        e.printStackTrace();
                    } catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        if(connection != null) {
                            connection.disconnect();
                        }
                        try {
                            if(reader != null) {
                                reader.close();
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                        return null;
                }

         @Override
         protected void onPostExecute(List<Weather> result){
             super.onPostExecute(result);
             WeatherAdapter adapter = new WeatherAdapter(getApplicationContext(), R.layout.row, result);
             lvWeather.setAdapter(adapter);
         }
     }

    public class WeatherAdapter extends ArrayAdapter{

            private List<Weather> weatherList;
            private LayoutInflater inflater;
            private int resource;

            public WeatherAdapter(Context context, int resource, List<Weather> objects){
                super(context, resource, objects);
                weatherList=objects;
                this.resource=resource;
                inflater=(LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent){

                if(convertView==null){
                    convertView=inflater.inflate(resource, null);
                }

                TextView weatherLocation;
                TextView weatherDegree;
                TextView weatherSunrise;
                TextView weatherSunset;
                TextView weatherTemperature;

                 weatherLocation = (TextView)convertView.findViewById(R.id.location);
                weatherSunrise=(TextView)convertView.findViewById(R.id.sunrise);
                 weatherSunset=(TextView)convertView.findViewById(R.id.sunset);
                weatherTemperature =(TextView)convertView.findViewById(R.id.temperature);
                weatherDegree=(TextView)convertView.findViewById(R.id.degree);

                weatherLocation.setText(weatherList.get(position).getLocation() );
                weatherSunrise.setText(weatherList.get(position).getSunrise() );
                weatherSunset.setText(weatherList.get(position).getSunset() );
                weatherTemperature.setText(weatherList.get(position).getTemperature() );
                weatherDegree.setText(weatherList.get(position).getDegree() );

                return convertView;
            }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String location = "Austin, TX";

        String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", location);
        String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL) );

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            new JSONTask().execute(endpoint);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

