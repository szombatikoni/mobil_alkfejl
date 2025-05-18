
package com.example.mobil_alkfejl_1;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cartRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);

        loadCartFromFirestore();

        findViewById(R.id.btnOrder).setOnClickListener(v -> showOrderDialog());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadCartFromFirestore() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        Long priceLong = doc.getLong("price");
                        Long quantityLong = doc.getLong("quantity");

                        int price = (priceLong != null) ? priceLong.intValue() : 0;
                        int quantity = (quantityLong != null) ? quantityLong.intValue() : 1;

                        cartItems.add(new CartItem(name, price, quantity));
                    }

                    adapter.notifyDataSetChanged();

                    Button btnOrder = findViewById(R.id.btnOrder);
                    btnOrder.setVisibility(cartItems.isEmpty() ? Button.GONE : Button.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Hiba a kosár betöltésekor", e);
                    Toast.makeText(this, "Hiba a kosár betöltésekor.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showOrderDialog() {
        String[] futarok = {"GLS - 1190 Ft", "MPL - 990 Ft", "FoxPost - 1590 Ft"};

        new AlertDialog.Builder(this)
                .setTitle("Válassz futárt")
                .setItems(futarok, (dialog, which) -> {
                    final int szallitasiKoltseg;
                    switch (which) {
                        case 0: szallitasiKoltseg = 1190; break;
                        case 1: szallitasiKoltseg = 990; break;
                        case 2: szallitasiKoltseg = 1590; break;
                        default: szallitasiKoltseg = 0;
                    }

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String uid = auth.getCurrentUser().getUid();
                    String email = auth.getCurrentUser().getEmail();

                    db.collection("users")
                            .document(uid)
                            .collection("cart")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                StringBuilder tartalom = new StringBuilder();
                                int osszesen = 0;

                                for (QueryDocumentSnapshot doc : snapshot) {
                                    String name = doc.getString("name");
                                    int price = doc.getLong("price").intValue();
                                    int qty = doc.getLong("quantity").intValue();
                                    int subtotal = price * qty;

                                    tartalom.append(name)
                                            .append(" - ")
                                            .append(qty)
                                            .append(" kg - ")
                                            .append(subtotal)
                                            .append(" Ft\n");

                                    osszesen += subtotal;
                                }

                                osszesen += szallitasiKoltseg;

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
                                sdf.setTimeZone(TimeZone.getTimeZone("Europe/Budapest"));
                                String datum = sdf.format(new Date());

                                new AlertDialog.Builder(this)
                                        .setTitle("Rendelés összefoglaló")
                                        .setMessage("Felhasználó: " + email + "\n\n" +
                                                "Termékek:\n" + tartalom +
                                                "\nSzállítás: " + szallitasiKoltseg + " Ft" +
                                                "\nVégösszeg: " + osszesen + " Ft" +
                                                "\nDátum: " + datum)
                                        .setPositiveButton("OK", (d, w) -> {
                                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                                doc.getReference().delete();
                                            }

                                            cartItems.clear();
                                            adapter.notifyDataSetChanged();

                                            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "order_channel")
                                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                    .setContentTitle("Rendelés leadva")
                                                    .setContentText("Sikeres rendelés: " + osszesen + " Ft")
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH);

                                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                                    checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                                NotificationManagerCompat.from(this).notify(1010, builder.build());
                                            }

                                            Toast.makeText(this, "Rendelés sikeres!", Toast.LENGTH_SHORT).show();
                                        })
                                        .show();
                            });
                })
                .show();
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
