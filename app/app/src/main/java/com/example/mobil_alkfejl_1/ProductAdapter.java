
package com.example.mobil_alkfejl_1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private ArrayList<Product> products;

    public ProductAdapter(ArrayList<Product> products) {
        this.products = products;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, price;
        Button addButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            addButton = itemView.findViewById(R.id.addToCartButton);
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = products.get(position);
        holder.name.setText(p.getName());
        holder.price.setText(p.getPrice() + " Ft");

        holder.addButton.setOnClickListener(v -> {
            Context context = v.getContext();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String uid = auth.getCurrentUser().getUid();
            String docId = p.getName();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "order_channel", "Rendelések", NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }

            db.collection("users")
                    .document(uid)
                    .collection("cart")
                    .document(docId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long qtyLong = documentSnapshot.getLong("quantity");
                            int currentQty = (qtyLong != null) ? qtyLong.intValue() : 1;
                            int newQty = currentQty + 1;

                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(docId)
                                    .update("quantity", newQty)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Mennyiség növelve: " + newQty + " db", Toast.LENGTH_SHORT).show();

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "order_channel")
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setContentTitle("Kosár frissítve")
                                                .setContentText(p.getName() + " darabszáma: " + newQty)
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

                                            NotificationManagerCompat.from(context).notify(p.getName().hashCode(), builder.build());
                                        }
                                    });

                        } else {
                            HashMap<String, Object> cartItem = new HashMap<>();
                            cartItem.put("name", p.getName());
                            cartItem.put("price", p.getPrice());
                            cartItem.put("quantity", 1);

                            db.collection("users")
                                    .document(uid)
                                    .collection("cart")
                                    .document(docId)
                                    .set(cartItem)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, p.getName() + " hozzáadva a kosárhoz", Toast.LENGTH_SHORT).show();

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "order_channel")
                                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                .setContentTitle("Kosár frissítve")
                                                .setContentText(p.getName() + " bekerült a kosárba.")
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                        NotificationManagerCompat.from(context).notify(p.getName().hashCode(), builder.build());
                                    });
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
