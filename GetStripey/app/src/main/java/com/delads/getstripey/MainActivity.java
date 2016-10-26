package com.delads.getstripey;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.EditText;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.delads.getstripey.com.delads.getstripey.util.URLFetcher;

import org.json.JSONArray;
import org.json.JSONObject;
import android.view.ViewGroup.LayoutParams;


import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.delads.getstripey.MESSAGE";
    public final static String GET_STRIPEY_HOST = "www.getstripey.com";

    ImageButton mImageButton;
    int mThumbnailCount;
    private HashMap<Integer,JSONObject> mProductListing;
    private HashMap<String, View> mPriceTransitionMap;
    private HashMap<String, View> mNameTransitionMap;
    private LinearLayout mListView;
    private ProgressBar mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (LinearLayout)findViewById(R.id.layoutView);

        mSpinner = (ProgressBar)findViewById(R.id.progress_bar_initial_load);
        mSpinner.setVisibility(View.VISIBLE);


        String json_url = "http://www.getstripey.com/jsons/";
        new DownloadJSONTask(this).execute(json_url);

    }


    private class DownloadJSONTask extends AsyncTask {

        Context mContext;

        public DownloadJSONTask(Context context) {
            mContext = context;
        }

        public Object doInBackground(Object... urls) {
            String json =  URLFetcher.getString((String) urls[0]);

            mProductListing = new HashMap<Integer, JSONObject>();

            //This is where we process the JSON request
            try {
                JSONArray array = new JSONArray((String) json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject product = array.getJSONObject(i);
                    mProductListing.put(i,product);
                }

            } catch (Exception e){
                Log.e("GetStripey _ 1", e.getMessage());
            }
            return json;
        }

        public void onPostExecute(Object result) {

                mNameTransitionMap = new HashMap<String, View>();
                mPriceTransitionMap = new HashMap<String, View>();

            try {
                if (result != null) {

                    for (int i = 0; i < mProductListing.size(); i++) {
                        JSONObject product = mProductListing.get(i);

                        String productId = product.getString("id");

                        Map<ImageButton, JSONObject> x = new HashMap<>();

                        ImageButton imageButton = new ImageButton(this.mContext);
                        imageButton.setPadding(20,20,20,20);
                        imageButton.setPadding(20,20,20,20);
                        imageButton.setBackgroundColor(Color.WHITE);


                        Resources resources = getResources();
                        imageButton.setImageDrawable(resources.getDrawable(R.drawable.connect_placeholder));


                        imageButton.setTransitionName("image_transition_" + productId);


                        mListView.addView(imageButton);


                        x.put(imageButton, product);

                        new DownloadImageTask(this.mContext).execute(x);


                        TextView text = new TextView(this.mContext);
                        text.setBackgroundColor(Color.WHITE);
                        text.setTextColor(Color.DKGRAY);
                        text.setTextSize(15);
                        text.setGravity(Gravity.CENTER_HORIZONTAL);
                        text.setText(product.getString("name"));
                        text.setTransitionName("name_transition_" + productId);
                        mListView.addView(text);
                        mNameTransitionMap.put(productId,text);

                        TextView price = new TextView(this.mContext);
                        price.setBackgroundColor(Color.WHITE);
                        price.setTextColor(Color.parseColor("#528b8b"));
                        price.setGravity(Gravity.CENTER_HORIZONTAL);
                        price.setTextSize(15);
                        price.setText("$ " + product.getString("price"));
                        price.setTransitionName("price_transition_" + productId);
                        mListView.addView(price);
                        mPriceTransitionMap.put(productId,price);

                        //Let's add some empty views to create spacing

                        for (int j=0; j<2; j++){
                            TextView blank = new TextView(this.mContext);
                            blank.setText("");
                            blank.setBackgroundColor(Color.WHITE);
                            mListView.addView(blank);
                        }





                    }


                }
            }catch (Exception e){
                Log.e("GetStripey _ 2", e.getMessage());
            }

            //Let's close off the spinner
            mSpinner.setVisibility(View.GONE);


        }
    }




        private class DownloadImageTask extends AsyncTask {

        Context mContext;
        ImageButton mView;
        JSONObject mJSONProduct = null;


        public DownloadImageTask(Context context){
            mContext = context;
        }

        public Object doInBackground(Object... urls) {
            HashMap map = (HashMap<ImageButton, JSONObject>)urls[0];
            Iterator iter = map.entrySet().iterator();
            String url = null;

            //Should only have one entry
            while(iter.hasNext()){
                Map.Entry pair = (Map.Entry)iter.next();
                mView = (ImageButton)pair.getKey();

                try {
                    mJSONProduct = (JSONObject) pair.getValue();
                    JSONObject picture = mJSONProduct.getJSONObject("picture");
                    url = (String) picture.getString("url");
                }catch (Exception e){}

               // image_url = (String)pair.getValue();
            }

            return URLFetcher.getBitmap(url);
        }

        public void onPostExecute(Object result) {

            Bitmap image = (Bitmap)result;
            final Bitmap image_copy = image;

            if(image != null){
                mView.setImageBitmap(image);

                mView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // Perform action on click
                        try {
                            Intent intent = new Intent(MainActivity.this, DisplayProductActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            String id = (String) mJSONProduct.getString("id");
                            String file_name = id + "_image";
                            createImageFromBitmap(image_copy,file_name);

                            intent.putExtra("name", (String) mJSONProduct.getString("name"));
                            intent.putExtra("summary",(String) mJSONProduct.getString("summary"));
                            intent.putExtra("price",(String) mJSONProduct.getString("price"));
                            intent.putExtra("id",(String) mJSONProduct.getString("id"));

                            Log.println(Log.INFO,"MainActivity", "onClickListener: v.getTransitionName = " + v.getTransitionName());
                            View name = (View)mNameTransitionMap.get(id);
                            View price = (View)mPriceTransitionMap.get(id);

                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                                    Pair.create(v, v.getTransitionName()),
                                    Pair.create(name, name.getTransitionName()),
                                    Pair.create(price, price.getTransitionName()));




                          //  ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, mView, mView.getTransitionName());
                            mView.getContext().startActivity(intent,options.toBundle());


                          //  mContext.getApplicationContext().startActivity(intent);

                        }catch (Exception e){
                            Log.println(Log.ERROR,"MainActivity", e.getMessage());
                        }

                    }
                });


            }


        }


    }

    public String createImageFromBitmap(Bitmap bitmap, String name) {

        String fileName = name;//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }





}
