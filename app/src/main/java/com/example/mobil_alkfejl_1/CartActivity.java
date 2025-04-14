package com.example.mobil_alkfejl_1;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class CartActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> items = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        listView = findViewById(R.id.cartListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        loadCartFromFirestore();
    }

    private void loadCartFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    int total = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        Long priceLong = doc.getLong("price");
                        Long quantityLong = doc.getLong("quantity");

                        int price = (priceLong != null) ? priceLong.intValue() : 0;
                        int quantity = (quantityLong != null) ? quantityLong.intValue() : 1;
                        int subtotal = price * quantity;

                        total += subtotal;
                        items.add(name + " - " + quantity + " db - " + price + " Ft/db");
                    }
                    items.add("Összesen: " + total + " Ft");
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Hiba a kosár betöltésekor", e);
                    Toast.makeText(this, "Hiba a kosár betöltésekor.", Toast.LENGTH_SHORT).show();
                });
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