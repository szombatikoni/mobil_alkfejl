package com.example.mobil_alkfejl_1;

public class CartItem {
    private String name;
    private int price;
    private int quantity;

    public CartItem() {}

    public CartItem(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
}