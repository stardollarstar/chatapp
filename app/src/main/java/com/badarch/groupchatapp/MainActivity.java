package com.badarch.groupchatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userIco;

    public MainActivity() {
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            final ImageView img = findViewById(R.id.profile_pic);
            final StorageReference pathToPic = mStorageRef.child(currentUser.getUid() + "/userIcon");
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("UserIcon", Context.MODE_PRIVATE);
            userIco = directory.getAbsolutePath();
            try {
                File f = new File(directory,  currentUser.getUid() + ".jpg");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setImageBitmap(b);
            } catch (Exception e) {
                e.printStackTrace();
            }
            db.collection("users").document(currentUser.getUid()).get()
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
                                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                saveImageToInternal(bitmap);
                                                img.setImageBitmap(bitmap);
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
                                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                saveImageToInternal(bitmap);
                                                img.setImageBitmap(bitmap);
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }
                    });


        }

        chatBuilder();
    }

    public void SignOut(View v) {
        Intent intent = new Intent(this, SignIn.class);
        mAuth.signOut();
        startActivity(intent);
        finish();
    }
    public void chatBuilder() {


        final ArrayList<Object> names = new ArrayList<>(Collections.emptyList());
        final ArrayList<Object> ids = new ArrayList<>(Collections.emptyList());
        final ArrayList<Object> uIcons = new ArrayList<>(Collections.emptyList());
        final RecyclerView rV = findViewById(R.id.chat_container);
        final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
        final FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        final boolean[] secondRun = {false};


        if (currentuser != null) {
            mStore.collection("users/" + currentuser.getUid() + "/chat history").orderBy("sequence", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String name = doc.getString("name");
                                String oUID = doc.getString("oUID");
                                uIcons.add(oUID);
                                ids.add(doc.getId());
                                names.add(name);
                                CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, names, ids, uIcons);
                                rV.setAdapter(customAdapter);
                            }
                            secondRun[0] = true;
                        }
                    });
            mStore.collection("users/" + currentuser.getUid() + "/chat history")
                    .orderBy("sequence", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(secondRun[0]) {
                                int position = value.getDocuments().size();
                                if(position > 0) {
                                    DocumentSnapshot doc = value.getDocuments().get(position - 1);
                                    String name = doc.getString("name");
                                    String oUID = doc.getString("oUID");
                                    uIcons.add(oUID);
                                    ids.add(doc.getId());
                                    names.add(name);
                                    CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, names, ids, uIcons);
                                    rV.setAdapter(customAdapter);
                                }
                            }
                        }
                    });
        }
    }

    @SuppressLint("InflateParams")
    public void NewChat(View v) {
        final String cID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
        final String myName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        final String[] id = {null};
        final boolean[] firstTest = {false};
        final boolean[] secondTest = {false};
        final boolean[] thirdTest = {false};

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.new_chat, null));
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();


        View inf = getLayoutInflater().inflate(R.layout.new_chat, null);

        Button nc_cancel = (Button) inf.findViewById(R.id.nc_cancel);
        Button nc_add = (Button) inf.findViewById(R.id.nc_new);
        final EditText nc_search = inf.findViewById(R.id.nc_search);

        nc_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        nc_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String enteredEmail = nc_search.getText().toString();
                checkMail(enteredEmail, new Determine() {
                    @Override
                    public void onDataReceived(boolean res, String personId) {
                        if(!cID.equals(personId)) {
                            if (res) {
                                firstTest[0] = true;
                                id[0] = personId;
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "You can't send request to yourself", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                checkingSentRequest(enteredEmail, new DetermineTwo() {
                    @Override
                    public void onDataReceived(boolean res) {
                        if(res) {
                            secondTest[0] = true;
                        }
                        else {
                            Toast.makeText(MainActivity.this, "You have already sent request", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });

                checkingCurrentChattingPeople(enteredEmail, new DetermineThree() {
                    @Override
                    public void onDataReceived(boolean res) {
                        if(res) {
                            thirdTest[0] = true;
                            if(firstTest[0] && secondTest[0] && thirdTest[0]) {
                                long currentTime = Calendar.getInstance().getTimeInMillis();
                                String myEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                Map<String, Object> notification = new HashMap<>();
                                notification.put("sender", myName);
                                notification.put("sentTime", currentTime);
                                notification.put("senderID", cID);
                                notification.put("senderEmail", myEmail);
                                final Map<String, Object> sR = new HashMap<>();
                                sR.put("Email", enteredEmail);
                                sR.put("status", "waiting");
                                mStore.collection("users/" + id[0] + "/notifications")
                                        .add(notification).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        mStore.collection("users/" + cID + "/sent requests")
                                                .document(documentReference.getId())
                                                .set(sR);
                                    }
                                });
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "Sent request", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "You are already chatting with this person", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.setView(inf);
        dialog.show();

    }

    interface Determine {
        void onDataReceived(boolean res, String id);
    }
    interface DetermineTwo {
        void onDataReceived(boolean res);
    }
    interface DetermineThree {
        void onDataReceived(boolean res);
    }
    public void checkingCurrentChattingPeople(final String data, final DetermineThree listener) {
        FirebaseFirestore mStore = FirebaseFirestore.getInstance();
        String cUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mStore.collection("users/" + cUID + "/chat history").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean taskTwo = true;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            if (data.equals(doc.getString("email"))) {
                                taskTwo = false;
                            }
                        }
                        listener.onDataReceived(taskTwo);
                    }
                });
    }
    public void checkingSentRequest(final String data, final DetermineTwo listener) {
        FirebaseFirestore mStore = FirebaseFirestore.getInstance();
        String cUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mStore.collection("users/" + cUID + "/sent requests").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            boolean taskTwo = true;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if(data.equals(doc.getString("Email"))) {
                                    taskTwo = false;
                                }
                            }
                            listener.onDataReceived(taskTwo);
                        }
                    }
                });
    }
    public void checkMail(final String data, final Determine listener) {
        FirebaseFirestore mStore = FirebaseFirestore.getInstance();
        mStore.collection("users/").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            boolean taskTwo = false;
                            String id = null;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if(data.equals(doc.getString("Email"))) {
                                    taskTwo = true;
                                    id = doc.getId();
                                }
                            }
                            listener.onDataReceived(taskTwo, id);
                        }
                    }
                });
    }
    public void ShowNotifications(View V) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflater = getLayoutInflater().inflate(R.layout.notification_dialog, null);
        builder.setView(inflater);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();

        ImageButton dismiss = inflater.findViewById(R.id.dismiss_button);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setView(inflater);
        dialog.show();

        final ArrayList<Object> notifications = new ArrayList<>(Collections.emptyList());
        final ArrayList<Long> sentTime = new ArrayList<>(Collections.<Long>emptyList());
        final ArrayList<Object> senderIDs = new ArrayList<>(Collections.emptyList());
        final ArrayList<Object> senderEmails = new ArrayList<>(Collections.emptyList());
        final ArrayList<Object> notifIds = new ArrayList<>(Collections.emptyList());
        final RecyclerView notifrv = inflater.findViewById(R.id.notification_recycler);

        FirebaseFirestore mStore = FirebaseFirestore.getInstance();
        String cUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mStore.collection("users/" + cUID + "/notifications")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        String sender = doc.getString("sender");
                        long time = (long) doc.get("sentTime");
                        String senderID = doc.getString("senderID");
                        String senderEmail = doc.getString("senderEmail");
                        String notifId = doc.getId();
                        notifications.add(sender);
                        sentTime.add(time);
                        senderIDs.add(senderID);
                        senderEmails.add(senderEmail);
                        notifIds.add(notifId);
                        NotificationRV notificationRV = new NotificationRV(dialog.getContext(), notifications, sentTime, senderIDs, senderEmails, notifIds);
                        notifrv.setAdapter(notificationRV);
                    }
                }
            }
        });

        NotificationRV notificationRV = new NotificationRV(dialog.getContext(), notifications, sentTime, senderIDs, senderEmails, notifIds);
        notifrv.setAdapter(notificationRV);
    }

    public void saveImageToInternal(Bitmap img) {
        String currentuser = mAuth.getCurrentUser().getUid();
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("UserIcon", Context.MODE_PRIVATE);
        File myPath  = new File(directory, currentuser + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            img.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(fos != null) {
                    fos.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void GoToProfile(View v) {
        Intent intent = new Intent(this, ProfilePictureChanger.class);
        intent.putExtra("path", userIco);
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if(req == 1 && res == 1) {
            Bundle bundle = data.getExtras();
            assert bundle != null;
            String editedURI = bundle.getString("imageURI");
            ImageView img = findViewById(R.id.profile_pic);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageURI(Uri.parse(editedURI));
        }
    }
    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }
}