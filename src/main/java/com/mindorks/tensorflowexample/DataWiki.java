package com.mindorks.tensorflowexample;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DataWiki extends AppCompatActivity {

    private TextView title,content,heading;

    private String prediction;
    ProgressDialog pd;
    ImageView mImageView;

    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_wiki);

        prediction = getIntent().getStringExtra("prediction");

        title = (TextView)findViewById(R.id.title);
        content = (TextView)findViewById(R.id.content);
        heading = (TextView)findViewById(R.id.heading);

        mImageView = (ImageView)findViewById(R.id.image);

        mRequestQueue = Volley.newRequestQueue(this);

        title.setText(prediction);

        String parameter = prediction.substring(0,prediction.indexOf("(")-1);
        if(parameter.indexOf(" ") == 0){
            parameter = parameter.substring(1,parameter.length());
        }
        heading.setText(parameter.substring(0,1).toUpperCase()+parameter.substring(1));
        parameter = parameter.replaceAll(" ", "%20");
        String xURL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles="+parameter;

        String imgURL = "https://en.wikipedia.org/w/api.php?action=query&titles="+parameter+"&prop=pageimages&format=json&pithumbsize=100";

        new JsonTask(xURL).execute();
        new ImageTask(imgURL).execute();
    }

        private class JsonTask extends AsyncTask<Void, Void, Void> {

            String fURL;
            String data;

            public JsonTask(String url){
                fURL = url;
            }

            protected void onPreExecute() {
                super.onPreExecute();

                pd = new ProgressDialog(DataWiki.this);
                pd.setMessage("Please wait");
                pd.setCancelable(false);
                pd.show();
            }

            protected Void doInBackground(Void... voids) {

                try {
                    URL url = new URL(fURL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = httpURLConnection.getInputStream();

                    //read the data from stream
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line = "";

                    while(line != null){
                        line = bufferedReader.readLine();
                        data = data + line;

                    }



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (pd.isShowing()){
                    pd.dismiss();
                }

                int x = data.indexOf("extract");
                data = data.substring(x+10,data.length()-9);
                data = data.replace("\\n","");
                data = data.replace("\"","");
                data = data.replace("\\","");
                if(!data.equals(""))
                content.setText(data);
                else
                content.setText("No data available for entered query on Wikipedia.");


            }
        }

    private class ImageTask extends AsyncTask<Void, Void, Void> {

        String fURL;
        String data;

        public ImageTask(String url){
            fURL = url;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL(fURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();

                //read the data from stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";

                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;

                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (data.contains("thumbnail")) {

                Log.i("imgData", data);
                int x = data.indexOf("\"thumbnail\":{\"source\"");
                int y = data.indexOf("width");
                data = data.substring(x + 23, y - 3);


                Glide
                        .with(DataWiki.this)
                        .load(data)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)      //Not caching the image because of image filter problem
                        .skipMemoryCache(true)
                        .into(new SimpleTarget<Bitmap>(200, 200) { //width and height
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                mImageView.setImageBitmap(resource);
                            }
                        });

            }
        }
    }


    }
