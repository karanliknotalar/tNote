package com.aturan.tnote;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class activity_firstLogin extends ComponentActivity {

    private final ActivityResultLauncher<Intent> activityResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {

                    try {

                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                        firebaseAccountWith(account);

                    } catch (Exception e) {

                        System.out.println(e.getMessage());
                    }

                } else
                    mesajGoster("İptal edildi");

            });

    private void firebaseAccountWith(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {

                    assert authResult.getAdditionalUserInfo() != null;
                    if (authResult.getAdditionalUserInfo().isNewUser()) {

                        mesajGoster("Yeni hesap oluşturuldu.\nGiriş yapıldı.");

                    } else
                        mesajGoster("Giriş yapıldı");

                    loginOkGoMainActivity();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Giriş yapılamadı\n" + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private GoogleSignInClient signInClient;
    private FirebaseAuth mAuth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        SignInButton girisYapBtn = findViewById(R.id.first_acc_gSingBtn);

        //Firebase aut init
        mAuth = FirebaseAuth.getInstance();
        loginOkGoMainActivity();

        //Google ile Giriş ayarları.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        signInClient.signOut();

        TextView textView = (TextView) girisYapBtn.getChildAt(0);
        textView.setText("Giriş Yap");

        girisYapBtn.setOnClickListener(view -> {

            Intent intent = signInClient.getSignInIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activityResultLaunch.launch(intent);

        });

    }

    private void loginOkGoMainActivity() {

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(activity_firstLogin.this, activity_main.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }

    private void mesajGoster(String mesaj) {
        Toast.makeText(this, mesaj, Toast.LENGTH_SHORT).show();
    }
}