package com.badarch.groupchatapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatShower extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Object> chats;
    ArrayList<Object> senderid;
    ArrayList<Object> time;
    byte[] byteArray;
    Context context;
    public ChatShower (Context context, ArrayList<Object> chats, ArrayList<Object> senderid, ArrayList<Object> time, byte[] byteArray) {
        this.chats = chats;
        this.context = context;
        this.byteArray = byteArray;
        this.senderid = senderid;
    }
    @Override
    public int getItemViewType(int position) {
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        int trigger = 1;
        if (currentUser.equals(senderid.get(position).toString())) {
            trigger = 0;
        }
        if(senderid.get(position).toString().equals("console")) {
            trigger = 2;
        }
        return trigger;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View opp = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_bubble, parent, false);
        View mine = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_bubble_two, parent, false);
        View console = LayoutInflater.from(parent.getContext()).inflate(R.layout.console_chat, parent, false);
        switch (viewType) {
            case 0: return new ViewHolder1(mine);
            case 2: return new ViewHolder3(console);
            default: return new ViewHolder2(opp);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView chat = holder.itemView.findViewById(R.id.chatBubble);
            chat.setText(chats.get(position).toString());


            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.length);
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(senderid.get(position).toString())) {
                ImageView user_icon = holder.itemView.findViewById(R.id.user_icon);
                user_icon.setImageBitmap(bmp);
            }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ViewHolder2 extends RecyclerView.ViewHolder {
        public ViewHolder2(View itemView){ super(itemView); }
    }
    public static class ViewHolder1 extends RecyclerView.ViewHolder {
        public ViewHolder1(View itemView){
            super(itemView);
        }
    }
    public static class ViewHolder3 extends RecyclerView.ViewHolder {
        public ViewHolder3(View itemView){
            super(itemView);
        }
    }
}
