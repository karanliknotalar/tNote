package com.aturan.tnote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class activity_viewNote extends AppCompatActivity {

    private ImageView backBtn, menuBtn;
    private ImageButton kaydetBtn, silBtn;
    private EditText txtBaslik, txtNot;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private DocumentReference df;
    private String not_ID;
    private String baslikStr;
    private String notStr;
    private final String sifreliVeri = "Şifreli";
    private CryptUtil crypt;

    private void init() {

        backBtn = findViewById(R.id.topbar_viewNote_backBtn);
        txtBaslik = findViewById(R.id.viewNote_txtBaslik);
        txtNot = findViewById(R.id.viewNote_txtNote);
        menuBtn = findViewById(R.id.topbar_viewNote_menuBtn);
        kaydetBtn = findViewById(R.id.view_acc_kaydetBtn);
        silBtn = findViewById(R.id.view_acc_silBtn);
        CardView cardView = findViewById(R.id.view_acc_cardView);

        cardView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunur));

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        not_ID = getIntent().getStringExtra("not_id");

        crypt = new CryptUtil(this).initKey();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        init();

        if (!TextUtils.isEmpty(not_ID)) {

            assert mUser.getEmail() != null;
            df = db.collection("Notlar")
                    .document(mUser.getEmail())
                    .collection("notlar")
                    .document(not_ID);

            df.get().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    assert task.getResult() != null;
                    baslikStr = crypt.decrypt(task.getResult().get("baslik").toString());
                    notStr = crypt.decrypt(task.getResult().get("not").toString());

                    txtBaslik.setText(Objects.equals(baslikStr, crypt.ERROR) ? sifreliVeri : baslikStr);
                    txtNot.setText(Objects.equals(notStr, crypt.ERROR) ? sifreliVeri : notStr);
                    txtBaslik.setTextColor(Objects.equals(baslikStr, crypt.ERROR) ?
                            getResources().getColor(R.color.sifreli_veri_item_color) :
                            getResources().getColor(R.color.item_text_color));
                    txtNot.setTextColor(Objects.equals(notStr, crypt.ERROR) ?
                            getResources().getColor(R.color.sifreli_veri_item_color) :
                            getResources().getColor(R.color.item_text_color));
                }

            });
        }

        backBtn.setOnClickListener(view -> {

            guncelle();

        });

        menuBtn.setOnClickListener(view -> {

            PopupMenu menu = new PopupMenu(this, menuBtn);
            menu.getMenuInflater().inflate(R.menu.view_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()) {
                    case R.id.view_menu_kaydet:
                        guncelle();
                        break;
                    case R.id.view_menu_sil:
                        sil();
                        break;
                }
                return true;
            });
            menu.show();

        });

        kaydetBtn.setOnClickListener(view -> guncelle());
        silBtn.setOnClickListener(view -> sil());

    }

    private void guncelle() {

        HashMap<String, Object> mNot = new HashMap<>();
        String baslik = txtBaslik.getText().toString();
        String not = txtNot.getText().toString();

        if (!TextUtils.isEmpty(not) && !(Objects.equals(notStr, crypt.ERROR))) {

            if (!TextUtils.isEmpty(baslik))
                mNot.put("baslik", crypt.encrypt(baslik));
            else
                mNot.put("baslik", crypt.encrypt(""));

            mNot.put("not_id", not_ID);
            mNot.put("not", crypt.encrypt(not));
            mNot.put("zaman", FieldValue.serverTimestamp());
            df.update(mNot);
        }
        returnMainActivity();
    }

    private void sil() {
        df.delete();
        Toast.makeText(this, "Silindi", Toast.LENGTH_SHORT).show();
        returnMainActivity();
    }

    private void returnMainActivity() {
        startActivity(new Intent(activity_viewNote.this, activity_main.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    @Override
    public void onBackPressed() {
        guncelle();
        super.onBackPressed();
    }

}