public class AccessPoint {

    private String BSSID;
    private String SSID;
    private Integer avgRSSI;

    public AccessPoint(){}

    public AccessPoint(String BSSID, String SSID, Integer avgRSSI)
    {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.avgRSSI = avgRSSI;
    }

    @Override
    public boolean equals(Object obj) {
        AccessPoint ap = (AccessPoint) obj;
        if (ap.BSSID.equals(BSSID) && ap.SSID.equals(SSID) && ap.avgRSSI.equals(avgRSSI)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ( 37 * 4 + BSSID.hashCode() ) +
                ( 37 * 4 + SSID.hashCode() );
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public Integer getAvgRSSI() {
        return avgRSSI;
    }

    public void setAvgRSSI(Integer avgRSSI) {
        this.avgRSSI = avgRSSI;
    }
}
