import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import wififingerprintlocator.DatabaseConfiguration;
import wififingerprintlocator.MainActivity;
import wififingerprintlocator.R;
import wififingerprintlocator.Users;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private Button btnLogin;
    private Button btnSignUp;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser fUser;
    private Users users;
    private String tipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.user);
        password = findViewById(R.id.pass);
        btnLogin = findViewById(R.id.log);
        btnSignUp = findViewById(R.id.signup);

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!username.getText().toString().equals("") || password.getText().toString().equals(""))
                {
                    users = new Users();
                    users.setEmail(username.getText().toString());
                    users.setPass(password.getText().toString());
                    validateLogin();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Please enter your info!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                //LoginActivity.this.finish();
            }
        });
    }

    private void validateLogin()
    {
        mAuth = DatabaseConfiguration.getFirebaseAuthentication();
        mAuth.signInWithEmailAndPassword(users.getEmail(), users.getPass()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    //fUser = FirebaseAuth.getInstance().getCurrentUser();
                    //String u = fUser.getUid();
                    //Toast.makeText(LoginActivity.this, "Success! " + u, Toast.LENGTH_SHORT).show();
                    String tipo = username.getText().toString();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("TYPE", tipo); //TESTE
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Wrong info!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
