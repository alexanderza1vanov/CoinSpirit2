package com.example.coinspirit.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coinspirit.R;
import com.example.coinspirit.data.model.CurrencyRVModel;
import com.example.coinspirit.data.model.PortfolioRVModel;
import com.example.coinspirit.data.repository.CurrencyRepository;
import com.example.coinspirit.ui.adapters.PortfolioRVAdapter;
import com.example.coinspirit.ui.fragments.CurrencyDetailFragment;
import com.example.coinspirit.ui.fragments.SearchFragment;
import com.example.coinspirit.ui.fragments.SettingsFragment;
import com.example.coinspirit.ui.interfaces.DataTransfer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DataTransfer {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private PortfolioRVAdapter portfolioRVAdapter;
    private List<PortfolioRVModel> portfolioList;
    private TextView totalValueTextView;
    private TextView profitTextView;
    private CurrencyRepository currencyRepository;
    private DecimalFormat df2 = new DecimalFormat("#.##");
    private long updateInterval = 5000L; // Interval in milliseconds (5 seconds)
    private Thread updateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.main_recyclerview);
        totalValueTextView = findViewById(R.id.value_portfolio_tv);
        profitTextView = findViewById(R.id.profit_portfolio_tv);
        portfolioList = new ArrayList<>();
        portfolioRVAdapter = new PortfolioRVAdapter(this, portfolioList);
        recyclerView.setAdapter(portfolioRVAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageView addImageView = findViewById(R.id.add_img);
        addImageView.setOnClickListener(view -> showSearchDialogFragment());

        ImageView settingsImageView = findViewById(R.id.settings_image);
        settingsImageView.setOnClickListener(view -> showSettingsDialogFragment());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currencyRepository = new CurrencyRepository(this);

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // User is not signed in, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // End the current activity
        } else {
            // Get the userId of the current user
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            // Retrieve user data from Firestore
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Map<String, Object> document = task.getResult().getData();
                    if (document != null && document.containsKey("username")) {
                        String username = document.get("username").toString();
                        Toast.makeText(this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Welcome, " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error getting user data.", Toast.LENGTH_SHORT).show();
                }
            });

            // Load transactions
            userRef.collection("Transactions").get()
                    .addOnSuccessListener(documents -> {
                        for (QueryDocumentSnapshot document : documents) {
                            PortfolioRVModel item = document.toObject(PortfolioRVModel.class);
                            portfolioList.add(item);
                        }
                        portfolioRVAdapter.notifyDataSetChanged();
                        updatePortfolioValue();
                        updatePortfolioProfit();
                        startUpdatingPrices(); // Start updating prices
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error loading transactions: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // Add ItemTouchHelper for deleting transactions on left swipe
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PortfolioRVModel deletedItem = portfolioList.get(position);

                // Remove item from list and notify adapter
                portfolioList.remove(position);
                portfolioRVAdapter.notifyItemRemoved(position);

                // Remove item from Firestore
                removeTransactionFromFirestore(deletedItem);

                // Update total value and profit
                updatePortfolioValue();
                updatePortfolioProfit();
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    // Remove transaction from Firestore
    private void removeTransactionFromFirestore(PortfolioRVModel transaction) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            userRef.collection("Transactions")
                    .whereEqualTo("name", transaction.getName())
                    .whereEqualTo("symbol", transaction.getSymbol())
                    .get()
                    .addOnSuccessListener(documents -> {
                        if (!documents.isEmpty()) {
                            for (QueryDocumentSnapshot document : documents) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Transaction " + document.getId() + " successfully deleted"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error deleting transaction " + document.getId(), e));
                            }
                            Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Firestore", "No matching transactions found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error getting documents", e);
                    });
        } else {
            Log.e("Firestore", "User is not authenticated");
        }
    }

    private void showSearchDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SearchFragment searchDialogFragment = new SearchFragment();
        searchDialogFragment.show(fragmentManager, "search_dialog");
    }

    private void showSettingsDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SettingsFragment settingsDialogFragment = SettingsFragment.newInstance();
        settingsDialogFragment.show(fragmentManager, "settings_dialog");
    }

    private void showCurrencyDetailDialog() {
        CurrencyDetailFragment dialogFragment = CurrencyDetailFragment.newInstance("Bitcoin", "BTC", 30000.0);
        dialogFragment.show(getSupportFragmentManager(), "CurrencyDetailDialog");
    }

    @Override
    public void onAddTransaction(String name, String symbol, String price, String quantity, String purchasePrice) {
        PortfolioRVModel newItem = new PortfolioRVModel(name, symbol, price, quantity, purchasePrice);
        portfolioList.add(newItem);
        portfolioRVAdapter.notifyDataSetChanged();
        updatePortfolioValue();
        updatePortfolioProfit();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("Users").document(userId);

            // Add transaction to the "Transactions" subcollection for the current user
            userRef.collection("Transactions").add(newItem)
                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error adding transaction: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void updatePortfolioValue() {
        double totalValue = 0.0;
        for (PortfolioRVModel item : portfolioList) {
            double price = item.getPrice().isEmpty() ? 0.0 : Double.parseDouble(item.getPrice());
            double quantity = item.getQuantity().isEmpty() ? 0.0 : Double.parseDouble(item.getQuantity());
            totalValue += price * quantity;
        }
        totalValueTextView.setText("$" + df2.format(totalValue));
    }

    private void updatePortfolioProfit() {
        double totalProfit = 0.0;
        for (PortfolioRVModel item : portfolioList) {
            double currentPrice = item.getPrice().isEmpty() ? 0.0 : Double.parseDouble(item.getPrice());
            double purchasePrice = item.getPurchasePrice().isEmpty() ? 0.0 : Double.parseDouble(item.getPurchasePrice());
            double quantity = item.getQuantity().isEmpty() ? 0.0 : Double.parseDouble(item.getQuantity());
            totalProfit += (currentPrice - purchasePrice) * quantity;
        }
        profitTextView.setText("$" + df2.format(totalProfit));
    }

    private void updateCryptoPrices() {
        currencyRepository.getCurrencyData(new CurrencyRepository.OnCurrencyDataReceived() {
            @Override
            public void onReceived(ArrayList<CurrencyRVModel> currencyList) {
                List<PortfolioRVModel> updatedPortfolioList = new ArrayList<>();
                for (PortfolioRVModel portfolioItem : portfolioList) {
                    double updatedPrice = portfolioItem.getPrice().isEmpty() ? 0.0 : Double.parseDouble(portfolioItem.getPrice());
                    for (CurrencyRVModel currency : currencyList) {
                        if (currency.getSymbol().equals(portfolioItem.getSymbol())) {
                            updatedPrice = currency.getPrice();
                            break;
                        }
                    }
                    portfolioItem.setPrice(String.valueOf(updatedPrice));
                    updatedPortfolioList.add(portfolioItem);
                }
                portfolioList.clear();
                portfolioList.addAll(updatedPortfolioList);
                portfolioRVAdapter.notifyDataSetChanged();
                updatePortfolioValue();
                updatePortfolioProfit();
            }
        }, new CurrencyRepository.OnCurrencyDataError() {
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startUpdatingPrices() {
        updateThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    runOnUiThread(this::updateCryptoPrices);
                    Thread.sleep(updateInterval);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        updateThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt(); // Прерывание потока при уничтожении активности
        }
    }
}
