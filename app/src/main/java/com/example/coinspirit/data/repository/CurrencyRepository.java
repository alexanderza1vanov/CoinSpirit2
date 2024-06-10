package com.example.coinspirit.data.repository;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.coinspirit.data.model.CurrencyRVModel;
import com.example.coinspirit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class CurrencyRepository {
    private RequestQueue requestQueue;
    public CurrencyRepository(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }
    public void getCurrencyData(final OnCurrencyDataReceived onSuccess, final OnCurrencyDataError onError) {
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray dataArray = response.getJSONArray("data");
                            ArrayList<CurrencyRVModel> currencyList = new ArrayList<>();
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject dataObj = dataArray.getJSONObject(i);
                                String name = dataObj.getString("name");
                                String symbol = dataObj.getString("symbol");
                                JSONObject quote = dataObj.getJSONObject("quote");
                                JSONObject USD = quote.getJSONObject("USD");
                                double price = USD.getDouble("price");
                                currencyList.add(new CurrencyRVModel(name, symbol, price));
                            }
                            onSuccess.onReceived(currencyList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onError.onError("Fail to extract json data...");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        onError.onError("Fail to get the data...");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-CMC_PRO_API_KEY", "9dda162c-a631-4d4e-8d1b-78b5f1c0b67e");
                return headers;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
    public interface OnCurrencyDataReceived {
        void onReceived(ArrayList<CurrencyRVModel> currencyList);
    }
    public interface OnCurrencyDataError {
        void onError(String errorMessage);
    }
}
