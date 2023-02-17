package com.example.nocnha.modelClass;

public class DataSend {
    public Data data;
    public String to;

    public DataSend(Data data, String receiver) {
        this.data = data;
        this.to = receiver;
    }
}
