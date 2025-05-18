package com.example.mobil_alkfejl_1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private ArrayList<CartItem> cartItems;

    public CartAdapter(Context context, ArrayList<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        Button deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemName);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        String display = item.getName() + " - " + item.getQuantity() + " kg - " + item.getPrice() + " Ft/kg";
        holder.itemText.setText(display);

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uid = auth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(uid)
                    .collection("cart")
                    .document(item.getName())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartItems.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, item.getName() + " törölve", Toast.LENGTH_SHORT).show();
                        Log.d("CartAdapter", "Törölve: " + item.getName());
                    });
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}