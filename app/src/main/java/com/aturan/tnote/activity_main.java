package com.aturan.tnote;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;

public class activity_main extends AppCompatActivity implements interfaceAdapter {


    private TextView selectedtxt;
    private ImageButton accountBtn, cancelBtn, selectAllBtn, silBtn, actionBtn;
    private RecyclerView recyclerView;
    private adapterNotlar adapter;
    private ArrayList<dataNotlar> dataNotlars;
    private FirebaseFirestore db;
    private FirebaseUser mUser;
    private dataNotlar dataNot;
    private CardView cardView;
    private EditText searchBox;
    private RecyclerViewState state;
    private Boolean animOk = false;
    private CryptUtil crypt;


    private void init() {

        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                getWindow().setStatusBarColor(getResources().getColor(R.color.main_bg_color));
                break;

        }

        db = FirebaseFirestore.getInstance();

        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build());

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        actionBtn = findViewById(R.id.main_acc_notEkleBtn);
        accountBtn = findViewById(R.id.topbar_main_accountBtn);

        cancelBtn = findViewById(R.id.topbar_main_cancelBtn);
        selectedtxt = findViewById(R.id.topbar_main_selectedTxt);
        selectAllBtn = findViewById(R.id.topbar_main_selectAllBtn);
        silBtn = findViewById(R.id.main_acc_silBtn);
        cardView = findViewById(R.id.main_acc_cardView);
        searchBox = findViewById(R.id.main_acc_searchBox);
        recyclerView = findViewById(R.id.main_acc_recyclerView);

        dataNotlars = new ArrayList<>();

        recyclerView.addItemDecoration(new SpacesItemDecoration((int) getResources().getDimension(R.dimen.recycler_padding)));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new adapterNotlar(activity_main.this, dataNotlars, this);

        state = new RecyclerViewState(recyclerView);
        crypt = new CryptUtil(this).initKey();

        boolean soruldumu = crypt.isFirstTimeView();
        if (!soruldumu) sifreDialog();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


        assert mUser.getEmail() != null;
        db.collection("Notlar")
                .document(mUser.getEmail())
                .collection("notlar")
                .addSnapshotListener((value, error) -> {

                    if (value != null) {

                        dataNotlars.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            dataNot = doc.toObject(dataNotlar.class);
                            if (dataNot != null)
                                dataNotlars.add(dataNot);
                        }

                        adapter = new adapterNotlar(activity_main.this, dataNotlars, this);
                        initAdapter(false);

                    }

                });

        accountBtn.setOnClickListener(view -> {

            String cikis = "Çıkış";
            String sifrebelirle = "Şifre Belirle";
            String hesapDegistir = "Hesap Değiştir";

            PopupMenu menu = new PopupMenu(this, accountBtn);
            menu.getMenu().add(sifrebelirle);
           // menu.getMenu().add(hesapDegistir);
            menu.getMenu().add(cikis);
            menu.setOnMenuItemClickListener(item -> {

                if (item.getTitle() == cikis) {

                    FirebaseAuth.getInstance().signOut();

                    Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(activity_main.this, activity_firstLogin.class)
                            .setFlags(FLAG_ACTIVITY_CLEAR_TOP));
                    crypt.setIsFirstTimeView(false);
                    finish();
                } else if (item.getTitle() == sifrebelirle) {
                    sifreDialog();
                } else if (item.getTitle() == hesapDegistir){


                }

                return true;
            });
            menu.show();


        });


        actionBtn.setOnClickListener(view -> startActivity(new Intent(activity_main.this, activity_addNote.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        cancelBtn.setOnClickListener(view -> setGone());

        selectAllBtn.setOnClickListener(view -> {

            if (adapter.getSelected().size() == dataNotlars.size())
                for (int i = 0; i < dataNotlars.size(); i++)
                    dataNotlars.get(i).setChecked(false);
            else
                for (int i = 0; i < dataNotlars.size(); i++)
                    dataNotlars.get(i).setChecked(true);

            setVisible();
            initAdapter(true);

        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filtrele(searchBox.getText().toString().toLowerCase());
            }
        });

        silBtn.setOnClickListener(view -> {

            if (adapter.getSelected().size() > 0) {

                for (int i = 0; i < adapter.getSelected().size(); i++) {

                    db.collection("Notlar")
                            .document(mUser.getEmail())
                            .collection("notlar").document(adapter.getSelected().get(i).getNot_id()).delete();
                }
                Toast.makeText(this, "Silindi", Toast.LENGTH_SHORT).show();
                setGone();

            }

        });


    }

    private Dialog d;
    private EditText sifreTxt;

    @SuppressLint("SetTextI18n")
    private void sifreDialog() {
        d = new Dialog(this);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setContentView(R.layout.dialog_sifre_belirle);
        ImageButton btnTamam = d.findViewById(R.id.dialog_btnTamam);
        sifreTxt = d.findViewById(R.id.dialog_sifreTxt);
        TextView infoTxt = d.findViewById(R.id.dialog_infoTxt);

        infoTxt.setText("Mevcut şifreleme anahtarı: " + crypt.getKey() + "\n\n" +
                "Notlarınız bu şifreleme anahtarı ile şifrelenir ve şifresi çözülür. " +
                "Farklı notlar için farklı şifreleme anahtarı ayarlayabilir ve bu şifreleme anahtarını kullanarak " +
                "ilgili notları okuyabilirsiniz. Burada belirleyecek olduğunuz şifreleme anahtarı sadece telefonunuzda saklanır. " +
                "Şifreleme anahtarını unutmanız halinde şifreli notları kurtaramazsınız.");

        sifreTxt.setText(crypt.getKey());
        btnTamam.setOnClickListener(view -> {

            String sifre = sifreTxt.getText().toString();
            if (TextUtils.isEmpty(sifre))
                sifre = "def";

            if (d.isShowing()) {

                crypt.setKey(sifre);
                initAdapter(false);
                d.dismiss();
            }

        });

        d.show();
    }

    @Override
    public void longClick(com.aturan.tnote.dataNotlar dataNotlar) {

        if (!animOk)
            gorunurAnim();
        setVisible();
        initAdapter(true);
    }

    @Override
    public void click(com.aturan.tnote.dataNotlar dataNotlar) {

        setVisible();
        if (!adapter.getSelectMode())
            startActivity(new Intent(this, activity_viewNote.class)
                    .setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra("not_id", dataNotlar.getNot_id()));

    }

    @Override
    public void onBackPressed() {

        if (adapter.getSelectMode()) {

            for (int i = 0; i < dataNotlars.size(); i++) {

                dataNotlars.get(i).setChecked(false);
            }
            setGone();
            initAdapter(false);

        } else
            super.onBackPressed();
    }

    private void gorunurAnim() {
        animOk = true;
        cancelBtn.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunur));
        selectedtxt.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunur));
        accountBtn.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunmez));
        selectAllBtn.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunur));
        cardView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunur));
    }

    private void gorunmezAnim() {
        animOk = false;
        cancelBtn.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunmez));
        selectedtxt.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunmez));
        accountBtn.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunur));
        selectAllBtn.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunmez));
        cardView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.item_gorunmez));
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForColorStateLists"})
    private void setVisible() {

        if (adapter.getSelectMode()) {

            int size = adapter.getSelected().size();

            if (size > 0) {


                cancelBtn.setVisibility(View.VISIBLE);
                selectedtxt.setVisibility(View.VISIBLE);
                selectedtxt.setText(size + " öğe seçildi ");
                accountBtn.setVisibility(View.INVISIBLE);
                selectAllBtn.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);

                silBtn.setBackgroundTintList(getResources().getColorStateList(R.color.sil_btn_icon_color));


            } else {

                silBtn.setBackgroundTintList(getResources().getColorStateList(R.color.btns_color));
                selectedtxt.setText("Öğeleri seç");
            }

            if (adapter.getSelected().size() == dataNotlars.size())
                selectAllBtn.setBackgroundTintList(getResources().getColorStateList(R.color.all_selected_icon_color));
            else
                selectAllBtn.setBackgroundTintList(getResources().getColorStateList(R.color.btns_color));

        }

    }

    private void setGone() {

        gorunmezAnim();

        for (int i = 0; i < dataNotlars.size(); i++) {
            dataNotlars.get(i).setChecked(false);
        }
        cancelBtn.setVisibility(View.GONE);
        selectedtxt.setVisibility(View.GONE);
        selectAllBtn.setVisibility(View.GONE);
        accountBtn.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.GONE);
        initAdapter(false);
    }


    private void initAdapter(Boolean selectMode) {

        state.save();
        adapter.setSelectMode(selectMode);
        recyclerView.setAdapter(adapter);
        state.restore();
    }

    @SuppressLint("SetTextI18n")
    private void filtrele(String ara) {
        ArrayList<dataNotlar> temp = new ArrayList<>();
        for (dataNotlar list : dataNotlars)
            if (crypt.decrypt(list.getBaslik()).toLowerCase().contains(ara) || crypt.decrypt(list.getNot()).toLowerCase().contains(ara))
                temp.add(list);

        adapter.setDataNotlars(temp);
    }

    @Override
    protected void onPause() {
        super.onPause();
        state.save();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initAdapter(false);

    }

}