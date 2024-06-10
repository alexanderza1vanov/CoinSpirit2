package com.example.coinspirit.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coinspirit.R;
import com.example.coinspirit.data.model.CurrencyRVModel;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CurrencyRVAdapter extends RecyclerView.Adapter<CurrencyRVAdapter.ViewHolder> {

    private ArrayList<CurrencyRVModel> currencyRVModelArrayList;
    private final Context context;
    private final OnItemClickListener onItemClickListener;
    private final DecimalFormat df2 = new DecimalFormat("#.##");

    public CurrencyRVAdapter(ArrayList<CurrencyRVModel> currencyRVModelArrayList, Context context, OnItemClickListener onItemClickListener) {
        this.currencyRVModelArrayList = currencyRVModelArrayList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    public void filterList(ArrayList<CurrencyRVModel> filteredList) {
        currencyRVModelArrayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.currency_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CurrencyRVModel currencyRVModel = currencyRVModelArrayList.get(position);
        holder.tvName.setText(currencyRVModel.getName());
        holder.tvSymbol.setText(currencyRVModel.getSymbol());
        holder.tvCurrencyRate.setText("$ " + df2.format(currencyRVModel.getPrice()));

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(currencyRVModel));
    }

    @Override
    public int getItemCount() {
        return currencyRVModelArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvSymbol;
        TextView tvCurrencyRate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.TVName);
            tvSymbol = itemView.findViewById(R.id.TVSymbol);
            tvCurrencyRate = itemView.findViewById(R.id.TVCurrencyRate);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(CurrencyRVModel currency);
    }
}
