import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class UserLocation extends AppCompatActivity {

    private PhotoView photoView;
    private Button end;
    private Button get;
    private Button loc;
    private Button myPos;
    private final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private final int INTERVAL_FOR_SCAN = 50;
    private int posX;
    private int posY;
    private String mapName;
    private String path;
    private int minD = Integer.MAX_VALUE;
    private static int v = 0;


    //Locate
    DatabaseReference databaseRSSIs;
    DatabaseReference databaseRSSIs2;
    DatabaseReference databaserefresh;

    List<AccessPoint> rssisList = new ArrayList<>();

    List<String> posicaoList = new ArrayList<>();

    List<String> dados = new ArrayList<>();

    String caminho = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        photoView = findViewById(R.id.imageView);
        end = findViewById(R.id.endLoct);
        get = findViewById(R.id.chooseMapL);
        loc = findViewById(R.id.getLoc);
        myPos = findViewById(R.id.getPos);

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

        myPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myPos.setVisibility(View.INVISIBLE);
                v = 1;
                DrawLineTransformation myTransformation = new DrawLineTransformation();
                Picasso.get().load(path).transform(myTransformation).fit().into(photoView);
                //Toast.makeText(UserLocation.this, "MIN "+minD+" X: "+posX+" Y: "+posY, Toast.LENGTH_SHORT).show();
            }
        });

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

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserLocation.this, MainActivity.class);
                startActivity(intent);
                UserLocation.this.finish();
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
        UserLocation.WifiReceiver receiverWifi = new UserLocation.WifiReceiver(wifi);
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

            loc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<ScanResult> wifiList = wifi.getScanResults();

                    if(v != 0)
                    {
                        Picasso.get().load(path).fit().into(photoView);
                    }

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
                    //myPos.setVisibility(View.VISIBLE);
                    new CountDownTimer(10000,1000)
                    {
                        public void onTick(long millisUntilFinished) {
                            //nada
                        }
                        public void onFinish() {
                            myPos.setVisibility(View.VISIBLE);
                        }
                    }.start();
                }
            });
            wifi.startScan();
        }
    }

    private void getAverageRSSI(Map<AccessPointConfirmation,ArrayList<Integer>> rssi)
    {
        dados.add("");
        Refresh();
        dados.clear();
        dados.add(mapName);

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

            dados.add(""+avgRSSI);
        }

        startLocating();
    }

    /**
     * Desenhar no mapa
     */
    class DrawLineTransformation implements Transformation {

        @Override
        public String key() {
            // TODO Auto-generated method stub
            return "drawline";
        }

        @Override
        public Bitmap transform(Bitmap bitmap) {
            // TODO Auto-generated method stub
            synchronized (DrawLineTransformation.class) {
                if(bitmap == null) {
                    return null;
                }
                Bitmap resultBitmap = bitmap.copy(bitmap.getConfig(), true);
                Canvas canvas = new Canvas(resultBitmap);
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStrokeWidth(10);
                canvas.drawPoint(posX,posY,paint);
                bitmap.recycle();
                return resultBitmap;
            }
        }
    }

    private void startLocating()
    {
        String pathbd = "fingerprint/";
        caminho = pathbd.concat(dados.get(0));
        databaseRSSIs = FirebaseDatabase.getInstance().getReference(caminho);

        databaseRSSIs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                posicaoList.clear();

                for (DataSnapshot rssiSnapshot : dataSnapshot.getChildren()) {
                    String posicao = rssiSnapshot.getKey();
                    posicaoList.add(posicao); //this adds an element to the list.
                }

                getSoma(posicaoList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Refresh() {
        rssisList.clear();

        posicaoList.clear();

        minD = Integer.MAX_VALUE;

        String caminho = "";
        String pathbd = "fingerprint/";
        caminho = pathbd.concat(dados.get(0));
        String caminhopos = caminho.concat("/0,0");
        databaserefresh = FirebaseDatabase.getInstance().getReference(caminhopos);

        int avgrrsi = 0;
        String ssid = "0";
        String bssi = "0";

        String id = databaserefresh.push().getKey();
        AccessPoint rssIs = new AccessPoint(bssi, ssid, avgrrsi);
        assert id != null;
        databaserefresh.child(id).setValue(rssIs);
    }

    private void getSoma(List<String> ListaPosicoes) {

        for (final String posicao : ListaPosicoes) {
            String caminhocb = caminho.concat("/");

            final String caminho2 = caminhocb.concat(posicao);

            databaseRSSIs2 = FirebaseDatabase.getInstance().getReference(caminho2);


            databaseRSSIs2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    rssisList.clear();

                    for (DataSnapshot rssiSnapshot : dataSnapshot.getChildren()) {
                        AccessPoint rssi = rssiSnapshot.getValue(AccessPoint.class);
                        rssisList.add(rssi);
                    }

                    int p = 1;
                    int dadosnovos;
                    int dist;
                    int auxdist = 0;

                    for(AccessPoint auxiliar : rssisList){

                        if(dados.size()> p){
                            dadosnovos = Integer.parseInt(dados.get(p));
                        }else{
                            dadosnovos = -109;
                        }

                        auxdist += Math.abs(dadosnovos - (auxiliar.getAvgRSSI()));
                        if(p==(rssisList.size())){
                            dist = (auxdist / p);
                            if(!posicao.matches("0.0"))
                            {
                                if(minD > dist)
                                {
                                    Toast.makeText(UserLocation.this, "Calculating your position! Wait please!!", Toast.LENGTH_SHORT).show();
                                    minD = dist;
                                    String[] cood = posicao.split(",");
                                    posX = Integer.parseInt(cood[0]);
                                    posY = Integer.parseInt(cood[1]);
                                }
                            }
                        }
                        p++;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
