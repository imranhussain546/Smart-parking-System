package com.imran.parkingsystem.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imran.parkingsystem.R;
import com.imran.parkingsystem.module.Feedback;
import com.imran.parkingsystem.module.Profile;

import java.util.List;

public class CustomFeedbackAdapter extends RecyclerView.Adapter<CustomFeedbackAdapter.ModuleViewHolder>
{
    private List<Feedback> feedbackList;
    private Profile ownerdetail;

    public CustomFeedbackAdapter(List<Feedback> feedbackList, Profile ownerdetail) {
        this.feedbackList = feedbackList;
        this.ownerdetail = ownerdetail;
    }

    private Context context;
    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.feedback_item, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position)
    {
        Feedback feedback = feedbackList.get(position);
        holder.townername.setText(ownerdetail.getName());
        holder.townermobile.setText(ownerdetail.getMobile());
        holder.trating.setRating(feedback.getRate());
        holder.tuserposted.setText(feedback.getRenteruid());
        holder.tmessage.setText("Message : " + feedback.getMessage());
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    class ModuleViewHolder extends RecyclerView.ViewHolder
    {
        TextView townername, townermobile, tuserposted, tmessage;
        RatingBar trating;
        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            townername = itemView.findViewById(R.id.name);
            townermobile = itemView.findViewById(R.id.mobile);
            trating = itemView.findViewById(R.id.ratingbar);
            tuserposted = itemView.findViewById(R.id.postedby);
            tmessage = itemView.findViewById(R.id.message);
        }
    }
}
