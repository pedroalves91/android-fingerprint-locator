import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ShowMap extends AppCompatActivity {

    private PhotoView photoView;
    private Button end;
    private Button get;
    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private final int INTERVAL_FOR_SCAN = 50;
    private int posX;
    private int posY;
    private String mapName;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmap);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        photoView = findViewById(R.id.imageview);
        end = findViewById(R.id.endLoc);
        get = findViewById(R.id.chooseMap);

        //QRCODE
        final Activity activity = this;

        //SCAN WIFI
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult

        } else {
            startScanning();
        }

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QRCODE
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("Scan The QRCode");
                integrator.setCameraId(0);
                integrator.initiateScan();
            }
        });

        //TESTE
        //Picasso.get().load(path).fit().into(photoView);

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowMap.this, MainActivity.class);
                startActivity(intent);
                ShowMap.this.finish();
            }
        });
    }

    /**
     * QR Code Scan
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {
            if(result.getContents() != null)
            {
                path = result.getContents();
                int i = 81;
                while (path.charAt(i) != '.')
                {
                    i++;
                }
                mapName = path.substring(81,i);
                Picasso.get().load(path).fit().into(photoView);
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            startScanning();

        }
    }

    private void startScanning() {
        final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
        ShowMap.WifiReceiver receiverWifi = new ShowMap.WifiReceiver(wifi);
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                wifi.startScan();
            }
        }, 0,INTERVAL_FOR_SCAN);
    }

    class WifiReceiver extends BroadcastReceiver {
        private WifiManager wifi;
        Map<AccessPointConfirmation,ArrayList<Integer>> rssMap = new HashMap<>();


        public WifiReceiver(WifiManager wifi) {
            this.wifi = wifi;
        }

        public void onReceive(Context c, Intent intent) {


            photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(ImageView view, float x, float y) {

                    posX = Math.round(x * view.getWidth()); //x * view.getWidth();
                    posY = Math.round(y * view.getHeight());

                    List<ScanResult> wifiList = wifi.getScanResults();

                    for (ScanResult result : wifiList) {

                        String ssid = result.SSID;
                        String bssid = result.BSSID;
                        Integer rssi = result.level;

                        AccessPointConfirmation ap = new AccessPointConfirmation(bssid, ssid);
                        if (!rssMap.containsKey(ap)) {
                            rssMap.put(ap, new ArrayList<Integer>());
                        }
                        rssMap.get(ap).add(rssi);
                    }

                    new CountDownTimer(5000,1000)
                    {
                        public void onTick(long millisUntilFinished) {
                            //nada
                            //getAverageRSSI(rssMap,posX,posY);
                        }
                        public void onFinish() {
                            getAverageRSSI(rssMap, posX, posY);
                        }
                    }.start();
                }
            });
            wifi.startScan();
        }
    }

    private void getAverageRSSI(Map<AccessPointConfirmation,ArrayList<Integer>> rssi, int x, int y)
    {

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("/fingerprint/" + mapName + "/" + x + "," + y);

        for (Map.Entry<AccessPointConfirmation,ArrayList<Integer>> entry : rssi.entrySet()) {
            int i = 0;
            int RSSI = 0;
            int avgRSSI;
            while(i < entry.getValue().size())
            {
                RSSI += entry.getValue().get(i);
                i++;
            }
            if(i == 0)
            {
                avgRSSI = 0;
            }
            else
            {
                avgRSSI = Math.round((float)RSSI/i);
            }
            AccessPoint accessPoint = new AccessPoint(entry.getKey().getBSSID(), entry.getKey().getSSID(), avgRSSI);
            dbRef.push().setValue(accessPoint);
        }
        Toast.makeText(this,"Reference Point Added to Map: " + mapName + "!!!", Toast.LENGTH_SHORT).show();
    }

}
