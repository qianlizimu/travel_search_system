package com.example.huangdi.cs572hw9;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private ViewPager pager;
    private List<View> views;
    private List<TextView> tvs = new ArrayList<TextView>();
    public HashMap<String, String> cates;
   // public PlaceAutoCompleteAdapter adapter;
    public ProgressDialog pd;
    public  RequestQueue volley_queue;
    public GoogleApiClient mGoogleApiClient;
    public double here_lat;
    public double here_lng;

    public RecyclerView mRecyclerView;
    public MyRecyclerAdapter favo_adapter;
    public ArrayList<OnePlace> favorite_list=new ArrayList<>();

    public SharedPreferences sp;
    public  Gson mygson= new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        favo_adapter=new MyRecyclerAdapter(favorite_list);
     //   getSupportActionBar().setElevation(0);
     //   setTitle("Places Search");
         try {
             sp = getSharedPreferences("CS571", Context.MODE_PRIVATE);
         } catch(Exception e) {
             System.out.println("hello world error: "+e.getMessage());
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION },
                  1);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, 0, this)
                .build();



        initTextView();
        initView();
        initViewPager();

        cates=new HashMap<String, String>();
        cates.put("Default","default");
        cates.put("Airport","airport");
        cates.put("Amusement Park","amusement_park");
        cates.put("Aquarium","aquarium");
        cates.put("Art Gallery","art_gallery");
        cates.put("Bakery","bakery");
        cates.put("Bar","bar");
        cates.put("Beauty Salon","beauty_salon");
        cates.put("Bowling Alley","bowling_alley");
        cates.put("Bus Station","bus_station");
        cates.put("Cafe","cafe");
        cates.put("Campground","campground");
        cates.put("Car Rental","car_rental");
        cates.put("Casino","casino");
        cates.put("Lodging","lodging");
        cates.put("Movie Theater","movie_theater");
        cates.put("Museum","museum");
        cates.put("Night Club","night_club");
        cates.put("Park","park");
        cates.put("Parking","parking");
        cates.put("Restaurant","restaurant");
        cates.put("Shopping Mall","shopping_mall");
        cates.put("Stadium","stadium");
        cates.put("Subway Station","subway_stations");
        cates.put("Taxi Stand","taxi_stand");
        cates.put("Train Station","train_station");
        cates.put("Transit Station","transit_station");
        cates.put("Travel Agency","travel_agency");
        cates.put("Zoo","zoo");


       //  adapter =  new PlaceAutoCompleteAdapter(this);
       pd = new ProgressDialog(this);

     //   RequestQueue mQueue = Volley.newRequestQueue(context);
        volley_queue = Volley.newRequestQueue(this);



        LocationListener locationListener = new LocationListener() {
              @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
             @Override
            public void onProviderEnabled(String provider) {

            }
             @Override
            public void onProviderDisabled(String provider) {
            }
            @Override
            public void onLocationChanged(Location location) {
            }
        };

        boolean get_cur_flag=false;
        here_lng=-118.285;
        here_lat=34.0224; // these are only default values, now I use Android locationManager to get the device latitude and longitude
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                get_cur_flag=true;
                here_lat = location.getLatitude();
                here_lng = location.getLongitude();
            }
        }
        if (get_cur_flag==false){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                here_lat = location.getLatitude();
                here_lng= location.getLongitude();
            }
        }

    }

    public void initTextView() {
        // TODO Auto-generated method stub
        TextView tv1 = (TextView) findViewById(R.id.tv1);
        TextView tv2 = (TextView) findViewById(R.id.tv2);
          //OnClickListener click=new MyClickListener();
        tv1.setOnClickListener(new MyClickListener(0));
        tv2.setOnClickListener(new MyClickListener(1));
        tvs.add(tv1);
        tvs.add(tv2);
    }

    private class MyClickListener implements View.OnClickListener {

        private int index;

        public MyClickListener(int index) {
            // TODO Auto-generated constructor stub
            this.index = index;
        }

        @Override
            public void onClick(View v) {
            // TODO Auto-generated method stub
            pager.setCurrentItem(index);
        }
    }

    public void initView() {
        // TODO Auto-generated method stub
        views = new ArrayList<View>();
        LayoutInflater li = getLayoutInflater();
        views.add(li.inflate(R.layout.f1, null));
        views.add(li.inflate(R.layout.favorite_page, null));
    }


    public void initViewPager() {
        // TODO Auto-generated method stub
        pager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter adapter = new MyPagerAdapter();
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int index) {
                // TODO Auto-generated method stub
                for (int i = 0; i < tvs.size(); i++) {
                    if (i == index) {
                        tvs.get(i).setBackgroundResource(R.drawable.bottom_red_border);
                       tvs.get(i).setTextColor(0xFFFFFFFF);
                    } else {
                        if (i==0) {
                            tvs.get(i).setBackgroundResource(R.drawable.right_border);
                        } else {
                            tvs.get(i).setBackgroundResource(R.drawable.left_border);
                        }
                        tvs.get(i).setTextColor(0xDCFFFFFF);
                    }
                }

                if (index==1){
                    String favo_str = sp.getString("favo_list", "Null");
                    if (favo_str.equals("Null")) {
                        favorite_list.clear();
                    } else {
                        favorite_list.clear();
                        favorite_list.addAll(place_str_to_arr(favo_str));
                    }
                    favo_adapter.notifyDataSetChanged();
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
            //return super.instantiateItem(container, position);
             if (position==0){

                // AutoCompleteTextView acTextView = (AutoCompleteTextView) views.get(position).findViewById(R.id.autotextView);
                // ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.f1 );
                // acTextView.setAdapter(adapter);
               //      AutoCompleteTextView searchPlace = (AutoCompleteTextView) views.get(position).findViewById(R.id.autotextView);
                 //    searchPlace.setAdapter(adapter);
                 final TextView keyword_reminder= (TextView) views.get(position).findViewById(R.id.keyword_reminder);
                 keyword_reminder.setVisibility(View.GONE);
                 final TextView location_reminder= (TextView) views.get(position).findViewById(R.id.location_reminder);
                 location_reminder.setVisibility(View.GONE);
                 final AutoCompleteTextView location_input =  views.get(position).findViewById(R.id.location_input);
                 final RadioGroup from_rg=(RadioGroup) views.get(position).findViewById(R.id.from_rg);

                 final EditText keyword_input = (EditText) views.get(position).findViewById(R.id.keyword_input);
                 final EditText input_distance= (EditText) views.get(position).findViewById(R.id.input_distance);
                 final Spinner category_spinner= (Spinner) views.get(position).findViewById(R.id.category_spinner);

                 Button btn_search = (Button) views.get(position).findViewById(R.id.btn_search);
                 btn_search.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         String input_keyword_str=keyword_input.getText().toString();
                         int i;
                         boolean kw_flag=false;
                         for (i=0; i<input_keyword_str.length(); i++){
                             if (input_keyword_str.charAt(i)!=' ') {
                                 kw_flag=true;
                                 break;
                             }
                         }
                         if (kw_flag==false) {
                             keyword_reminder.setVisibility(View.VISIBLE);
                         } else {
                             keyword_reminder.setVisibility(View.GONE);
                         }

                         boolean loca_flag=true;
                         String input_location_str="";
                         if (from_rg.getCheckedRadioButtonId()==R.id.from_other) {
                             loca_flag=false;
                             input_location_str = location_input.getText().toString();
                             for (i = 0; i < input_location_str.length(); i++) {
                                 if (input_location_str.charAt(i) != ' ') {
                                     loca_flag = true;
                                     break;
                                 }
                             }
                             if (loca_flag == false) {
                                 location_reminder.setVisibility(View.VISIBLE);
                             } else {
                                 location_reminder.setVisibility(View.GONE);
                             }
                         }

                         if (kw_flag==false || loca_flag==false){
                             Toast.makeText(MainActivity.this, "Please fix all fields with errors", Toast.LENGTH_LONG).show();
                         } else {
                             pd.setMessage("Fetching results");
                             pd.show();
                             String input_cat=cates.get( (String)category_spinner.getSelectedItem() );
                             Double dis_value=10.0;
                             try {
                                 dis_value = Double.parseDouble(input_distance.getText().toString());
                             }catch(NumberFormatException e){
                                 dis_value=10.0;
                             }
                             dis_value=1609.344*dis_value;

                             String req_url="";
                             if (from_rg.getCheckedRadioButtonId()==R.id.from_other){
                                 try {
                                     req_url= "http://cs571hw-env.us-east-2.elasticbeanstalk.com/submit_form?keyword=" + URLEncoder.encode(input_keyword_str, "utf8") +
                                             "&categorys=" + input_cat + "&distance=" + String.valueOf(dis_value) + "&fromplace=other&input_location=" + URLEncoder.encode(input_location_str, "utf8");
                                 } catch (UnsupportedEncodingException e) {
                                     e.printStackTrace();
                                 }
                             } else {
                                 try {
                                     req_url="http://cs571hw-env.us-east-2.elasticbeanstalk.com/submit_form?keyword=" + URLEncoder.encode(input_keyword_str, "utf8")
                                + "&categorys=" + input_cat + "&distance=" + String.valueOf(dis_value) + "&fromplace=here&here_lat=" + String.valueOf(here_lat) + "&here_lon=" + String.valueOf(here_lng);
                                 } catch (UnsupportedEncodingException e) {
                                     e.printStackTrace();
                                 }
                             }

                           //  Toast.makeText(MainActivity.this, String.valueOf(here_lat)+","+String.valueOf(here_lng), Toast.LENGTH_SHORT).show();
                             //          req_url ="http://www.google.com";
                             StringRequest stringRequest = new StringRequest(Request.Method.GET, req_url,
                                     new Response.Listener<String>() {
                                         @Override
                                         public void onResponse(String response) {
                                                       pd.cancel();

                                                 Intent intent_result = new Intent();
                                                 intent_result.setClass(MainActivity.this, ResultTableActivity.class);
                                                 Bundle bundle=new Bundle();
                                                 bundle.putString("res_data", response);
                                                  intent_result.putExtras(bundle);
                                                 startActivity(intent_result);

                                             //            Toast.makeText(MainActivity.this, response.substring(0,500), Toast.LENGTH_LONG).show();
                                         }
                                     }, new Response.ErrorListener() {
                                 @Override
                                 public void onErrorResponse(VolleyError error) {
                                     pd.cancel();
                                     Toast.makeText(MainActivity.this, "Failure of the Network or API", Toast.LENGTH_LONG).show();
                                 }
                             });
                            volley_queue.add(stringRequest);
                         }

                     }
                 });

                 //   final TextView keyword_text=(TextView)views.get(position).findViewById(R.id.keyword_text);
                 from_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                     @Override
                     public void onCheckedChanged(RadioGroup group, int checkedId){
                         if (checkedId==R.id.from_current){
                             location_input.setEnabled(false);
                             location_reminder.setVisibility(View.GONE);
                         } else {
                             location_input.setEnabled(true);
                         }
                     }
                 });


                 Button btn_clear = (Button) views.get(position).findViewById(R.id.btn_clear);
                 btn_clear.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                             keyword_reminder.setVisibility(View.GONE);
                             location_reminder.setVisibility(View.GONE);
                         location_input.setText("");
                         keyword_input.setText("");
                         category_spinner.setSelection(0);
                         input_distance.setText("");
                         location_input.setEnabled(false);
                         from_rg.check(R.id.from_current);
                     }
                 });


                 AutoCompleteTextView searchPlace = views.get(position).findViewById(R.id.location_input);
                 CustomAutoCompleteAdapter adapter =  new CustomAutoCompleteAdapter(MainActivity.this);
                 searchPlace.setAdapter(adapter);
                 searchPlace.setOnItemClickListener(onItemClickListener);
             } else { //position==1
                    try {

                        String favo_str = sp.getString("favo_list", "Null");
                        if (favo_str.equals("Null")) {
                            favorite_list.clear();
                        } else {
                            favorite_list.clear();
                            favorite_list.addAll(place_str_to_arr(favo_str));
                        }

                        favo_adapter.notifyDataSetChanged();

                 mRecyclerView =  views.get(1).findViewById(R.id.favo_recycler);
                 LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                 layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                 mRecyclerView.setLayoutManager(layoutManager);

                decide_no_record();
                 favo_adapter.setOnItemClickListener(new MyRecyclerAdapter.OnItemClickListener() {
                     @Override
                     public void onClick(int position) {

                         OnePlace click_place=favorite_list.get(position);

                         Intent intent_detail = new Intent();
                         intent_detail.setClass(MainActivity.this, DetailInfoActivity.class);
                         Bundle bundle=new Bundle();
                         bundle.putString("place_id", click_place.place_id);
                         bundle.putString("name", click_place.name);
                         intent_detail.putExtras(bundle);
                         startActivity(intent_detail);
                     }
                 });

                 favo_adapter.setOnFavListener(new MyRecyclerAdapter.OnFavListener() {
                     @Override
                     public void onClick(int position) {
                         Toast.makeText(MainActivity.this, favorite_list.get(position).name+" was removed from favorites", Toast.LENGTH_LONG).show();

                         favorite_list.remove(position);
                            String str_tmp=mygson.toJson(favorite_list);

                         favo_adapter.notifyDataSetChanged();
                         SharedPreferences.Editor edit = sp.edit();
                         edit.putString("favo_list", str_tmp);
                         edit.commit();
                         decide_no_record();
                     }
                 });

                 mRecyclerView.setAdapter( favo_adapter );
                    } catch(Exception e){
                        System.out.println(" helloworld error: "+e.getMessage());
                    }

               //  findViewById(R.id.tv2).setAlpha(0.9f);
               TextView this_tv = findViewById(R.id.tv2);
                 this_tv.setTextColor(0xDCFFFFFF);
             }


            container.addView(views.get(position));
            return views.get(position);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
        MainActivity.this.finish();
    }

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            };

    static ArrayList<OnePlace> place_str_to_arr(String favo_str){
        ArrayList<OnePlace> res=new ArrayList<>();
        if (favo_str.length()==0) return res;
        try {
            JSONArray fav_json=new JSONArray(favo_str);
            for (int i=0; i<fav_json.length(); i++){
                OnePlace a_place=new OnePlace();
                a_place.icon=fav_json.getJSONObject(i).getString("icon");
                a_place.place_id=fav_json.getJSONObject(i).getString("place_id");
                a_place.name=fav_json.getJSONObject(i).getString("name");
                a_place.vicinity=fav_json.getJSONObject(i).getString("vicinity");
                a_place.isfav=fav_json.getJSONObject(i).getBoolean("isfav");
                res.add(a_place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void decide_no_record(){
        LinearLayout recycle_lay = views.get(1).findViewById(R.id.recycle_lay);
        LinearLayout no_favorite_lay = views.get(1).findViewById(R.id.no_favorite_lay);
        if (favorite_list.size()==0){
            recycle_lay.setVisibility(View.GONE);
            no_favorite_lay.setVisibility(View.VISIBLE);
        } else {
            recycle_lay.setVisibility(View.VISIBLE);
            no_favorite_lay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
         super.onResume();


        String fav_str = sp.getString("favo_list", "Null");
        if (fav_str.equals("Null")==false) {
            favorite_list.clear();
            favorite_list.addAll( place_str_to_arr(fav_str));
        } else {
            favorite_list.clear();
        }

        favo_adapter.notifyDataSetChanged();
        decide_no_record();
    }
}
