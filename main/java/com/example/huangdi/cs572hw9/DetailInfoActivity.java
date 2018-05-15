package com.example.huangdi.cs572hw9;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Rating;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;

public class DetailInfoActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    public ProgressDialog pd;
    private List<TextView> tvs = new ArrayList<TextView>();
    private ViewPager pager;
    private List<View> views;
    public RequestQueue volley_queue;
    public JSONObject res_json;
    public ArrayList<Bitmap> photo_array;

    public GoogleApiClient mGoogleApiClient;
     public float target_lat;
     public float target_lng;
     public String target_name;
     public int scroll_width;
     public String target_placeid;
      public GoogleMap myMap;
      public  Polyline polyline;
     public Marker marker1;
    public Marker marker2;
    public String start_name;
    public String  sel_place_id;

    private RecyclerView mRecyclerView;
    public ArrayList<OneReview> review_list=new ArrayList<OneReview>();
    public ArrayList<OneReview> google_reviews;
    public ArrayList<OneReview> yelp_reviews;
    public Spinner review_ord_spinner;
    public Spinner review_g_y_spinner;
    public ReviewRecycleAdapter result_adapter;

    public ArrayList<OnePlace> favorite_list=new ArrayList<>();
    public SharedPreferences sp;
    public Gson mygson= new Gson();
    public boolean place_is_fav;
    public String place_id;
    public SharedPreferences.Editor edit;

    public ImageView fav_img;
    public OnePlace this_place=new OnePlace();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main);

        try {
            sp = getSharedPreferences("CS571", Context.MODE_PRIVATE);
        } catch(Exception e) {
            System.out.println("hello world error: "+e.getMessage());
        }
        edit = sp.edit();
        String favo_str = sp.getString("favo_list", "Null");
        if (favo_str.equals("Null")) {
            favorite_list.clear();
        } else {
            favorite_list.clear();
            favorite_list.addAll(MainActivity.place_str_to_arr(favo_str));
        }

        pd = new ProgressDialog(this);

        Bundle bundle = this.getIntent().getExtras();
         place_id = bundle.getString("place_id");
        String place_name = bundle.getString("name");
        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView detail_title = (TextView) findViewById(R.id.detail_title);
        detail_title.setText(place_name);

        fav_img= findViewById(R.id.fav_img);

       place_is_fav=false;
       for (int i=0;  i<favorite_list.size(); i++){
           if (favorite_list.get(i).place_id.equals(place_id)){
               place_is_fav=true;
               break;
           }
       }
       if (place_is_fav){
           fav_img.setImageResource(R.drawable.heart_fill_white);
       }else {
           fav_img.setImageResource(R.drawable.heart_outline_white);
       }

        photo_array = new ArrayList<Bitmap>();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

//        initTextView();
//        initView();
//        initViewPager();

        pd.setMessage("Fetching details");
        pd.show();


        new PhotoTask() {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onPostExecute(ArrayList<Bitmap> res_arr) {
                photo_array = res_arr;
                if (photo_array.size()>0) {
                    display_photos();
                } else {
                    ScrollView photo_scroll =  views.get(1).findViewById(R.id.photo_scroll);
                    photo_scroll.setVisibility(GONE);
                    LinearLayout no_photo_lay = (LinearLayout) views.get(1).findViewById(R.id.no_photo_lay);
                    no_photo_lay.setVisibility(View.VISIBLE);
                }
            }
        }.execute(place_id);


        volley_queue = Volley.newRequestQueue(this);
        String req_url = "http://cs571hw-env.us-east-2.elasticbeanstalk.com/place_detail?placeid=" + place_id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, req_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {

                            res_json = new JSONObject(response);

                            JSONObject location=res_json.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");

                            target_lat=Float.parseFloat(location.getString("lat"));
                            target_lng=Float.parseFloat(location.getString("lng"));

                            target_name=res_json.getJSONObject("result").getString("name");
                            target_placeid= res_json.getJSONObject("result").getString("place_id");

                            this_place.place_id=target_placeid;
                            this_place.icon=res_json.getJSONObject("result").getString("icon");
                            this_place.name =res_json.getJSONObject("result").getString("name");
                            if (res_json.getJSONObject("result").has("vicinity"))
                            this_place.vicinity =res_json.getJSONObject("result").getString("vicinity");
                            this_place.isfav=place_is_fav;

                                    google_reviews=new ArrayList<OneReview>();
                            if (res_json.getJSONObject("result").has("reviews")){
                                JSONArray tmp_arr=res_json.getJSONObject("result").getJSONArray("reviews");
                                OneReview a_review;

                                for (int i=0; i<tmp_arr.length(); i++){
                                   a_review=new OneReview();
                                   a_review.review_image="";
                                   if (tmp_arr.getJSONObject(i).has("profile_photo_url")){
                                       a_review.review_image=tmp_arr.getJSONObject(i).getString("profile_photo_url");
                                   }
                                   a_review.writer_name=tmp_arr.getJSONObject(i).getString("author_name");
                                   a_review.author_url=tmp_arr.getJSONObject(i).getString("author_url");
                                   a_review.time_stamp=Long.parseLong( tmp_arr.getJSONObject(i).getString("time"));
                                   a_review.rating=Float.parseFloat(tmp_arr.getJSONObject(i).getString("rating"));
                                   a_review.review_text=tmp_arr.getJSONObject(i).getString("text");
                                   a_review.index=i;
                                   a_review.date_time=stampToDate(a_review.time_stamp*1000);
                                  // Log.d("DetailInfoActivity", "rating:"+String.valueOf(a_review.rating));
                                    google_reviews.add(a_review);

                                }
                                review_list.clear();
                                review_list.addAll(google_reviews);
                            }

                            pd.cancel();
                            initTextView();
                            initView();
                            initViewPager();

                            String  format_address="";
                            if (res_json.getJSONObject("result").has("formatted_address")) {
                                format_address = res_json.getJSONObject("result").getString("formatted_address");
                                String[] str_seg = format_address.split(",");
                                String address1 = str_seg[0];
                                String city = str_seg[str_seg.length - 3];
                                String state = str_seg[str_seg.length - 2];
                                int i = 0;
                                while (i < state.length() && state.charAt(i) == ' ') {
                                    i++;
                                }
                                state = state.substring(i, i + 2);
                                JSONArray add_comp = res_json.getJSONObject("result").getJSONArray("address_components");
                                String country = "US";
                                for (i = 0; i < add_comp.length(); i++) {
                                    if (add_comp.getJSONObject(i).getJSONArray("types").getString(0).equals("country")) {
                                        country = add_comp.getJSONObject(i).getString("short_name");
                                    }
                                }
                                send_yelp_req(target_name, address1, city, state, country);
                            }


                            ImageView share_twitter= findViewById(R.id.share_img);
                            share_twitter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {

                                    Uri uri;
                                    try {
                                        String t_url="";
                                        if (res_json.getJSONObject("result").has("url"))
                                            t_url=res_json.getJSONObject("result").getString("url");
                                        if (res_json.getJSONObject("result").has("website") &&
                                                res_json.getJSONObject("result").getString("website").length()>0 ){
                                            t_url=res_json.getJSONObject("result").getString("website");
                                        }


                                        uri = Uri.parse("https://twitter.com/intent/tweet?text="+ URLEncoder.encode("Check out "+target_name+" located at "+
                                                res_json.getJSONObject("result").getString("formatted_address")+".  ", "utf8")+"Website:"+
                                                URLEncoder.encode(t_url, "utf8")+"&hashtags=TravelAndEntertainmentSearch");
                                        Intent intent_twitter = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent_twitter);
                                    } catch (JSONException e) {
                                        System.out.println("JSON error"+e.getMessage());
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }


                                    }
                            });


                            fav_img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    if (place_is_fav){
                                        place_is_fav=false;
                                        this_place.isfav=false;
                                        int idx=-1;
                                        for (int i=0; i<favorite_list.size(); i++){
                                            if (favorite_list.get(i).place_id.equals(place_id)){
                                                idx=i;
                                                break;
                                            }
                                        }
                                        favorite_list.remove(idx);

                                        edit.putString("favo_list",  mygson.toJson(favorite_list) );
                                        edit.commit();

                                        fav_img.setImageResource(R.drawable.heart_outline_white);
                                        Toast.makeText(DetailInfoActivity.this, this_place.name+" was removed from favorites", Toast.LENGTH_LONG).show();
                                    } else {
                                        place_is_fav=true;
                                        fav_img.setImageResource(R.drawable.heart_fill_white);
                                        this_place.isfav=true;
                                        favorite_list.add(this_place);

                                        edit.putString("favo_list",  mygson.toJson(favorite_list) );
                                        edit.commit();
                                        Toast.makeText(DetailInfoActivity.this, this_place.name+" was added to favorites", Toast.LENGTH_LONG).show();

                                    }

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //            Toast.makeText(MainActivity.this, response.substring(0,500), Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.cancel();
                Toast.makeText(DetailInfoActivity.this, "Failure of Network or API", Toast.LENGTH_LONG).show();
            }
        });
        volley_queue.add(stringRequest);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initTextView() {
        TextView tab_info = (TextView) findViewById(R.id.tab_info);
        TextView tab_photo = (TextView) findViewById(R.id.tab_photo);
        TextView tab_map = (TextView) findViewById(R.id.tab_map);
        TextView tab_review = (TextView) findViewById(R.id.tab_review);
        //OnClickListener click=new MyClickListener();
        tab_info.setOnClickListener(new DetailInfoActivity.MyClickListener(0));
        tab_photo.setOnClickListener(new DetailInfoActivity.MyClickListener(1));
        tab_map.setOnClickListener(new DetailInfoActivity.MyClickListener(2));
        tab_review.setOnClickListener(new DetailInfoActivity.MyClickListener(3));

        tvs.add(tab_info);
        tvs.add(tab_photo);
        tvs.add(tab_map);
        tvs.add(tab_review);
    }

    private class MyClickListener implements View.OnClickListener {
        private int index;

        public MyClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            pager.setCurrentItem(index);
        }
    }

    public void initView() {
        views = new ArrayList<View>();
        LayoutInflater li = getLayoutInflater();
        views.add(li.inflate(R.layout.detail_info, null));
        views.add(li.inflate(R.layout.detail_photo, null));
        views.add(li.inflate(R.layout.detail_map, null));
        views.add(li.inflate(R.layout.detail_review, null));
    }

    public void initViewPager() {
        // TODO Auto-generated method stub
        pager = (ViewPager) findViewById(R.id.detail_pager);
        PagerAdapter adapter = new DetailInfoActivity.MyPagerAdapter();
        pager.setAdapter(adapter);
     //   pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int index) {
                // TODO Auto-generated method stub

               final HorizontalScrollView hr_scroll= (HorizontalScrollView) findViewById(R.id.scrollView);

                scroll_width =hr_scroll.getWidth();
//                hr_scroll.post(new Runnable() {
//                    @Override
//                    public void run() {
//                         scroll_width=  hr_scroll.getWidth();
//                    }
//                });

                if (index==0 || index==1){
                    hr_scroll.smoothScrollTo(0 ,0);
                } else {
                    hr_scroll.smoothScrollTo(scroll_width ,0);
                }
                for (int i = 0; i < tvs.size(); i++) {
                    if (i == index) {
                        tvs.get(i).setBackgroundResource(R.drawable.bottom_red_border);
                        tvs.get(i).setTextColor(0xFFFFFFFF);
                    } else {
                        if (i < index) {
                            tvs.get(i).setBackgroundResource(R.drawable.right_border);
                        } else {
                            tvs.get(i).setBackgroundResource(R.drawable.left_border);
                        }
                        tvs.get(i).setTextColor(0xDCFFFFFF);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
        DetailInfoActivity.this.finish();
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return views.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            // TODO Auto-generated method stub
            //super.destroyItem(container, position, object);
            container.removeView(views.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // TODO Auto-generated method stub
            //    TextView tv = new TextView(this);
            TextView onetv;
            int i;

            tvs.get(0).setTextColor(0xFFFFFFFF);
            tvs.get(1).setTextColor(0xDCFFFFFF);
            tvs.get(2).setTextColor(0xDCFFFFFF);
            tvs.get(3).setTextColor(0xDCFFFFFF);
            if (position == 0) {

                final LinearLayout info_lay = (LinearLayout) views.get(position).findViewById(R.id.info_lay);
                JSONObject place_res = null;
                try {
                    place_res = res_json.getJSONObject("result");
                    String address = "";
                    if (place_res.has("formatted_address")) {
                        address = place_res.getString("formatted_address");
                        TextView address_text = (TextView) views.get(position).findViewById(R.id.address_text);
                        address_text.setText(address);
                    } else {
                        LinearLayout layout_address = (LinearLayout) views.get(position).findViewById(R.id.layout_address);
                        layout_address.setVisibility(GONE);
                    }

                    String phone_num = "";
                    if (place_res.has("international_phone_number")) {
                        phone_num = place_res.getString("international_phone_number");
                        TextView phone_text = (TextView) views.get(position).findViewById(R.id.phone_text);
                        phone_text.setText(phone_num);
                    } else {
                        LinearLayout layout_phone = (LinearLayout) views.get(position).findViewById(R.id.layout_phone);
                        layout_phone.setVisibility(GONE);
                    }

                    int price_level = -1;
                    if (place_res.has("price_level")) {
                        price_level = Integer.parseInt(place_res.getString("price_level"));
                        TextView price_text = (TextView) views.get(position).findViewById(R.id.price_text);
                        if (price_level == 0) {
                            price_text.setText("0");
                        } else {
                            String price_str = "";
                            for (i = 1; i <= price_level; i++) {
                                price_str += "$";
                            }
                            price_text.setText(price_str);
                        }
                    } else {
                        LinearLayout layout_price = (LinearLayout) views.get(position).findViewById(R.id.layout_price);
                        layout_price.setVisibility(GONE);
                    }

                    float rating;
                    if (place_res.has("rating")) {
                        rating = Float.parseFloat(place_res.getString("rating"));
                        RatingBar rating_text = (RatingBar) views.get(position).findViewById(R.id.rating_text);
                        //   TextView rating_text= (TextView) views.get(position).findViewById(R.id.rating_text);
                        rating_text.setRating(rating);
                    } else {
                        LinearLayout layout_rating = (LinearLayout) views.get(position).findViewById(R.id.layout_rating);
                        layout_rating.setVisibility(GONE);
                    }

                    String google_page = "";
                    if (place_res.has("url")) {
                        google_page = place_res.getString("url");
                        TextView google_text = (TextView) views.get(position).findViewById(R.id.google_text);
                        google_text.setText(google_page);
                    } else {
                        LinearLayout layout_googlepage = (LinearLayout) views.get(position).findViewById(R.id.layout_googlepage);
                        layout_googlepage.setVisibility(GONE);
                    }

                    String website = "";
                    if (place_res.has("website")) {
                        website = place_res.getString("website");
                        TextView website_text = (TextView) views.get(position).findViewById(R.id.website_text);
                        website_text.setText(website);
                    } else {
                        LinearLayout layout_website = (LinearLayout) views.get(position).findViewById(R.id.layout_website);
                        layout_website.setVisibility(GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (position == 1) {
             if (photo_array.size()>0){
                 display_photos();
             }

            } else if (position == 2) {
//                TextView tv =  (TextView) views.get(position).findViewById(R.id.from_text);
//                tv.setText(String.valueOf(target_lat)+","+String.valueOf(target_lng));
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
                mapFragment.getMapAsync(DetailInfoActivity.this);

                final AutoCompleteTextView searchPlace = views.get(position).findViewById(R.id.from_input);
                CustomAutoCompleteAdapter adapter =  new CustomAutoCompleteAdapter(DetailInfoActivity.this);
                searchPlace.setAdapter(adapter);
                searchPlace.setOnItemClickListener(onItemClickListener);

                  Spinner mode_spinner= (Spinner) views.get(2).findViewById(R.id.mode_spinner);
                mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                      String from_word= searchPlace.getText().toString();
                      boolean flag=false;
                      for (int i=0; i<from_word.length(); i++){
                          if (from_word.charAt(i)!=' '){
                              flag=true;
                              break;
                          }
                      }
                      if (flag==false) return;

                          update_map_direction();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub
                    }
                });

            } else if (position==3){
                mRecyclerView = (RecyclerView) views.get(3).findViewById(R.id.review_recycler);
                LinearLayoutManager layoutManager = new LinearLayoutManager(DetailInfoActivity.this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(layoutManager);

              result_adapter=new ReviewRecycleAdapter(review_list);
                result_adapter.setOnItemClickListener(new ReviewRecycleAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(int position) {

                        Uri uri = Uri.parse(review_list.get(position).author_url);
                        Intent intent_review = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent_review);
                    }
                });
                mRecyclerView.setAdapter( result_adapter);

                LinearLayout no_review_lay=   views.get(3).findViewById(R.id.no_review_lay);
                LinearLayout  review_recycle_lay=(LinearLayout)  views.get(3).findViewById(R.id.review_recycle_lay);
                if (review_list.size()==0){
                    no_review_lay.setVisibility(View.VISIBLE);
                    review_recycle_lay.setVisibility(View.GONE);
                } else {
                    no_review_lay.setVisibility(View.GONE);
                    review_recycle_lay.setVisibility(View.VISIBLE);
                }

                review_g_y_spinner= (Spinner) views.get(3).findViewById(R.id.g_or_y_sel);
                review_g_y_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                       update_review_list();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub
                    }
                });


                review_ord_spinner= (Spinner) views.get(3).findViewById(R.id.order_sel);
                review_ord_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                      update_review_list();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub
                    }
                });


            }
            container.addView(views.get(position));
            return views.get(position);
        }
    }

    public abstract class PhotoTask extends AsyncTask<String, Void, ArrayList<Bitmap>> {
        public PhotoTask() {
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(String... params) {
            if (params.length != 1) {
                return null;
            }
            final String placeId = params[0];
            ArrayList<Bitmap> res_arr = new ArrayList<Bitmap>();

            PlacePhotoMetadataResult result = Places.GeoDataApi
                    .getPlacePhotos(mGoogleApiClient, placeId).await();

            if (result.getStatus().isSuccess()) {
                PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                    for (int i = 0; i < photoMetadataBuffer.getCount(); i++) {
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(i);
                        Bitmap image = photo.getPhoto(mGoogleApiClient).await()
                                .getBitmap();

                        res_arr.add(image);
                    }
                }
                photoMetadataBuffer.release();
            }
            return res_arr;
        }

    }


    @Override
    public void onMapReady(GoogleMap map) {
         myMap=map;
        LatLng place_lat_lng = new LatLng(target_lat, target_lng);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION },
                    1);
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    1);
        }
       // map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place_lat_lng , 15));

         marker1 = map.addMarker(new MarkerOptions()
                .position(place_lat_lng )
                .title(target_name));
        marker1.showInfoWindow();
        marker2= map.addMarker(new MarkerOptions()
                .position(place_lat_lng )
                .visible(false));

        polyline = map.addPolyline(new PolylineOptions().visible(false).width(25).color(Color.BLUE).geodesic(true));
        sel_place_id="";
        start_name="";
    }

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              sel_place_id=   ((MyPlace)adapterView.getItemAtPosition(i)).placeId;
                start_name=((MyPlace)adapterView.getItemAtPosition(i)).name;
                        update_map_direction();
                }
            };

   public void update_map_direction(){
       Spinner mode_spinner= (Spinner) views.get(2).findViewById(R.id.mode_spinner);
       String input_mode= (String)mode_spinner.getSelectedItem();
       if (input_mode.equals("Driving")) input_mode="driving";
       else if (input_mode.equals("Bicycling")) input_mode="bicycling";
       else if (input_mode.equals("Transit")) input_mode="transit";
       else if (input_mode.equals("Walking")) input_mode="walking";

       String req_url = "http://cs571hw-env.us-east-2.elasticbeanstalk.com/get_routes?st_place_id=" + sel_place_id+
               "&des_lat="+String.valueOf(target_lat)+"&des_lng="+String.valueOf(target_lng)+"&mode="+input_mode;

       StringRequest stringRequest = new StringRequest(Request.Method.GET, req_url,
               new Response.Listener<String>() {
                   @Override
                   public void onResponse(String response) {
                       int i,j;
                       try {

                           JSONObject  target_res = new JSONObject(response);
                           JSONArray route_legs;
                           double p_lat, p_lng, e_lat, e_lng;
                           if (target_res.has("routes") && target_res.getJSONArray("routes").length()>0){
//                                          pd.setMessage(target_res.getString("routes"));
//                                          pd.show();

                               route_legs=target_res.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                               ArrayList<LatLng> route_arr=new ArrayList<LatLng>();
                               for (i=0; i<route_legs.length(); i++){
                                   JSONArray steps=route_legs.getJSONObject(i).getJSONArray("steps");
                                   for (j=0; j<steps.length(); j++){
                                       if (i==0 && j==0) {
                                           p_lat = steps.getJSONObject(j).getJSONObject("start_location").getDouble("lat");
                                           p_lng = steps.getJSONObject(j).getJSONObject("start_location").getDouble("lng");
                                           route_arr.add(new LatLng(p_lat, p_lng));
                                           marker1.remove();
                                           marker1 = myMap.addMarker(new MarkerOptions()
                                                   .position(new LatLng(p_lat, p_lng) )
                                                   .title(start_name));
                                                  marker1.showInfoWindow();
                                       }
                                       e_lat=steps.getJSONObject(j).getJSONObject("end_location").getDouble("lat");
                                       e_lng=steps.getJSONObject(j).getJSONObject("end_location").getDouble("lng");
                                       route_arr.add(new LatLng(e_lat, e_lng));
                                       if (i==route_legs.length()-1 && j==steps.length()-1){
                                           marker2.setPosition(new LatLng(e_lat, e_lng));
                                           marker2.setVisible(true);
                                       }
                                   }
                               }
                               //        PolylineOptions rectOptions = new PolylineOptions().addAll(route_arr).width(25).color(Color.BLUE).geodesic(true);
                               //      Polyline polyline = myMap.addPolyline(rectOptions);
                               polyline.setPoints(route_arr);
                               polyline.setVisible(true);

                           }

                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }
               }, new Response.ErrorListener() {
           @Override
           public void onErrorResponse(VolleyError error) {
           }
       });
       volley_queue.add(stringRequest);
   }

    public static long dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        return ts;
    }

    public static String stampToDate(long lt){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    public void send_yelp_req(String target_name, String address1, String city,  String state,  String country){
        String req_url = "";
        try {
            req_url = "http://cs571hw-env.us-east-2.elasticbeanstalk.com/get_yelp?name=" +
       URLEncoder.encode(target_name, "utf8")+"&address1="+URLEncoder.encode(address1, "utf8")+"&city="+URLEncoder.encode(city, "utf8")+"&state="+state+"&country="+country;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, req_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                           if (response.equals("none")){
                               yelp_reviews=new ArrayList<OneReview>();
                           }else {
                               try {
                                    yelp_reviews=new ArrayList<OneReview>();
                                 //  JSONObject  res_json = new JSONObject(response);
                                   JSONArray tmp_arr=new JSONArray(response);
                                   OneReview a_review;
                                   for (int i=0; i<tmp_arr.length(); i++){
                                       a_review=new OneReview();
                                       a_review.review_image="";
                                       a_review.writer_name="";
                                       if (tmp_arr.getJSONObject(i).has("user")) {
                                           if (tmp_arr.getJSONObject(i).getJSONObject("user").has("image_url")) {
                                               a_review.review_image = tmp_arr.getJSONObject(i).getJSONObject("user").getString("image_url");
                                           }
                                           if (tmp_arr.getJSONObject(i).getJSONObject("user").has("name")) {
                                               a_review.writer_name = tmp_arr.getJSONObject(i).getJSONObject("user").getString("name");
                                           }
                                       }
                                       a_review.author_url="";
                                       if (tmp_arr.getJSONObject(i).has("url"))
                                       a_review.author_url=tmp_arr.getJSONObject(i).getString("url");
                                       a_review.rating=0;
                                       if (tmp_arr.getJSONObject(i).has("rating"))
                                       a_review.rating=Float.parseFloat(tmp_arr.getJSONObject(i).getString("rating"));
                                       a_review.review_text="";
                                       if (tmp_arr.getJSONObject(i).has("text"))
                                       a_review.review_text=tmp_arr.getJSONObject(i).getString("text");
                                       a_review.index=i;
                                       if (tmp_arr.getJSONObject(i).has("time_created"))
                                       a_review.date_time=tmp_arr.getJSONObject(i).getString("time_created");
                                       else {
                                           a_review.date_time="2000-01-01 01:00:00";
                                       }
                                       try {
                                           a_review.time_stamp=dateToStamp(a_review.date_time);
                                       } catch (ParseException e) {
                                           e.printStackTrace();
                                       }
                                         yelp_reviews.add(a_review);

                                   }


                               } catch (JSONException e) {
                                   e.printStackTrace();
                               }
                           }

                     }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        volley_queue.add(stringRequest);
    }

    public void update_review_list(){
        String g_y_choice  = (String)review_g_y_spinner.getSelectedItem();
       if (g_y_choice.equals("Google reviews")){
           review_list.clear();
           review_list.addAll(google_reviews);
       } else {
           review_list.clear();
           review_list.addAll(yelp_reviews);
       }

        String sel_term= (String)review_ord_spinner.getSelectedItem();
        if (sel_term.equals("Default order")){
            Collections.sort(review_list, new Comparator<OneReview>() {
                @Override
                public int compare(OneReview o1, OneReview o2) {
                    return o1.index-o2.index;
                }
            });
        } else if (sel_term.equals("Highest rating")){
            Collections.sort(review_list, new Comparator<OneReview>() {
                @Override
                public int compare(OneReview o1, OneReview o2) {
                    return (int)o2.rating-(int)o1.rating;
                }
            });
        } else if (sel_term.equals("Lowest rating")){
            Collections.sort(review_list, new Comparator<OneReview>() {
                @Override
                public int compare(OneReview o1, OneReview o2) {
                    return (int)o1.rating-(int)o2.rating;
                }
            });
        }  else if (sel_term.equals("Most recent")){
            Collections.sort(review_list, new Comparator<OneReview>() {
                @Override
                public int compare(OneReview o1, OneReview o2) {
                    if (o2.time_stamp<o1.time_stamp) return -1;
                    else return 1;
                }
            });
        }  else if (sel_term.equals("Least recent")){
            Collections.sort(review_list, new Comparator<OneReview>() {
                @Override
                public int compare(OneReview o1, OneReview o2) {
                    if (o2.time_stamp>o1.time_stamp) return -1;
                    else return 1;
                }
            });
        }

     //   mRecyclerView.setAdapter(new ReviewRecycleAdapter(review_list));
         result_adapter.notifyDataSetChanged();
        LinearLayout no_review_lay=   views.get(3).findViewById(R.id.no_review_lay);
        LinearLayout  review_recycle_lay=(LinearLayout)  views.get(3).findViewById(R.id.review_recycle_lay);
        if (review_list.size()==0){
            no_review_lay.setVisibility(View.VISIBLE);
            review_recycle_lay.setVisibility(View.GONE);
        } else {
            no_review_lay.setVisibility(View.GONE);
            review_recycle_lay.setVisibility(View.VISIBLE);
        }
    }

    public void display_photos(){
        LinearLayout photo_layout = (LinearLayout) views.get(1).findViewById(R.id.photo_layout);
        photo_layout.removeAllViews();
        int i;
        for (i = 0; i < photo_array.size(); i++) {
            ImageView imageView = new ImageView(DetailInfoActivity.this);
            //     View.inflate(DetailInfoActivity.this, R.id.photo_layout, null).setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ViewGroup.LayoutParams img_lay_para = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //  img_lay_para.setMargins(0, 1, 0, 0);
            imageView.setLayoutParams(img_lay_para);
            imageView.setPadding(30, 30, 30, 30);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(photo_array.get(i));
            imageView.setAdjustViewBounds(true);
            photo_layout.addView(imageView);

        }

        ScrollView photo_scroll =  views.get(1).findViewById(R.id.photo_scroll);
        photo_scroll.setVisibility(View.VISIBLE);
        LinearLayout no_photo_lay = (LinearLayout) views.get(1).findViewById(R.id.no_photo_lay);
        no_photo_lay.setVisibility(View.GONE);
    }
}
