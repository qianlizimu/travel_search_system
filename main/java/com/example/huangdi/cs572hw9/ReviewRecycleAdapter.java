package com.example.huangdi.cs572hw9;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ReviewRecycleAdapter extends RecyclerView.Adapter<ReviewRecycleAdapter.ViewHolder> {

    private List<OneReview> myList;
    private OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView review_image;
        TextView writer_name;
        TextView date_time;
        TextView review_text;
        LinearLayout place_item;
        RatingBar rating_text;
        LinearLayout review_out_lay;

        public ViewHolder(View view) {
            super(view);
            review_image=(ImageView) view.findViewById(R.id.review_image);

            writer_name = (TextView) view.findViewById(R.id.writer_name);
            date_time = (TextView) view.findViewById(R.id.date_time);
            review_text = (TextView) view.findViewById(R.id.review_text);
            rating_text=(RatingBar) view.findViewById(R.id.rating_text);
            review_out_lay= view.findViewById(R.id.review_out_lay);


        }
    }

    public ReviewRecycleAdapter(List<OneReview> aList) {
        this.myList = aList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        OneReview list_item=myList.get(position);
        holder.writer_name.setText(list_item.writer_name);
        holder.date_time.setText(list_item.date_time);
        holder.review_text.setText(list_item.review_text);
        if (list_item.review_image.length()>0) {
            Picasso.get().load(list_item.review_image).into(holder.review_image);
        }
        holder.rating_text.setRating(list_item.rating);

        if( mOnItemClickListener!= null){

            holder.review_out_lay.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(position);
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
}