package dao;

import java.util.ArrayList;

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
    private static ArrayList<Game> allGames;

    // equals to getName
    // 5 elements construction ( 1 2 3 4 5)
    // 1 -> 3    ===  1 2 3
    // 5 -> 2    ===  5 4 2
    //
    // , that return 3 element content of Cunctruction

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
                new ScType(ScType.ArcCommonConst),
                new ScType(),
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
        ArrayList<ScAddr> result = new ArrayList<ScAddr>();
        SctpIterator iter3 = sctpClient.iterate3(SctpIterator.Iterator3F_A_A,
                scNodeClass,
                new ScType(ScType.ArcPosConstPerm),
                new ScType(ScType.NodeConstClass)); // game
        while(iter3.next()){
            result.add(iter3.value(2));
        }
        System.out.println("get All Games (2) result: ");
        System.out.println(result);
        System.out.println("get All Games (2) result.length: " + result.size());
        System.out.println("get All Games (2) array from result: ");
        System.out.println(new ArrayList<ScAddr>(result));
        return new ArrayList<ScAddr>(result);
    }

    private ArrayList<Game> getAllGames(ScAddr scNodeClass){
        ArrayList<Game> result = new ArrayList<Game>();
        ArrayList<ScAddr> scAddresses = getAllGames(true, scNodeClass);
        for (int i = 0; i < scAddresses.size(); i += 1) {
            System.out.println("Game iter = " + i);
            result.add(this.getGameByScAddr(scAddresses.get(i)));
        }
        return result;
    }

    public ArrayList<Game> getAllGames(){
        return allGames;
    }

    public ArrayList<Game> getFilteredGames(String nameFilter,String publisherFilter,String developerFilter, String genreName) {
        ArrayList<Game> result = new ArrayList<>();
        for (int i = 0; i < allGames.size(); i += 1) {
            Game curGame = allGames.get(i);
            boolean filterNamePassed = passPartialFilter(nameFilter, curGame.getName());
            boolean filterPublisherPassed = passPartialFilter(publisherFilter, curGame.getCompanyPublisher());
            boolean filterDeveloperPassed = passPartialFilter(developerFilter, curGame.getCompanyDevelop());

            if (filterNamePassed && filterPublisherPassed && filterDeveloperPassed) {
                result.add(curGame);
            }
        }
        return result;
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
    public Game getGame(String name, String publisher, String developer){
        for (int i = 0; i < allGames.size(); i += 1) {
            Game curGame = allGames.get(i);
            boolean filterNamePassed = passFilter(name, curGame);
            boolean filterPublisherPassed = passFilter(publisher, curGame);
            // boolean filterDeveloperPassed = passFilter(developer, curGame);

            if (filterNamePassed && filterPublisherPassed) {
                return curGame;
            }
        }
        return null;
    }

    private boolean passPartialFilter(String filter, ArrayList<String> criteria) {
        if (!filter.equals("")){
            for (int j = 0; j < criteria.size(); j += 1) {
                System.out.println(criteria.get(j));
                if (criteria.get(j).indexOf(filter) != -1) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean passFilter(String filter, Game game) {
        if (!filter.equals("")){
            ArrayList<String> names = game.getName();
            for (int j = 0; j < names.size(); j += 1) {
                System.out.println(names.get(j));
                if (names.get(j).equals(filter)) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
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

        System.out.println("START ALL GAMES");
        System.out.println("====================================================================================");
        allGames = this.getAllGames(COMPUTER_GAME);
        System.out.println(allGames);
        System.out.println("====================================================================================");

        return flag;
    }
}
