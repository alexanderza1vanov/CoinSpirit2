package com.example.coinspirit.ui.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.coinspirit.data.utils.PasswordUtils;
import com.example.coinspirit.ui.activitys.LoginActivity;
import com.example.coinspirit.databinding.ActivityRegisterBinding;
import com.example.coinspirit.ui.activitys.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.backBtn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        binding.signUpBtn.setOnClickListener(v -> {
            String email = binding.emailEt.getText().toString();
            String password = binding.passwordEt.getText().toString();
            String username = binding.usernameEt.getText().toString();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                // Хеширование пароля перед отправкой его в Firebase
                String hashedPassword = PasswordUtils.hashPassword(password);

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    String userId = user.getUid();
                                    DocumentReference userRef = db.collection("Users").document(userId);
                                    userRef.set(new User(email, username, hashedPassword))
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("RegisterActivity", "User data saved successfully");
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("RegisterActivity", "Failed to save user data", e);
                                                Toast.makeText(getApplicationContext(), "Failed to save user data. Please try again.", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                Log.e("RegisterActivity", "User registration failed", task.getException());
                                Toast.makeText(getApplicationContext(), "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    public static class User {
        private String email;
        private String username;
        private String hashedPassword;

        public User() {

        }

        public User(String email, String username, String hashedPassword) {
            this.email = email;
            this.username = username;
            this.hashedPassword = hashedPassword;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getHashedPassword() {
            return hashedPassword;
        }

        public void setHashedPassword(String hashedPassword) {
            this.hashedPassword = hashedPassword;
        }
    }
}
