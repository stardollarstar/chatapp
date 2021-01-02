package com.badarch.groupchatapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class CustomAdapter extends RecyclerView.Adapter {
    ArrayList<Object> personNames;
    Context context;
    ArrayList<Object> ids;
    ArrayList<Object> user_ids;
    public CustomAdapter(Context context, ArrayList<Object> personNames, ArrayList<Object> iD, ArrayList<Object> oUID) {
        this.context = context;
        this.personNames = personNames;
        this.ids = iD;
        this.user_ids = oUID;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_preview, parent, false);
        return new MyViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        TextView name = holder.itemView.findViewById(R.id.chat_preview_name);
        final ImageView img = holder.itemView.findViewById(R.id.userIcon);
        final TextView chat = holder.itemView.findViewById(R.id.chat_preview);
        final String currentuserID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        name.setText(personNames.get(position).toString());
        final Bitmap[] bitmap = new Bitmap[personNames.size()];
        final StorageReference pathToPic = FirebaseStorage.getInstance().getReference().child(user_ids.get(position).toString() + "/userIcon");
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference("chats/" + ids.get(position).toString());

        mData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mData.child((snapshot.getChildrenCount() - 1) + "").addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Modelclass mclass = snapshot.getValue(Modelclass.class);
                        String previewTxt = mclass.getChat();
                        if(previewTxt.length() > 25) {
                            previewTxt = previewTxt.substring(0,25) + "...";
                        }
                        if(mclass.getSender().equals(currentuserID)) {
                            chat.setText("You: " + previewTxt + "  •  " + mclass.getTime());
                        }
                        else {
                            chat.setText(previewTxt + "  •  " + mclass.getTime());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        db.collection("users").document(user_ids.get(position) + "").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if(doc.getString("userIcon") == null) {
                                try {
                                    final File localFile = File.createTempFile("uIcon", "jpg");
                                    mStorageRef.child("default/defaultIcon.png").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Bitmap bitmap1 = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            bitmap[position] = bitmap1;
                                            img.setImageBitmap(bitmap1);
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try {
                                    final File localFile = File.createTempFile("uIcon", "jpg");
                                    pathToPic.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Bitmap bitmap1 = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            bitmap[position] = bitmap1;
                                            img.setImageBitmap(bitmap1);
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Openchat.class);
                intent.putExtra("chatid", ids.get(position).toString());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap[position].compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytearray = stream.toByteArray();
                intent.putExtra("picture", bytearray);
                intent.putExtra("name", personNames.get(position).toString());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return personNames.size();
    }
}