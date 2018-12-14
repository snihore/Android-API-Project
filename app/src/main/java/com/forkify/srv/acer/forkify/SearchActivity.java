package com.forkify.srv.acer.forkify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class SearchActivity extends AppCompatActivity {

    ListView searchItemListView;
    String[] items;
    ProgressBar loopIcon;
    Boolean isLoopActive = false;
    TextView noItemFoundText;
    Bitmap[] images;

    public class DownloadImageFromURL extends AsyncTask<String, Void, Bitmap>{

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

    public class SearchAPIData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection httpURLConnection = null;
            String result = "";
            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1) {
                    result += (char) data;
                    data = inputStreamReader.read();
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

//            Log.i("API Data", result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                int count = (int) jsonObject.get("count");
                String recipes = jsonObject.getString("recipes");
                items = new String[count];
                images = new Bitmap[count];

                JSONArray jsonArray = new JSONArray(recipes);

                for (int i = 0; i < count; i++) {
                    items[i] = jsonArray.getString(i);

                }
                isLoopActive = false;
//                searchItemListView.setAdapter(new CustomizeListView(items));
                if(items.length == 0){
                    noItemFoundText.setVisibility(View.VISIBLE);
                }else {
                    noItemFoundText.setVisibility(View.INVISIBLE);
                    for(int i=0; i<items.length; i++){
                        JSONObject imageJSONObject = new JSONObject(items[i]);
                        String imageURL = (String) imageJSONObject.get("image_url");
//                        Log.i("IMAGE URL:::: ", imageURL);
                        DownloadImageFromURL downloadImageFromURL = new DownloadImageFromURL();
                        Bitmap bitmap = downloadImageFromURL.execute(imageURL).get();
                        images[i] = bitmap;
                    }
                }
                searchItemListView.setAdapter(new CustomizeListView(items));


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public class CustomizeListView extends BaseAdapter {

        String items[];

        CustomizeListView() {
            items = null;
        }

        public CustomizeListView(String[] items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.length;
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

            LayoutInflater layoutInflater = getLayoutInflater();
            view = layoutInflater.inflate(R.layout.search_item_list_customize, null);
            TextView foodItemTitle = view.findViewById(R.id.food_item_title);
            TextView foodItemPublisher = view.findViewById(R.id.food_item_publisher_name);
            ImageView foodItemImg = (ImageView) findViewById(R.id.food_item_img);
            TextView foodItemSocialRank = view.findViewById(R.id.food_item_rank);

            try {
                noItemFoundText.setVisibility(View.INVISIBLE);
                JSONObject jsonObject = new JSONObject(items[i]);
                String title = (String) jsonObject.get("title");
                String publisher = (String) jsonObject.get("publisher");
                String imageURL = (String) jsonObject.get("image_url");
                String socialRank = jsonObject.get("social_rank").toString();


                foodItemTitle.setText(title);
                foodItemPublisher.setText(publisher);
                foodItemSocialRank.setText(socialRank);
                if(images.length>0 && images[i]!=null){
                    foodItemImg.setImageBitmap(images[i]);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final ImageView search_icon = (ImageView) findViewById(R.id.search_icon);
        final EditText search_area = (EditText) findViewById(R.id.search_area);
        RelativeLayout searchActivity = (RelativeLayout) findViewById(R.id.searchActivity);
        searchItemListView = (ListView) findViewById(R.id.search_item_list_view);
        loopIcon = (ProgressBar) findViewById(R.id.loop_icon);
        noItemFoundText = (TextView) findViewById(R.id.no_item_found_text);


        searchActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });


        searchItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Intent intent = new Intent(getApplicationContext(), ThirdActivity.class);
                try {
                    JSONObject jsonObject = new JSONObject(items[i]);
                    String id = jsonObject.getString("recipe_id");
                    intent.putExtra("recipe_id", id);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        search_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_icon.setVisibility(View.INVISIBLE);
                search_area.setVisibility(View.VISIBLE);


                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(search_area, InputMethodManager.SHOW_IMPLICIT);
            }
        });


        search_area.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (i == KeyEvent.KEYCODE_ENTER)) {
//                    Toast.makeText(SearchActivity.this, "Searched", Toast.LENGTH_SHORT).show();

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    searchItemListView.setVisibility(View.VISIBLE);

                    final String itemName = search_area.getText().toString();
                    search_area.setText("");
                    search_area.setHint(itemName);

                    if (itemName.matches("")) {
                        Toast.makeText(SearchActivity.this, "Enter something you wish", Toast.LENGTH_SHORT).show();
                    } else {
                        isLoopActive = true;
                        final SearchAPIData searchAPIData = new SearchAPIData();
                        searchAPIData
                                .execute("https://www.food2fork.com/api/search?key=377f217b02b5261458206f66a2743eda&q=" + itemName + "");

                    }

                    return true;
                }
                return false;
            }
        });

        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {

                if (isLoopActive == true) {
                    loopIcon.setVisibility(View.VISIBLE);

                } else {
                    loopIcon.setVisibility(View.INVISIBLE);
                }

                handler.postDelayed(this, 1000);

            }
        };
        handler.post(run);


    }


}
