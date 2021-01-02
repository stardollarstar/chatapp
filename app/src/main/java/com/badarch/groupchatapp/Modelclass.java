package com.badarch.groupchatapp;

public class Modelclass {
    String chat;
    String sender;
    String time;

    Modelclass() {}

    public Modelclass(String chat, String sender, String time) {
        this.chat = chat;
        this.sender = sender;
        this.time = time;
    }
    public String getChat() {
        return chat;
    }

    public String getSender() {
        return sender;
    }

    public String getTime() {
        return time;
    }
}
