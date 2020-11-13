import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import wififingerprintlocator.Login.LoginActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView mycard1, mycard2, mycard4;
    LinearLayout ll;
    private String userT;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ll = findViewById(R.id.ll);
        mycard1 = findViewById(R.id.n1cardId);
        mycard2 = findViewById(R.id.n2cardId);
        mycard4 = findViewById(R.id.n4cardId);

        mycard1.setOnClickListener(this);
        mycard2.setOnClickListener(this);
        mycard4.setOnClickListener(this);

        userT = getIntent().getStringExtra("TYPE");

        fUser=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users/"+userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                            for (DataSnapshot dS : dataSnapshot.getChildren())
                            {
                                Users u = new Users();
                                u.setType(dS.child(userid).getValue(Users.class).getType());

                                userT = u.getType();
                            }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {

        Intent i;

        switch(view.getId()){
            case R.id.n1cardId :
                i = new Intent(this, StartLocating.class);
                startActivity(i);
                //MainActivity.this.finish();
                break;
            case R.id.n2cardId :
                if(userT.matches("user"))
                {
                    i = new Intent(this, UserLocation.class);
                    startActivity(i);
                    MainActivity.this.finish();
                    break;
                }
                if(userT.matches("owner"))
                {
                    i = new Intent(this, UserPremium.class);
                    startActivity(i);
                    MainActivity.this.finish();
                    break;
                }
            case R.id.n4cardId :
                FirebaseAuth.getInstance().signOut();
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                MainActivity.this.finish();
                break;
            default:break;
        }

    }
}
