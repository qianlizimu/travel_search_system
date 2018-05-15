package com.example.huangdi.cs572hw9;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

public class ResultTableActivity  extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private JSONArray mData;
    public List<OnePlace> data_list;
    public RequestQueue volley_queue;
    public String next_page_token;
    public ProgressDialog pd;
    public String third_page_token;
    public List<OnePlace> second_data_list;
    public List<OnePlace> third_data_list;
    public List<OnePlace> first_data_list;
    public JSONObject jsonObject;
    public int now_at_page;

    public MyRecyclerAdapter result_adapter;
    public SharedPreferences sp;
    public ArrayList<OnePlace> favorite_json=new ArrayList<>();
    public  Gson mygson= new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_table);
        String fav_str="Null";
  try {
      sp = getSharedPreferences("CS571", Context.MODE_PRIVATE);
       fav_str = sp.getString("favo_list", "Null");
  }catch (Exception e){
        System.out.println("hello world errorok:"+e.getMessage());
        }
       if (fav_str.equals("Null")==false) {
                   favorite_json = place_str_to_arr(fav_str);
       }

        volley_queue = Volley.newRequestQueue(this);
        pd = new ProgressDialog(this);

        Toolbar mToolbarTb = (Toolbar) findViewById(R.id.result_toolbar);
        mToolbarTb.setTitle("Search results");
        mToolbarTb.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(mToolbarTb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Button btn_previous=(Button) findViewById(R.id.btn_previous);
        btn_previous.setEnabled(false);
        final Button btn_next=(Button) findViewById(R.id.btn_next);

        Bundle bundle = this.getIntent().getExtras();
        String res_data = bundle.getString("res_data");
        now_at_page=1;

        jsonObject = null;
        data_list=new ArrayList<OnePlace>();
        first_data_list=new ArrayList<OnePlace>();
        second_data_list=new ArrayList<OnePlace>();
        third_data_list=new ArrayList<OnePlace>();

        int i, j;
        boolean has_next_page=false;
         next_page_token="";
         third_page_token="";
        try {
            jsonObject = new JSONObject(res_data);
            if (jsonObject.has("next_page_token")){
                has_next_page=true;
                next_page_token=jsonObject.getString("next_page_token");
                btn_next.setEnabled(true);
            } else {
                btn_next.setEnabled(false);
            }
            mData = jsonObject.getJSONArray("results");
            for ( i=0; i<mData.length(); i++){
                JSONObject one_json=mData.getJSONObject(i);
                OnePlace one_p = new OnePlace();
                one_p.name=one_json.getString("name");
                one_p.icon=one_json.getString("icon");
                one_p.place_id=one_json.getString("place_id");
                one_p.vicinity=one_json.getString("vicinity");
                one_p.isfav=false;
                for (j=0; j<favorite_json.size(); j++){
                    if (favorite_json.get(j).place_id.equals(one_p.place_id) ) {
                        one_p.isfav = true;
                        break;
                    }
                }
                first_data_list.add(one_p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        data_list.clear();
        data_list.addAll(first_data_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        result_adapter=new MyRecyclerAdapter( data_list);

        LinearLayout result_table_lay= findViewById(R.id.result_table_lay);
        LinearLayout no_result_lay= findViewById(R.id.no_result_lay);
        if (data_list.size()==0){
            no_result_lay.setVisibility(View.VISIBLE);
            result_table_lay.setVisibility(View.GONE);
        } else {
            no_result_lay.setVisibility(View.GONE);
            result_table_lay.setVisibility(View.VISIBLE);
        }

        result_adapter.setOnItemClickListener(new MyRecyclerAdapter.OnItemClickListener() {

            @Override
            public void onClick(int position) {

            OnePlace click_place;

            if (now_at_page==1){
                click_place=data_list.get(position);
            } else if (now_at_page==2) {
                click_place=second_data_list.get(position);
            } else {
                click_place=third_data_list.get(position);
            }
                Intent intent_detail = new Intent();
                intent_detail.setClass(ResultTableActivity.this, DetailInfoActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("place_id", click_place.place_id);
                bundle.putString("name", click_place.name);
                intent_detail.putExtras(bundle);
                startActivity(intent_detail);

            }
        });

        result_adapter.setOnFavListener(new MyRecyclerAdapter.OnFavListener() {
            @Override
            public void onClick(int position) {

                OnePlace click_place=data_list.get(position);

                if (click_place.isfav){
                    data_list.get(position).isfav=false;
                    for (int x=0; x<favorite_json.size(); x++){
                            if (favorite_json.get(x).place_id.equals(click_place.place_id)){
                            favorite_json.remove(x);
                                break;
                            }
                    }
                    Toast.makeText(ResultTableActivity.this,
                          click_place.name+" was removed from favorites",
                            Toast.LENGTH_LONG).show();
                } else {
                    data_list.get(position).isfav=true;
                favorite_json.add(data_list.get(position));
                    Toast.makeText(ResultTableActivity.this,
                            click_place.name+" was added to favorites",
                            Toast.LENGTH_LONG).show();
                }

                if (now_at_page==1){
                    first_data_list.set(position, data_list.get(position));
                } else if (now_at_page==2){
                    second_data_list.set(position, data_list.get(position));
                } else if (now_at_page==3){
                    third_data_list.set(position, data_list.get(position));
                }

                String str_tmp=mygson.toJson(favorite_json);

                result_adapter.notifyDataSetChanged();
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("favo_list", str_tmp);
                edit.commit();
            }
        });

        mRecyclerView.setAdapter( result_adapter);


        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url_token=next_page_token;
                if (now_at_page==2){
                    url_token=third_page_token;
                }

                if (now_at_page==1 && second_data_list.size()>0){
                 //   mRecyclerView.setAdapter(new MyRecyclerAdapter(second_data_list));
                    data_list.clear();
                    data_list.addAll(second_data_list);
                    result_adapter.notifyDataSetChanged();
                    btn_previous.setEnabled(true);
                    if (third_page_token.length()>0){
                        btn_next.setEnabled(true);
                    } else {
                        btn_next.setEnabled(false);
                    }
                    now_at_page=2;
                    return;
                }

                if (now_at_page==2 && third_data_list.size()>0){
                 //   mRecyclerView.setAdapter(new MyRecyclerAdapter(third_data_list));
                    data_list.clear();
                    data_list.addAll(third_data_list);
                    result_adapter.notifyDataSetChanged();
                    btn_previous.setEnabled(true);
                        btn_next.setEnabled(false);
                    now_at_page=3;
                    return;
                }

                pd.setMessage("Fetching next page");
                pd.show();

                String next_page_req_str="http://cs571hw-env.us-east-2.elasticbeanstalk.com/next_page?token=" +url_token;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, next_page_req_str,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                pd.cancel();

                                int i;
                                try {
                                    jsonObject = new JSONObject(response);
                                    mData = jsonObject.getJSONArray("results");
                                    for ( i=0; i<mData.length(); i++){
                                        JSONObject one_json=mData.getJSONObject(i);
                                        OnePlace one_p = new OnePlace();
                                        one_p.name=one_json.getString("name");
                                        one_p.icon=one_json.getString("icon");
                                        one_p.place_id=one_json.getString("place_id");
                                        one_p.vicinity=one_json.getString("vicinity");
                                        one_p.isfav=false;
                                        for (int j=0; j<favorite_json.size(); j++){
                                            if (favorite_json.get(j).place_id.equals(one_p.place_id) ) {
                                                one_p.isfav = true;
                                                break;
                                            }
                                        }
                                        if (now_at_page==1) {
                                            second_data_list.add(one_p);
                                        } else {
                                            third_data_list.add(one_p);
                                        }
                                    }

                                    if (jsonObject.has("next_page_token")){
                                        third_page_token=jsonObject.getString("next_page_token");
                                        btn_next.setEnabled(true);
                                    } else {
                                        btn_next.setEnabled(false);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (now_at_page==1) {
                               //     mRecyclerView.setAdapter(new MyRecyclerAdapter(second_data_list));
                                    data_list.clear();
                                    data_list.addAll(second_data_list);
                                    result_adapter.notifyDataSetChanged();
                                    now_at_page=2;
                                  } else {
                             //       mRecyclerView.setAdapter(new MyRecyclerAdapter(third_data_list));
                                    data_list.clear();
                                    data_list.addAll(third_data_list);
                                    result_adapter.notifyDataSetChanged();
                                    now_at_page=3;
                                }

                                btn_previous.setEnabled(true);
                                }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                volley_queue.add(stringRequest);

            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (now_at_page==2){
              //    mRecyclerView.setAdapter(new MyRecyclerAdapter(data_list));
                  data_list.clear();
                  data_list.addAll(first_data_list);
                  result_adapter.notifyDataSetChanged();
                  now_at_page=1;
                  btn_previous.setEnabled(false);
                  btn_next.setEnabled(true);
                  return;
              } else {  //now_at_page==3
              //    mRecyclerView.setAdapter(new MyRecyclerAdapter(second_data_list));
                  data_list.clear();
                  data_list.addAll(second_data_list);
                  result_adapter.notifyDataSetChanged();
                  now_at_page=2;
                  btn_previous.setEnabled(true);
                  btn_next.setEnabled(true);
                  return;
              }
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<OnePlace> place_str_to_arr(String favo_str){
        ArrayList<OnePlace> res=new ArrayList<>();
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

    @Override
    protected void onResume() {
        super.onResume();
        String fav_str = sp.getString("favo_list", "Null");
        if (fav_str.equals("Null")==false) {
            favorite_json = place_str_to_arr(fav_str);
        } else {
            favorite_json=new ArrayList<>();
        }
        int i,j;
        boolean judge_flag;
        for (i=0; i<data_list.size(); i++){
            judge_flag=false;
            String i_place_id=data_list.get(i).place_id;
            for (j=0; j<favorite_json.size(); j++){
                if (favorite_json.get(j).place_id.equals(i_place_id)){
                    judge_flag=true;
                    break;
                }
            }
            if (judge_flag){
                data_list.get(i).isfav=true;
            } else {
                data_list.get(i).isfav=false;
            }
        }

        if (now_at_page==1){
            first_data_list.clear();
            first_data_list.addAll(data_list);
        } else if (now_at_page==2) {
            second_data_list.clear();
            second_data_list.addAll(data_list);
        } else {
            third_data_list.clear();
            third_data_list.addAll(data_list);
        }

        result_adapter.notifyDataSetChanged();
    }


}
