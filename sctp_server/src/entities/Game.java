package entities;

import org.json.simple.JSONObject;

public class Game {
    private int scAddr;
    private String name;
    private String genre;
    private String setting;
    private String companyDevelop;
    private String companyRelease;
    private String engine;
    private String platform;

    public Game(){
        this.scAddr = 0;
        this.name = "";
        this.genre = "";
        this.setting = "";
        this.companyDevelop = "";
        this.companyRelease = "";
        this.engine = "";
        this.platform = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getCompanyDevelop() {
        return companyDevelop;
    }

    public void setCompanyDevelop(String companyDevelop) {
        this.companyDevelop = companyDevelop;
    }

    public String getCompanyRelease() {
        return companyRelease;
    }

    public void setCompanyRelease(String companyRelease) {
        this.companyRelease = companyRelease;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getScAddr() {
        return scAddr;
    }

    public void setScAddr(int scAddr) {
        this.scAddr = scAddr;
    }

    public JSONObject getGameAsJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", this.getName());
        jsonObject.put("genre", this.getGenre());
        jsonObject.put("setting", this.getSetting());
        jsonObject.put("developer", this.getCompanyDevelop());
        jsonObject.put("publisher", this.getCompanyRelease());
        jsonObject.put("platform", this.getPlatform());

        return  jsonObject;
    }
}