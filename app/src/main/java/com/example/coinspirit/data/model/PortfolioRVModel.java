package com.example.coinspirit.data.model;

public class PortfolioRVModel {
    private String name;
    private String symbol;
    private String price;
    private String quantity;
    private String purchasePrice;

    public PortfolioRVModel() {
        this.name = "";
        this.symbol = "";
        this.price = "";
        this.quantity = "";
        this.purchasePrice = "";
    }

    public PortfolioRVModel(String name, String symbol, String price, String quantity, String purchasePrice) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
}
