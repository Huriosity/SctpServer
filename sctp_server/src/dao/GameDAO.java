package dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sctp.*;
import entities.Game;

public class GameDAO {
    private static ScAddr COMPUTER_GAME;
    private static ScAddr GENRE;
    private static ScAddr SETTING;
    private static ScAddr COMPANY_DEVELOP;
    private static ScAddr COMPANY_PUBLISHER;
    private static ScAddr PLATFORM;
    private static ScAddr ENGINE;
    private static ScAddr MAIN_IDTF;

    private SctpClient sctpClient;

    // equals to getName
    // 5 elements construction ( 1 2 3 4 5)
    // 1 -> 3    ===  1 2 3
    // 5 -> 2    ===  5 4 2
    //
    // , that return 3 element of Cunctruction
    // new ScType(ScType.ArcCommon)
    // ArcCommonConst

    private String get(ScAddr game, ScAddr elem){
        SctpIterator iter5 = sctpClient.iterate5(SctpIterator.Iterator5F_A_A_A_F,
                game,
                new ScType(),
                new ScType(),
                new ScType(),
                elem);
        if(iter5.next()){
            if(sctpClient.getLinkContent(iter5.value(2)) != null){
                return SctpClient.ByteBufferToString(sctpClient.getLinkContent(iter5.value(2)));
            }
            else{
                return this.get(iter5.value(2), MAIN_IDTF);
            }
        }
        return "";
    }

    private String get3(ScAddr game, ScAddr elem){
        SctpIterator iter3 = sctpClient.iterate3(SctpIterator.Iterator3F_A_A,
                elem,
                new ScType(),
                new ScType());
        while(iter3.next()){
            SctpIterator iter32 = sctpClient.iterate3(SctpIterator.Iterator3F_A_F,
                    iter3.value(2),
                    new ScType(),
                    game);
            if(iter32.next()){
                return this.get(iter3.value(2), MAIN_IDTF);
            }
        }
        return "";
    }

    private ArrayList<String> getAllGameElements(ScAddr game,ScAddr elem, boolean elemIs3nodeConstruction){
        if (elemIs3nodeConstruction == false) {
            return getAllGameElements(game, elem);
        }
        ArrayList<String> result = new ArrayList<String>();
        SctpIterator iter3 = sctpClient.iterate3(SctpIterator.Iterator3F_A_A,
                elem,
                new ScType(),
                new ScType());
        while(iter3.next()){
            SctpIterator iter32 = sctpClient.iterate3(SctpIterator.Iterator3F_A_F,
                    iter3.value(2),
                    new ScType(),
                    game);
            if(iter32.next()){
                result.add(this.get(iter3.value(2), MAIN_IDTF));
            }
        }

        return result;
    }

    private ArrayList<String> getAllGameElements(ScAddr game,ScAddr elem){
        ArrayList<String> result = new ArrayList<String>();
        SctpIterator iter5 = sctpClient.iterate5(SctpIterator.Iterator5F_A_A_A_F,
                game,
                //new ScType(),
                //new ScType(),
                //new ScType(),
                new ScType(ScType.ArcCommonConst),
                new ScType(),
                // new ScType(ScType.Link),
                new ScType(ScType.ArcPosConstPerm),
                elem);
        while(iter5.next()){
            if(sctpClient.getLinkContent(iter5.value(2)) != null){
                result.add(SctpClient.ByteBufferToString(sctpClient.getLinkContent(iter5.value(2))));
            } else {
                result.add(this.get(iter5.value(2), MAIN_IDTF));
            }

        }
        return result;
    }

    private ArrayList<ScAddr> getAllGames(boolean needScAddresses, ScAddr scNodeClass){
        Set<ScAddr> result = new HashSet<ScAddr>();
        SctpIterator iter3 = sctpClient.iterate3(SctpIterator.Iterator3F_A_A,
                scNodeClass,
                new ScType(ScType.ArcPosConstPerm),
                new ScType(ScType.NodeConstClass)); // game
        while(iter3.next()){
            result.add(iter3.value(2));
        }
        System.out.println("get All Games (2) result: ");
        System.out.println(result);
        System.out.println("get All Games (2) array from result: ");
        System.out.println(new ArrayList<ScAddr>(result));
        return new ArrayList<ScAddr>(result);
    }

    public ArrayList<Game> getAllGames(ScAddr scNodeClass){
        ArrayList<Game> result = new ArrayList<Game>();
        ArrayList<ScAddr> scAddresses = getAllGames(true, scNodeClass);
        for (int i = 0; i < scAddresses.size(); i += 1) {
            //System.out.println();
            result.add(this.getGameByScAddr(scAddresses.get(i)));
        }
        return result;
    }

    public ArrayList<Game> getAllGames(){
        return getAllGames(COMPUTER_GAME);
    }

    public ArrayList<Game> getFilteredGames(String filter, String genreName) {
        long startTime = System.currentTimeMillis();
        long startMethod = startTime;
        ScAddr genreClass = sctpClient.findElementBySystemIdentifier(genreName);
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time of get genre class: " + (endTime-startTime) + "ms");
        if (genreClass == null) {
            return new ArrayList<>();
        }
        ArrayList<Game> filteredGames = new ArrayList<>();
        startTime = System.currentTimeMillis();
        ArrayList<ScAddr> scAddresses = getAllGames(true, genreClass);
        System.out.println("get scAddresses array from result: ");
        System.out.println(scAddresses);
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time of getAllGames method: " + (endTime-startTime) + "ms");
        for (int i = 0; i < scAddresses.size(); i += 1) {
            ScAddr curAddr = scAddresses.get(i);
            ArrayList<String> names = getAllGameElements(curAddr, MAIN_IDTF);
            for (int j = 0; j < names.size(); j += 1) {
                if (names.get(j).indexOf(filter) != -1) {
                    System.out.println("FIND !!!!!!!!!!!!!!!!!!!!!!!!!! " + i);
                    // Game game = getGameByScAddr(curAddr);
                    // filteredGames.add(game);
                    break;
                }
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time of getFilteredGames method: " + (endTime-startMethod) + "ms");
        return filteredGames;
    }


    private ScAddr findNodeById(ScAddr addr, String name) {
        SctpIterator iter3 = sctpClient.iterate3(SctpIterator.Iterator3F_A_A,
                addr,
                new ScType(),
                new ScType());
        if(iter3 == null){
            return null;
        }
        while (iter3.next()) {
            if(name.equals(this.get(iter3.value(2), MAIN_IDTF))){
                return iter3.value(2);
            }
        }
        return null;
    }

    private Game getGameByScAddr(ScAddr scGame) {
        System.out.println("scGame addr " + scGame);
        Game game = new Game();
        game.setScAddr(scGame.getValue());
        game.setName(getAllGameElements(scGame, MAIN_IDTF));
        game.setCompanyPublisher(getAllGameElements(scGame, COMPANY_PUBLISHER));
        game.setCompanyDevelop(getAllGameElements(scGame, COMPANY_DEVELOP));
        game.setPlatform(getAllGameElements(scGame, PLATFORM));
        game.setEngine(getAllGameElements(scGame, ENGINE));
        game.setGenre(getAllGameElements(scGame, GENRE, true));
        game.setSetting( getAllGameElements(scGame, SETTING, true));

        return game;
    }
    public Game getGame(String name){
        Game game = new Game();
        ScAddr scGame = this.findNodeById(COMPUTER_GAME, name);

        game.setScAddr(scGame.getValue());
        game.setName(getAllGameElements(scGame, MAIN_IDTF));
        game.setCompanyPublisher(getAllGameElements(scGame, COMPANY_PUBLISHER));
        game.setCompanyDevelop(getAllGameElements(scGame, COMPANY_DEVELOP));
        game.setPlatform(getAllGameElements(scGame, PLATFORM));
        game.setEngine(getAllGameElements(scGame, ENGINE));
        game.setGenre(getAllGameElements(scGame, GENRE, true));
        game.setSetting( getAllGameElements(scGame, SETTING, true));

        return game;

    }
    public void getFullInfoAboutGame(String name) {

        ScAddr scGame = this.findNodeById(COMPUTER_GAME, name);
        ArrayList<String> allNames = getAllGameElements(scGame, MAIN_IDTF);
        ArrayList<String> publisherNames = getAllGameElements(scGame, COMPANY_PUBLISHER);
        ArrayList<String> developerNames = getAllGameElements(scGame, COMPANY_DEVELOP);
        ArrayList<String> platformNames = getAllGameElements(scGame, PLATFORM);
        ArrayList<String> engineNames = getAllGameElements(scGame, ENGINE);
        ArrayList<String> genreNames = getAllGameElements(scGame, GENRE, true);
        ArrayList<String> settingNames = getAllGameElements(scGame, SETTING, true);

        for (int j = 0; j < allNames.size(); j += 1) {
            printScAddrIdf(allNames.get(j));
        }

        System.out.println("publishers");
        System.out.println(publisherNames.size());
        for (int j = 0; j < publisherNames.size(); j += 1) {
            printScAddrIdf(publisherNames.get(j));
        }

        System.out.println("developers");
        System.out.println(developerNames.size());
        for (int j = 0; j < developerNames.size(); j += 1) {
            printScAddrIdf(developerNames.get(j));
        }

        System.out.println("platformNames");
        System.out.println(platformNames.size());
        for (int j = 0; j < platformNames.size(); j += 1) {
            printScAddrIdf(platformNames.get(j));
        }

        System.out.println("engineNames");
        System.out.println(engineNames.size());
        for (int j = 0; j < engineNames.size(); j += 1) {
            printScAddrIdf(engineNames.get(j));
        }

        System.out.println("genreNames");
        System.out.println(genreNames.size());
        for (int j = 0; j < genreNames.size(); j += 1) {
            printScAddrIdf(genreNames.get(j));
        }

        System.out.println("settingNames");
        System.out.println(settingNames.size());
        for (int j = 0; j < settingNames.size(); j += 1) {
            printScAddrIdf(settingNames.get(j));
        }
    }

    public void printScAddrIdf(String ScAddrIdf) {
        if (ScAddrIdf == "") {
            System.out.println("Sorry bro, but man, who add this concept in bd don't add idf");
        } else {
            System.out.println(ScAddrIdf);
        }
    }

    public boolean connect(){
        boolean flag = false;
        sctpClient = new SctpClient();
        if(sctpClient.connect("localhost", 55770)){
            System.out.println("Connect success");
            flag = true;
        }
        COMPUTER_GAME = sctpClient.findElementBySystemIdentifier("concept_computer_game");
        GENRE = sctpClient.findElementBySystemIdentifier("concept_game_genre");
        SETTING = sctpClient.findElementBySystemIdentifier("concept_setting");
        COMPANY_DEVELOP = sctpClient.findElementBySystemIdentifier("nrel_developer");
        COMPANY_PUBLISHER = sctpClient.findElementBySystemIdentifier("nrel_publisher");
        PLATFORM = sctpClient.findElementBySystemIdentifier("nrel_platform");
        ENGINE = sctpClient.findElementBySystemIdentifier("nrel_game_engine");
        MAIN_IDTF = sctpClient.findElementBySystemIdentifier("nrel_main_idtf");

        return flag;
    }
}
