package com.example.mynutrition;

public class Upload {
    private String mImageUrl;
    private String mName;
    private String mDate;
    public Upload(){

    }
    public Upload(String imageUrl, String name, String date){
        mImageUrl = imageUrl;
        mName = name;
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
}
