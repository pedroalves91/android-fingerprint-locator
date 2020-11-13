import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import wififingerprintlocator.R;
import wififingerprintlocator.Users;

public class RegisterActivity extends AppCompatActivity{

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private RadioButton rbUser;
    private RadioButton rbOwner;
    private Button btRegister;
    private Users users;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference dbRef;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.userName);
        etEmail = findViewById(R.id.userEmail);
        etPassword = findViewById(R.id.userPassword);
        rbUser = findViewById(R.id.radioUser);
        rbOwner = findViewById(R.id.radioOwner);
        btRegister = findViewById(R.id.btnReg);

        dbRef = FirebaseDatabase.getInstance().getReference("users");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //user sign in
                }
                else{
                    //user signed out
                }

            }
        };

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(rbUser.isChecked())
                {
                    userType = "user";
                }
                else
                {
                    userType = "owner";
                }
                users = new Users(etName.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString(), userType);
                dbRef.child(dbRef.push().getKey()).setValue(users);
                registerUser(etEmail.getText().toString(), etPassword.getText().toString());
                startActivity(new Intent(getApplication(), LoginActivity.class));
                RegisterActivity.this.finish();

            }
        });

    }

    public void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void onStop()
    {
        super.onStop();
        if(mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void registerUser(String email, String password)
    {
        if(!validateRegistration())
        {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    signOut();
                }
                else
                {
                    showMessage("User registration failed!");
                }
            }
        });
    }

    private void signOut()
    {
        mAuth.signOut();
    }

    private boolean validateRegistration()
    {
        boolean valid = true;

        String name = etName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            showMessage("Please insert your name!");
        }

        String email = etEmail.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            showMessage("Please insert your email!");
        }

        String password = etPassword.getText().toString().trim();
        if(TextUtils.isEmpty(password)){
            showMessage("Please insert your password!");
        }

        String type = userType.trim();
        if(TextUtils.isEmpty(type)){
            showMessage("Please choose your role!");
        }

        return valid;

    }

    private void showMessage(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

}
