package com.aturan.tnote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

public class activity_addNote extends AppCompatActivity {

    private ImageButton addNoteBtn;
    private EditText txtBaslik, txtNote;
    private FirebaseFirestore db;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        ImageButton backBtn = findViewById(R.id.topbar_addnote_backBtn);
        addNoteBtn = findViewById(R.id.topbar_addnote_addNoteBtn);

        txtBaslik = findViewById(R.id.addnote_txtBaslik);
        txtNote = findViewById(R.id.addnote_txtNote);

        db = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        backBtn.setOnClickListener(view -> {

            notEkle();
            startActivity(new Intent(activity_addNote.this, activity_main.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();

        });


        addNoteBtn.setOnClickListener(view -> notEkle());

        txtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (txtNote.getText().toString().length() > 0) {
                    addNoteBtn.setVisibility(View.VISIBLE);
                    addNoteBtn.setAnimation(AnimationUtils.loadAnimation(activity_addNote.this, R.anim.item_gorunur));
                } else {
                    addNoteBtn.setAnimation(AnimationUtils.loadAnimation(activity_addNote.this, R.anim.item_gorunmez));
                    addNoteBtn.setVisibility(View.GONE);
                }

            }
        });

    }


    @Override
    public void onBackPressed() {
        notEkle();
        super.onBackPressed();

    }

    private void notEkle() {

        if (!TextUtils.isEmpty(txtNote.getText())) {

            if (TextUtils.isEmpty(txtBaslik.getText().toString()))
                notEkleSetData("", txtNote.getText().toString());
            else
                notEkleSetData(txtBaslik.getText().toString(), txtNote.getText().toString());
        }
    }

    private void notEkleSetData(String baslik, String icerik) {

        CryptUtil cryp = new CryptUtil(this).initKey();

        String not_id = UUID.randomUUID().toString();
        HashMap<String, Object> mNot = new HashMap<>();
        mNot.put("not_id", not_id);
        mNot.put("baslik", cryp.encrypt(baslik));
        mNot.put("not", cryp.encrypt(icerik));
        mNot.put("zaman", FieldValue.serverTimestamp());

        assert mUser.getEmail() != null;
        db.collection("Notlar")
                .document(mUser.getEmail())
                .collection("notlar")
                .document(not_id)
                .set(mNot)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                })
                .addOnFailureListener(e -> {
                    System.out.println(e.getMessage());
                });

    }

}