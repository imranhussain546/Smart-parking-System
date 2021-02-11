package com.imran.parkingsystem.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imran.parkingsystem.R;
import com.imran.parkingsystem.module.Book;

import java.util.List;

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.MyViewHolder>
{
    private List<Book> bookList;
    public ParkingHistoryAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parking_history_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.towneruid.setText(book.getOwneruid());
        holder.tdate.setText(book.getDate());
        holder.tprice.setText(book.getTotalprice());

    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView towneruid, tdate, tprice;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            towneruid = itemView.findViewById(R.id.parking_history_owneruid);
            tdate = itemView.findViewById(R.id.parking_history_date);
            tprice = itemView.findViewById(R.id.parking_history_price);
        }
    }
}
