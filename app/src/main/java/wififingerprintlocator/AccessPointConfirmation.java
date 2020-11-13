public class AccessPointConfirmation {

    private String BSSID;
    private String SSID;

    public AccessPointConfirmation() {
    }

    public AccessPointConfirmation(String BSSID, String SSID) {
        this.BSSID = BSSID;
        this.SSID = SSID;
    }

    @Override
    public boolean equals(Object obj) {
        AccessPointConfirmation apc = (AccessPointConfirmation) obj;
        if (apc.BSSID.equals(BSSID) && apc.SSID.equals(SSID)) {
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
}
