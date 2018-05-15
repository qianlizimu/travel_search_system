package com.example.huangdi.cs572hw9;

import android.app.ProgressDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private List<OnePlace> myList;
    private OnItemClickListener mOnItemClickListener;
    private OnFavListener  mOnFavListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cate_iamge;
        TextView place_title;
        TextView place_address;
        ImageView place_fav_icon;
        LinearLayout place_item;

        public ViewHolder(View view) {
            super(view);
            cate_iamge=(ImageView) view.findViewById(R.id.cate_image);
            place_fav_icon=(ImageView) view.findViewById(R.id.place_fav_icon);
            place_title = (TextView) view.findViewById(R.id.place_title);
            place_address= (TextView) view.findViewById(R.id.place_address);

            place_item= (LinearLayout) view.findViewById(R.id.place_item);


        }
    }

    public MyRecyclerAdapter(List<OnePlace> aList) {
        this.myList = aList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        OnePlace list_item=myList.get(position);
        holder.place_title.setText(list_item.name);
        holder.place_address.setText(list_item.vicinity);
        Picasso.get().load(list_item.icon).into( holder.cate_iamge);

        if (list_item.isfav){
            holder.place_fav_icon.setImageResource(R.drawable.heart_fill_red);
        } else {
            holder.place_fav_icon.setImageResource(R.drawable.heart_outline_black);
        }

        if( mOnItemClickListener!= null){
            holder.place_item.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
                }
            });

        }

        if (mOnFavListener!=null){
            holder.place_fav_icon.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnFavListener.onClick(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    public interface OnItemClickListener{
        void onClick( int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener ){
        this.mOnItemClickListener=onItemClickListener;
    }

    public interface OnFavListener{
        void onClick(int position);
    }
    public void setOnFavListener(OnFavListener theFavListener ){
        this.mOnFavListener=theFavListener;
    }
}