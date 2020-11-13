import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseConfiguration {

    private static DatabaseReference databaseRef;
    private static FirebaseAuth auth;

    public static DatabaseReference getFirebase()
    {
        if(databaseRef == null)
        {
            databaseRef = FirebaseDatabase.getInstance().getReference();
        }
        return databaseRef;
    }

    public static FirebaseAuth getFirebaseAuthentication()
    {
        if(auth == null)
        {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

}
