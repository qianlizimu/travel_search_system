package com.example.huangdi.cs572hw9;

// this part of codes are cited from the reference website: http://www.zoftino.com/google-places-auto-complete-android
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class CustomAutoCompleteAdapter extends ArrayAdapter {
    public static final String TAG = "CustomAutoCompAdapter";
    private List<MyPlace> dataList;
    private Context mContext;
    private GeoDataClient geoDataClient;

    private CustomAutoCompleteAdapter.CustomAutoCompleteFilter listFilter =
            new CustomAutoCompleteAdapter.CustomAutoCompleteFilter();

//simple_dropdown_item_1line
    public CustomAutoCompleteAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1,
                new ArrayList<MyPlace>());
        mContext = context;

        //get GeoDataClient
        geoDataClient = Places.getGeoDataClient(mContext, null);


    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public MyPlace getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        TextView textOne;



        if (position!=dataList.size()-1 ){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1,
                            parent, false);
            textOne = view.findViewById(android.R.id.text1);
            textOne.setText(dataList.get(position).placeText);
        }   else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.autocomplete_google_layout,
                            parent, false);
         //   ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        }



        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    public class CustomAutoCompleteFilter extends Filter {
        private Object lock = new Object();
        private Object lockTwo = new Object();
        private boolean placeResults = false;


        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            placeResults = false;
            final List<MyPlace> placesList = new ArrayList<>();

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    results.values = new ArrayList<MyPlace>();
                    results.count = 0;
                }
            } else {
                final String searchStrLowerCase = prefix.toString().toLowerCase();

                Task<AutocompletePredictionBufferResponse> task
                        = getAutoCompletePlaces(searchStrLowerCase);

                task.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<AutocompletePredictionBufferResponse> task) {
                        if (task.isSuccessful()) {
                         //   Log.d(TAG, "Auto complete prediction successful");
                            AutocompletePredictionBufferResponse predictions = task.getResult();
                            MyPlace autoPlace = new MyPlace();
                            for (AutocompletePrediction prediction : predictions) {
                                autoPlace = new MyPlace();
                                autoPlace.placeId=prediction.getPlaceId();
                                autoPlace.placeText=prediction.getFullText(null).toString();
                                autoPlace.name=prediction.getPrimaryText(null).toString();
                                placesList.add(autoPlace);
                            }

                            predictions.release();
                    //        Log.d(TAG, "Auto complete predictions size " + placesList.size());
                        } else {
                     //       Log.d(TAG, "Auto complete prediction unsuccessful");
                        }
                        //inform waiting thread about api call completion
                        placeResults = true;
                        synchronized (lockTwo) {
                            lockTwo.notifyAll();
                        }
                    }
                });

                //wait for the results from asynchronous API call
                while (!placeResults) {
                    synchronized (lockTwo) {
                        try {
                            lockTwo.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }
                results.values = placesList;
                results.count = placesList.size();
                Log.d(TAG, "Autocomplete predictions size after wait" + results.count);
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                dataList = (ArrayList<MyPlace>) results.values;

                MyPlace autoPlace = new MyPlace();
                       autoPlace.placeId="hello";
                            autoPlace.placeText="google";
                            dataList.add(autoPlace);
            } else {
                dataList = null;
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        private Task<AutocompletePredictionBufferResponse> getAutoCompletePlaces(String query) {
            //create autocomplete filter using data from filter Views
            AutocompleteFilter.Builder filterBuilder = new AutocompleteFilter.Builder();
        //    filterBuilder.setCountry(country.getText().toString());

            Task<AutocompletePredictionBufferResponse> results =
                    geoDataClient.getAutocompletePredictions(query, null,
                            filterBuilder.build());
            return results;
        }
    }
}