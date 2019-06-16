package dev.sevtix.horizonmonitor;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.MyViewHolder> {

    private List<Value> valuesList;
    private List<Value> oldValuesList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title, description, value;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);
            value = (TextView) view.findViewById(R.id.value);
        }
    }

    public ValueAdapter(List<Value> moviesList) {
        this.valuesList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.value_row_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Value value = valuesList.get(position);
        holder.title.setText(value.getTitle());
        holder.description.setText(value.getDescription());
        holder.value.setText(value.getValue());
    }


    @Override
    public int getItemCount() {
        return valuesList.size();
    }
}
