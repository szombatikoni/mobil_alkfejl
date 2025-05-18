
package com.example.mobil_alkfejl_1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Product> productList;
    private ProductAdapter adapter;
    public static ArrayList<Product> cartList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        loadProductsFromFirestore();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        findViewById(R.id.btnCart).startAnimation(fadeIn);

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        loadProductsFromFirestore();

        findViewById(R.id.btnCheap).setOnClickListener(v -> {
            loadCheapProducts();
        });

        findViewById(R.id.btnAll).setOnClickListener(v -> {
            loadProductsFromFirestore();
        });
    }

    private void loadProductsFromFirestore() {
        db.collection("products")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    productList.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        String name = doc.getString("name");
                        Long priceLong = doc.getLong("price");
                        int price = (priceLong != null) ? priceLong.intValue() : 0;
                        productList.add(new Product(name, price));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Hiba a termékek betöltésekor.", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Hiba:", task.getException());
                }
            });
    }

    private void loadFilteredProducts() {
        db.collection("products")
                .whereGreaterThan("price", 200)
                .orderBy("price")
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            Long priceLong = doc.getLong("price");
                            int price = (priceLong != null) ? priceLong.intValue() : 0;
                            productList.add(new Product(name, price));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Hiba a szűrt termékek betöltésekor.", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Lekérdezés hiba:", task.getException());
                    }
                });
    }

    private void loadCheapProducts() {
        db.collection("products")
                .whereLessThanOrEqualTo("price", 300)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        Long priceLong = doc.getLong("price");
                        int price = (priceLong != null) ? priceLong.intValue() : 0;
                        productList.add(new Product(name, price));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    public void onAddToCart(Product product) {
        cartList.add(product);
        Toast.makeText(this, product.getName() + " hozzáadva a kosárhoz", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            startActivity(new Intent(this, HomeActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
