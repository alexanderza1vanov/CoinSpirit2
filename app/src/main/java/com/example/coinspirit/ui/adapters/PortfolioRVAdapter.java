package com.example.coinspirit.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coinspirit.R;
import com.example.coinspirit.data.model.PortfolioRVModel;

import java.text.DecimalFormat;
import java.util.List;

public class PortfolioRVAdapter extends RecyclerView.Adapter<PortfolioRVAdapter.PortfolioViewHolder> {

    private final Context context;
    private final List<PortfolioRVModel> portfolioItems;
    private final DecimalFormat df2 = new DecimalFormat("#.##");

    public PortfolioRVAdapter(Context context, List<PortfolioRVModel> portfolioItems) {
        this.context = context;
        this.portfolioItems = portfolioItems;
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_portfolio_rv, parent, false);
        return new PortfolioViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        PortfolioRVModel item = portfolioItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvSymbol.setText(item.getSymbol());

        // Преобразование строки в Double перед форматированием
        double price = item.getPrice() != null ? Double.parseDouble(item.getPrice()) : 0.0;
        holder.tvPrice.setText("$" + df2.format(price));

        holder.tvQuantity.setText(item.getQuantity());

        // Преобразование строки в Double перед форматированием
        double purchasePrice = item.getPurchasePrice() != null ? Double.parseDouble(item.getPurchasePrice()) : 0.0;
        holder.tvAvgPrice.setText("$" + df2.format(purchasePrice));
    }

    @Override
    public int getItemCount() {
        return portfolioItems.size();
    }

    public static class PortfolioViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvSymbol;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvAvgPrice;

        public PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.TVNameCurrency);
            tvSymbol = itemView.findViewById(R.id.TVSymbolCurrency);
            tvPrice = itemView.findViewById(R.id.TVCurrencyPrice);
            tvQuantity = itemView.findViewById(R.id.TVQuantity);
            tvAvgPrice = itemView.findViewById(R.id.TVAvgPrice);
        }
    }
}
