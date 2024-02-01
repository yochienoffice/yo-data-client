package com.hotsaucepos.onlineorder.dataclient.model;

import java.math.BigDecimal;

public class Data {
    long storeId;
    int numOfOrder;
    BigDecimal convenienceFeeTotal;
    BigDecimal orderTotal;
    String storeName;

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public int getNumOfOrder() {
        return numOfOrder;
    }

    public void setNumOfOrder(int numOfOrder) {
        this.numOfOrder = numOfOrder;
    }

    public BigDecimal getConvenienceFeeTotal() {
        return convenienceFeeTotal;
    }

    public void setConvenienceFeeTotal(BigDecimal convenienceFeeTotal) {
        this.convenienceFeeTotal = convenienceFeeTotal;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
