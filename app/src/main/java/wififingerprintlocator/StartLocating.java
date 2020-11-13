import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StartLocating extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private final int INTERVAL_FOR_SCAN = 50;

    private List<AccessPoint> APList = new ArrayList<>();
    private RecyclerView recyclerView;
    private APadapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_locating);

        recyclerView = findViewById(R.id.recyclerview);

        mAdapter = new APadapter(APList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult

        } else {
            startScanning();
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
        StartLocating.WifiReceiver receiverWifi = new StartLocating.WifiReceiver(wifi);
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
                }
                public void onFinish() {
                    getAverageRSSI(rssMap);
                }
            }.start();

            wifi.startScan();
        }
    }

    private void getAverageRSSI(Map<AccessPointConfirmation,ArrayList<Integer>> rssi)
    {
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
            APList.add(accessPoint);
            mAdapter.notifyDataSetChanged();
        }
    }

}


