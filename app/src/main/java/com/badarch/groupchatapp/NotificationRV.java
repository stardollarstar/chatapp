package com.badarch.groupchatapp;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NotificationRV extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Object> notifSender;
    ArrayList<Long> timeOver;
    ArrayList<Object> senderIDs;
    ArrayList<Object> senderEmails;
    ArrayList<Object> notifIds;

    public NotificationRV(android.content.Context context, ArrayList<Object> notifications, ArrayList<Long> sentTime, ArrayList<Object> senderIds, ArrayList<Object> senderEmails, ArrayList<Object> notifIds) {
        this.context = context;
        this.notifSender = notifications;
        this.timeOver = sentTime;
        this.senderIDs = senderIds;
        this.senderEmails = senderEmails;
        this.notifIds = notifIds;
    }

    public int getItemViewType(int position) {
        if(notifSender.size() == 0) {
            Log.d("Data tester", "Yes it is empty");
            return 1;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inf = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification, parent, false);
        View inf2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_notif, parent, false);
        if (viewType == 1) {
            return new CustomHolder1(inf2);
        }
        return new CustomHolder(inf);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        TextView request = holder.itemView.findViewById(R.id.sender);
        TextView timeTxt = holder.itemView.findViewById(R.id.overTime);
        Calendar currentTime = Calendar.getInstance();

        long millis = currentTime.getTimeInMillis();
        long millis2 = timeOver.get(position);
        long conv = 1000 * 60 * 60 * 24;
        long dayDiff = Math.abs(millis - millis2) / conv;
        long l = Math.abs(millis - millis2) % conv;
        long hourDiff = l / (1000 * 60 * 60);
        long minDiff = (l % (1000 * 60 * 60)) / (1000 * 60);

        request.setText(notifSender.get(position) + " sent you chat request.");
        if (dayDiff == 0) {
            if (hourDiff == 0) {
                if(minDiff == 0) {
                    timeTxt.setText("Just now");
                }
                else {
                    timeTxt.setText(minDiff + "m ago");
                }
            }
            else {
                timeTxt.setText(hourDiff + "h " + minDiff + "m ago");
            }
        }
        else {
            timeTxt.setText(dayDiff + "d " + hourDiff + "h " + minDiff + "m ago");
        }

        final Button accept = holder.itemView.findViewById(R.id.accept_request);
        final Button decline = holder.itemView.findViewById(R.id.decline_request);
        final TextView txtResult = holder.itemView.findViewById(R.id.textOfResult);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("chats");
                final String key = myRef.push().getKey();

                String currentTimeTwo = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                Map<String, Object> firstChat = new HashMap<>();
                firstChat.put("chat", "Start chatting with your friend");
                firstChat.put("sender", "console");
                firstChat.put("time", currentTimeTwo);
                accept.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                txtResult.setText("Request accepted");
                txtResult.setVisibility(View.VISIBLE);

                Map<String, Object> fullData = new HashMap<>();
                fullData.put("email", senderEmails.get(position));
                fullData.put("name", notifSender.get(position));
                fullData.put("oUID", senderIDs.get(position));
                fullData.put("sequence", 2);

                final Map<String, Object> fullDataTwo = new HashMap<>();
                fullDataTwo.put("email", user.getEmail());
                fullDataTwo.put("name", user.getDisplayName());
                fullDataTwo.put("oUID", user.getUid());
                myRef.child(key + "/0").setValue(firstChat);

                mStore.collection("users/" + user.getUid() + "/chat history")
                        .document(key)
                        .set(fullData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mStore.collection("users/" + senderIDs.get(position) + "/chat history")
                                .document(key).set(fullDataTwo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                    mStore.collection("users/" + user.getUid() + "/notifications")
                                            .document(notifIds.get(position).toString())
                                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Request accepted", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                mStore.collection("users/" + senderIDs.get(position) + "/sent requests")
                                        .document(notifIds.get(position).toString())
                                        .delete();
                            }
                        });
                    }
                });
            }
        });
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore mStore = FirebaseFirestore.getInstance();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                accept.setVisibility(View.GONE);
                decline.setVisibility(View.GONE);
                txtResult.setText("Request declined");
                txtResult.setVisibility(View.VISIBLE);
                mStore.collection("users/" + user.getUid() + "/notifications")
                        .document(notifIds.get(position).toString())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Request declined", Toast.LENGTH_SHORT).show();
                    }
                });
                mStore.collection("users/" + senderIDs.get(position) + "/sent requests")
                        .document(notifIds.get(position).toString())
                        .delete();
            }
        });
    }

    @Override
    public int getItemCount() {
        return  notifSender.size();
    }

    public static class CustomHolder extends RecyclerView.ViewHolder {
        public CustomHolder(View itemView) { super(itemView);}
    }
    public static class CustomHolder1 extends RecyclerView.ViewHolder {
        public CustomHolder1(View itemView) { super(itemView);}
    }
}
