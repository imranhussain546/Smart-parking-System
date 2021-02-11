package com.imran.parkingsystem.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.imran.parkingsystem.R;
import com.imran.parkingsystem.module.Module;

import java.util.List;

public class ModuleAdapter  extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>
{
    private List<Module> moduleList;
    private MyModuleClickListener myModuleClickListener;

    public ModuleAdapter(List<Module> moduleList) {
        this.moduleList = moduleList;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.module_item, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module module = moduleList.get(position);
        holder.tname.setText(module.getName());
        holder.timage.setImageResource(module.getImage());
    }

    @Override
    public int getItemCount() {
    return moduleList.size();
}

    class ModuleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView tname;
        private ImageView timage;
        public ModuleViewHolder(@NonNull View itemView)
        {
            super(itemView);
            timage = itemView.findViewById(R.id.mylogoimage);
            tname = itemView.findViewById(R.id.mymodulename);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            myModuleClickListener.onModuleClick(view, getAdapterPosition());
        }
    }
    public void setOnMyModuleClickListener(MyModuleClickListener myModuleClickListener) {
        this.myModuleClickListener = myModuleClickListener;
    }

    public interface MyModuleClickListener {
        void onModuleClick(View view, int position);
    }
}
