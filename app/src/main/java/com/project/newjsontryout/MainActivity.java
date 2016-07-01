package com.project.newjsontryout;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.project.newjsontryout.models.PumpModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    private TextView tvData;
    private ListView lvPump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create global configuration and initialize ImageLoader with this config
        // Create default options which will be used for every
//  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .defaultDisplayImageOptions(defaultOptions)
        .build();
        ImageLoader.getInstance().init(config); // Do it on Application start
        lvPump = (ListView) findViewById(R.id.lvPump);

            //    new JSONTask().execute("http://172.17.25.117/minorproject/minor.php");



    }


    public class JSONTask extends AsyncTask<String, String, List<PumpModel>> {

        @Override
        protected List<PumpModel> doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJSON = buffer.toString();
                JSONObject parentObject = new JSONObject(finalJSON);
                JSONArray parentArray = parentObject.getJSONArray("server_response");
                List<PumpModel> pumpModelList = new ArrayList<>();

                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    PumpModel pumpModel = new PumpModel();
                    pumpModel.setPump(finalObject.getString("Pump"));
                    pumpModel.setAvailable(finalObject.getString("Available"));
                    pumpModelList.add(pumpModel);
                }
                return pumpModelList;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<PumpModel> result) {
            super.onPostExecute(result);
            //TODO need to set the data to the list
            PumpAdapter adapter = new PumpAdapter(getApplicationContext(),R.layout.row,result);
            lvPump.setAdapter(adapter);
        }

    }

    public class PumpAdapter extends ArrayAdapter{
        private List<PumpModel> pumpModelList;
        private int resource;
        private LayoutInflater inflater;
        public PumpAdapter(Context context, int resource, List<PumpModel> objects) {
            super(context, resource, objects);
            pumpModelList = objects;
            this.resource=resource;
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                convertView = inflater.inflate(resource,null);
            }
            ImageView ivIcon;
            TextView tvPump;
            ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            tvPump = (TextView)convertView.findViewById(R.id.tvPump);
            // Then later, when you want to display image
            ImageLoader.getInstance().displayImage(pumpModelList.get(position).getAvailable(), ivIcon); // Default options will be used
            tvPump.setText(pumpModelList.get(position).getPump());
            return convertView;
        }
    }

      @Override
       public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            //noinspection SimplifiableIfStatement
            if (id == R.id.action_refresh) {
                new JSONTask().execute("http://10.1.1.3/minorproject/minor.php");
                return true;
            }

            return super.onOptionsItemSelected(item);
        }


    }
