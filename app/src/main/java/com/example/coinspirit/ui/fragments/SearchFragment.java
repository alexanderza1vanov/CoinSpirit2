package com.example.coinspirit.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.coinspirit.ui.adapters.CurrencyRVAdapter;
import com.example.coinspirit.data.model.CurrencyRVModel;
import com.example.coinspirit.R;
import com.example.coinspirit.data.repository.CurrencyRepository;
import java.util.ArrayList;
import java.util.Locale;

public class SearchFragment extends DialogFragment implements CurrencyRVAdapter.OnItemClickListener {

    private EditText searchEt;
    private RecyclerView currenciesRV;
    private ProgressBar loadingPB;
    private ArrayList<CurrencyRVModel> currencyRVModelArrayList;
    private CurrencyRVAdapter currencyRVAdapter;
    private CurrencyRepository currencyRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchEt = view.findViewById(R.id.EdtSearch);
        currenciesRV = view.findViewById(R.id.RVCurrencies);
        loadingPB = view.findViewById(R.id.PBLoading);
        currencyRVModelArrayList = new ArrayList<>();
        currencyRVAdapter = new CurrencyRVAdapter(currencyRVModelArrayList, requireContext(), this);
        currenciesRV.setLayoutManager(new LinearLayoutManager(getContext()));
        currenciesRV.setAdapter(currencyRVAdapter);
        currencyRepository = new CurrencyRepository(requireContext());

        getCurrencyData();

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterCurrencies(s.toString());
            }
        });

        return view;
    }

    private void filterCurrencies(String currency) {
        ArrayList<CurrencyRVModel> filteredList = new ArrayList<>();
        for (CurrencyRVModel item : currencyRVModelArrayList) {
            if (item.getName().toLowerCase(Locale.ROOT).contains(currency.toLowerCase(Locale.ROOT))) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "Криптовалюта по вашему запросу не найдена", Toast.LENGTH_SHORT).show();
        } else {
            currencyRVAdapter.filterList(filteredList);
        }
    }

    private void getCurrencyData() {
        loadingPB.setVisibility(View.VISIBLE);
        currencyRepository.getCurrencyData(
                currencyList -> {
                    loadingPB.setVisibility(View.GONE);
                    currencyRVModelArrayList.addAll(currencyList);
                    currencyRVAdapter.notifyDataSetChanged();
                },
                errorMessage -> {
                    loadingPB.setVisibility(View.GONE);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void onItemClick(CurrencyRVModel currency) {
        CurrencyDetailFragment dialogFragment = CurrencyDetailFragment.newInstance(currency.getName(), currency.getSymbol(), currency.getPrice());
        dialogFragment.show(getParentFragmentManager(), "currency_detail_dialog");
    }

    private void showCurrencyDetailFragment(String name, String symbol, double price) {
        CurrencyDetailFragment fragment = CurrencyDetailFragment.newInstance(name, symbol, price);
        fragment.show(getParentFragmentManager(), "currency_detail");
    }
}
