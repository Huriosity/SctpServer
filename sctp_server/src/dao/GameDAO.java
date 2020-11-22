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
    private static ScAddr ID;

    private SctpClient sctpClient;

    // equals to getName
    // 5 elements construction ( 1 2 3 4 5)
    // 1 -> 3    ===  1 2 3
    // 5 -> 2    ===  5 4 2
    //
    // , that return 3 element of Cunctruction
    // new ScType(ScType.ArcCommon)
    // ArcCommonConst

    private ArrayList<String> getAllGameNamesOfElement(ScAddr elem){
        ArrayList<String> result = new ArrayList<String>();
        SctpIterator iter5 = sctpClient.iterate5(SctpIterator.Iterator5F_A_A_A_F,
                elem,
                // new ScType(),
                // new ScType(),
                // new ScType(),
                new ScType(ScType.ArcCommonConst),
                new ScType(ScType.Link),
                new ScType(ScType.ArcPosConstPerm),
                ID);
        while(iter5.next()){
            result.add(SctpClient.ByteBufferToString(sctpClient.getLinkContent(iter5.value(2))));
        }
        return result;
        /*if(iter5.next()){
            if(sctpClient.getLinkContent(iter5.value(2)) != null){
                return SctpClient.ByteBufferToString(sctpClient.getLinkContent(iter5.value(2)));
            }
            else{
                return this.get(iter5.value(2), ID);
            }
        }
        return ""*/
    }

    private ArrayList<ScAddr> getAllGames(){
        //ITERATOR_3F_A_A
        // Iterator3F_A_A
        ArrayList<ScAddr> result = new ArrayList<ScAddr>();
        SctpIterator iter3 = sctpClient.iterate3(SctpIterator.Iterator3F_A_A,
                COMPUTER_GAME,
                new ScType(ScType.ArcPosConstPerm),
                new ScType(ScType.NodeConstClass)); // game
        while(iter3.next()){
            result.add(iter3.value(2));
            // result.add(SctpClient.ByteBufferToString(sctpClient.getLinkContent(iter3.value(2))));
        }
        return result;
    }


    private String get(ScAddr game, ScAddr elem){
        SctpIterator iter5 = sctpClient.iterate5(SctpIterator.Iterator5F_A_A_A_F,
                game,
                new ScType(),
                new ScType(),
                new ScType(),
                // new ScType(ScType.ArcCommonConst),
                // new ScType(ScType.Link),
                // new ScType(ScType.ArcPosConstPerm),
                elem);
        if(iter5.next()){
            if(sctpClient.getLinkContent(iter5.value(2)) != null){
                return SctpClient.ByteBufferToString(sctpClient.getLinkContent(iter5.value(2)));
            }
            else{
                return this.get(iter5.value(2), ID);
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
                return this.get(iter3.value(2), ID);
            }
        }
        return "";
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
            if(name.equals(this.get(iter3.value(2), ID))){
                return iter3.value(2);
            }
        }
        return null;
    }

    public Game getGame(String name){
        // this.connect();
        System.out.println("name of game is:" + name);
        Game game = new Game();
        game.setName(name);
        ScAddr scGame = this.findNodeById(COMPUTER_GAME, name);
        if(scGame == null)
            return null;
        game.setScAddr(scGame.getValue());
        game.setName(this.get(scGame, ID));
        game.setCompanyDevelop(this.get(scGame, COMPANY_DEVELOP));
        game.setCompanyRelease(this.get(scGame, COMPANY_PUBLISHER));
        game.setEngine(this.get(scGame, ENGINE));
        game.setPlatform(this.get(scGame, PLATFORM));
        game.setGenre(this.get3(scGame, GENRE));
        game.setSetting(this.get3(scGame, SETTING));
        return game;
    }

    public void getFullInfoAboutGame() {
        ArrayList<ScAddr> allGameNames = getAllGames();

        // Game game = new Game();
        System.out.println("Start getAllNames method");
        for (int i = 0; i < allGameNames.size(); i += 1) {
            ScAddr scAddrId = allGameNames.get(i);
            System.out.println(scAddrId.getValue());
            ArrayList<String> allNames = getAllGameNamesOfElement(scAddrId);
            for (int j = 0; j < allNames.size(); j += 1) {
                System.out.println(allNames.get(j));
            }

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
        ID = sctpClient.findElementBySystemIdentifier("nrel_main_idtf");

        return flag;
    }
}
