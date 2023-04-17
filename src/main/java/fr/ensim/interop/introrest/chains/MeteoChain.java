package fr.ensim.interop.introrest.chains;

public class MeteoChain {
    String cityName;
    int delayIndex;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getDelayIndex() {
        return delayIndex;
    }

    public void setDelayIndex(int delayIndex) {
        this.delayIndex = delayIndex;
    }
}
