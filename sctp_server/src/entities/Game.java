package entities;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class Game {
    private int scAddr;

    private ArrayList<String> name;
    private ArrayList<String> genre;
    private ArrayList<String> setting;
    private ArrayList<String> companyDevelop;
    private ArrayList<String> companyPublisher;
    private ArrayList<String> engine;
    private ArrayList<String> platform;

    public Game(){
        this.scAddr = 0;
        this.name = new ArrayList<>();
        this.genre = new ArrayList<>();
        this.setting = new ArrayList<>();
        this.companyDevelop = new ArrayList<>();
        this.companyPublisher = new ArrayList<>();
        this.engine = new ArrayList<>();
        this.platform = new ArrayList<>();
    }

    public ArrayList<String> getName() {
        return name;
    }

    public void setName(ArrayList<String> name) {
        this.name = name;
    }

    public ArrayList<String> getGenre() {
        return genre;
    }

    public void setGenre(ArrayList<String> genre) {
        this.genre = genre;
    }

    public ArrayList<String> getSetting() {
        return setting;
    }

    public void setSetting(ArrayList<String> setting) {
        this.setting = setting;
    }

    public ArrayList<String> getCompanyDevelop() {
        return companyDevelop;
    }

    public void setCompanyDevelop(ArrayList<String> companyDevelop) {
        this.companyDevelop = companyDevelop;
    }

    public ArrayList<String> getCompanyPublisher() {
        return companyPublisher;
    }

    public void setCompanyPublisher(ArrayList<String> companyPublisher) {
        this.companyPublisher = companyPublisher;
    }

    public ArrayList<String> getEngine() {
        return engine;
    }

    public void setEngine(ArrayList<String> engine) {
        this.engine = engine;
    }

    public ArrayList<String> getPlatform() {
        return platform;
    }

    public void setPlatform(ArrayList<String> platform) {
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
        jsonObject.put("publisher", this.getCompanyPublisher());
        jsonObject.put("platform", this.getPlatform());

        return  jsonObject;
    }
}