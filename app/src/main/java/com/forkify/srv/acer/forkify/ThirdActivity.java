package com.forkify.srv.acer.forkify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class ThirdActivity extends AppCompatActivity {

    ImageView imageView;
    TextView itemName;
    TextView publisherName, publisherURL;
    TextView officialWeb;
    boolean isActive = false;
    ProgressBar waitBar;
    ScrollView itemScroll;

    class GetImageFromURL extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    class GetFoodDetail extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection httpURLConnection = null;
            String result = "";
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader in = new InputStreamReader(inputStream);
                int data = in.read();
                while (data != -1){
                    result += (char)data;
                    data = in.read();
                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("DETAIL::::: ", result);
            isActive = true;
            try {
                JSONObject jsonObject = new JSONObject(result);
                String s = jsonObject.getString("recipe");
                JSONObject jsonObject1 = new JSONObject(s);
                String itemname = jsonObject1.getString("title");
                String publishername = jsonObject1.getString("publisher");
                String publisherurl = jsonObject1.getString("publisher_url");
                String officialweb = jsonObject1.getString("source_url");
                String itemimg = jsonObject1.getString("image_url");

                if(itemname.length() > 20){
                   String newStr = itemname.substring(0, 19)+"...";
                   itemName.setText(newStr);
                }else {
                    itemName.setText(itemname);
                }
                publisherName.setText(publishername);
                GetImageFromURL getImageFromURL = new GetImageFromURL();
                Bitmap bitmap = getImageFromURL.execute(itemimg).get();
                imageView.setImageBitmap(bitmap);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        imageView = (ImageView)findViewById(R.id.imageView);
        itemName = (TextView)findViewById(R.id.itemName);
        publisherName = (TextView)findViewById(R.id.publisherName);
        publisherURL = (TextView)findViewById(R.id.publisherURL);
        officialWeb = (TextView)findViewById(R.id.officialweb);
        waitBar = (ProgressBar)findViewById(R.id.waitBar);
        itemScroll = (ScrollView)findViewById(R.id.itemScroll);
        Intent intent = getIntent();
        String id = intent.getStringExtra("recipe_id");
        Log.i("R ID::::::", id);

        GetFoodDetail getFoodDetail = new GetFoodDetail();
        getFoodDetail.execute("https://www.food2fork.com/api/get?key=377f217b02b5261458206f66a2743eda&rId="+id+"");

        ListView ingredientListView = (ListView)findViewById(R.id.ingredients_list_view);
        ingredientListView.setAdapter(new IngredientListView());
        ingredientListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {

                if(isActive == false){
                    waitBar.setVisibility(View.VISIBLE);
                    itemScroll.setVisibility(View.INVISIBLE);
                }else{
                    waitBar.setVisibility(View.INVISIBLE);
                    itemScroll.setVisibility(View.VISIBLE);
                }

                handler.postDelayed(this, 1000);

            }
        };
        handler.post(run);

    }
    public class IngredientListView extends BaseAdapter{

        @Override
        public int getCount() {
            return 10;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            view = getLayoutInflater().inflate(R.layout.ingredients_list_view_layout, null);

            return view;
        }
    }
}
