import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class APadapter extends RecyclerView.Adapter<APadapter.MyViewHolder> {

    private List<AccessPoint> APList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView ssid, bssid, rssi;

        public MyViewHolder(View view) {
            super(view);
            ssid = view.findViewById(R.id.title);
            bssid = view.findViewById(R.id.genre);
            rssi = view.findViewById(R.id.year);
        }
    }
    public APadapter(List<AccessPoint> APList) {
        this.APList = APList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.accesspoint_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AccessPoint ap = APList.get(position);
        holder.ssid.setText(ap.getSSID());
        holder.bssid.setText(ap.getBSSID());
        holder.rssi.setText(ap.getAvgRSSI()+"");
    }

    @Override
    public int getItemCount() {
        return APList.size();
    }
}
