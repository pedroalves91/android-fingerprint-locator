import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Users {

    private String pass;
    private String uid;
    private String name;
    private String email;
    private String type;

    public Users(String name, String email, String pass, String type)
    {
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.type = type;
    }

    public Users() {
    }


    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String id) {
        this.uid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "User{" +
                "pass='" + pass + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", type='" + type + '\'' +'}';
    }

}
