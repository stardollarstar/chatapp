package com.badarch.groupchatapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class Openchat extends AppCompatActivity {
    private String chat_id;
    private byte[] byteArray;
    private static int LengthOfChat;
    final ArrayList<Object> senderID = new ArrayList<>(Collections.emptyList());
    final ArrayList<Object> chats = new ArrayList<>(Collections.emptyList());
    final ArrayList<Object> time = new ArrayList<>(Collections.emptyList());
    final ArrayList<Object> chatCount = new ArrayList<>(Collections.emptyList());

    @Override
    protected void onCreate(Bundle savedI) {
        super.onCreate(savedI);
        setContentView(R.layout.chat);
        Bundle infos = getIntent().getExtras();
        assert infos != null;
        chat_id = infos.getString("chatid");
        String title = infos.getString("name");
        byteArray = getIntent().getByteArrayExtra("picture");
        final ImageView img = findViewById(R.id.profPic);
        if (byteArray != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            img.setImageBitmap(bmp);
        }
        else {
            img.setImageResource(R.drawable.ic_account);
        }
        TextView name = findViewById(R.id.name_text);
        final RecyclerView rv = findViewById(R.id.chat_showing_recycler);
        name.setText(title);
        final DatabaseReference db = FirebaseDatabase.getInstance().getReference("chats/" + chat_id);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LengthOfChat = (int) snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ImageButton send_button = findViewById(R.id.sendButton);
        final EditText text_of_input = findViewById(R.id.sendingText);

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = text_of_input.getText().toString();
                if(!text.equals("")){
                    DatabaseReference GeneralChatHolder = db.child(LengthOfChat + "/");
                    String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
                    ChatObject chatObject = new ChatObject(text, currentUserID, currentTime);
                    GeneralChatHolder.setValue(chatObject);
                    text_of_input.setText("");
                    scrollToBottom(rv);
                }
            }
        });


        BuildChat();
    }


    public void BuildChat() {
        final RecyclerView rv = findViewById(R.id.chat_showing_recycler);
        final boolean[] secondRun = {false};

        rv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(oldBottom > bottom) {
                    scrollToBottom(rv);
                }
            }
        });

        DatabaseReference mDb = FirebaseDatabase.getInstance().getReference("chats/" + chat_id);
        mDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (secondRun[0]) {
                    Modelclass data = snapshot.getValue(Modelclass.class);
                    String chat = data.getChat();
                    String sender = data.getSender();
                    LengthOfChat++;
                    chats.add(chat);
                    senderID.add(sender);
                    ChatShower chatShower = new ChatShower(Openchat.this, chats, senderID, time, byteArray);
                    rv.setAdapter(chatShower);
                    scrollToBottom(rv);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!secondRun[0]) {
                    for (DataSnapshot doc : snapshot.getChildren()) {
                        Modelclass data = doc.getValue(Modelclass.class);
                        chats.add(data.getChat());
                        senderID.add(data.getSender());
                        time.add(data.getTime());
                        chatCount.add(LengthOfChat);
                        ChatShower chatShower = new ChatShower(Openchat.this, chats, senderID, time, byteArray);
                        rv.setAdapter(chatShower);
                    }
                    scrollToBottom(rv);
                    secondRun[0] = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void scrollToBottom(final RecyclerView recyclerView) {
        final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        final RecyclerView.Adapter adapter = recyclerView.getAdapter();
        final int lastItemPosition = adapter.getItemCount() - 1;

        layoutManager.scrollToPositionWithOffset(lastItemPosition, 0);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                View target = layoutManager.findViewByPosition(lastItemPosition);
                if (target != null) {
                    int offset = recyclerView.getMeasuredHeight() - target.getMeasuredHeight();
                    layoutManager.scrollToPositionWithOffset(lastItemPosition, offset);
                }
            }
        });
    }
}
