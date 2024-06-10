package com.example.coinspirit.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.coinspirit.R;
import com.example.coinspirit.ui.interfaces.DataTransfer;

public class CurrencyDetailFragment extends DialogFragment {

    private String name;
    private String symbol;
    private double price;
    private DataTransfer dataTransferInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            dataTransferInterface = (DataTransfer) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DataTransfer");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            symbol = getArguments().getString(ARG_SYMBOL);
            price = getArguments().getDouble(ARG_PRICE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency_detail, container, false);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvSymbol = view.findViewById(R.id.tvSymbol);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        EditText etQuantity = view.findViewById(R.id.etQuantity);
        EditText etPurchasePrice = view.findViewById(R.id.etPurchasePrice);
        Button btnAddTransaction = view.findViewById(R.id.btnAddTransaction);

        tvName.setText(name);
        tvSymbol.setText(symbol);
        tvPrice.setText(String.format("$ %.2f", price));

        btnAddTransaction.setOnClickListener(v -> {
            String quantity = etQuantity.getText().toString();
            String purchasePrice = etPurchasePrice.getText().toString();

            if (!quantity.isEmpty() && !purchasePrice.isEmpty()) {
                dataTransferInterface.onAddTransaction(name, symbol, String.valueOf(price), quantity, purchasePrice);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public static final String ARG_NAME = "name";
    public static final String ARG_SYMBOL = "symbol";
    public static final String ARG_PRICE = "price";

    public static CurrencyDetailFragment newInstance(String name, String symbol, double price) {
        CurrencyDetailFragment fragment = new CurrencyDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_SYMBOL, symbol);
        args.putDouble(ARG_PRICE, price);
        fragment.setArguments(args);
        return fragment;
    }
}
