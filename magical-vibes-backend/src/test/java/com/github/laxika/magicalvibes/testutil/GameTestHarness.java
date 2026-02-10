package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.LobbyService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import com.github.laxika.magicalvibes.config.JacksonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameTestHarness {

    private final GameRegistry gameRegistry;
    private final WebSocketSessionManager sessionManager;
    private final GameService gameService;
    private final LobbyService lobbyService;
    private final GameData gameData;
    private final Player player1;
    private final Player player2;
    private final FakeConnection conn1;
    private final FakeConnection conn2;

    public GameTestHarness() {
        gameRegistry = new GameRegistry();
        sessionManager = new WebSocketSessionManager(new JacksonConfig().objectMapper());
        gameService = new GameService(sessionManager);
        lobbyService = new LobbyService(gameRegistry, gameService);

        player1 = new Player(UUID.randomUUID(), "Alice");
        player2 = new Player(UUID.randomUUID(), "Bob");
        conn1 = new FakeConnection("conn-1");
        conn2 = new FakeConnection("conn-2");

        sessionManager.registerPlayer(conn1, player1.getId(), player1.getUsername());
        sessionManager.registerPlayer(conn2, player2.getId(), player2.getUsername());

        lobbyService.createGame("Test Game", player1);
        GameData gd = gameRegistry.getGameForPlayer(player1.getId());
        lobbyService.joinGame(gd, player2);

        this.gameData = gameRegistry.getGameForPlayer(player1.getId());

        // Force player1 as starting player for deterministic tests
        this.gameData.startingPlayerId = player1.getId();
    }

    public void skipMulligan() {
        gameService.keepHand(gameData, player1);
        gameService.keepHand(gameData, player2);
    }

    public void setHand(Player player, List<Card> cards) {
        gameData.playerHands.put(player.getId(), new ArrayList<>(cards));
    }

    public void addMana(Player player, String color, int amount) {
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        for (int i = 0; i < amount; i++) {
            pool.add(color);
        }
    }

    public void addToBattlefield(Player player, Card card) {
        gameData.playerBattlefields.get(player.getId()).add(new Permanent(card));
    }

    public void setLife(Player player, int life) {
        gameData.playerLifeTotals.put(player.getId(), life);
    }

    public void castCreature(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null);
    }

    public void castEnchantment(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null);
    }

    public void castSorcery(Player player, int cardIndex, int xValue) {
        gameService.playCard(gameData, player, cardIndex, xValue, null);
    }

    public void castInstant(Player player, int cardIndex, UUID targetPermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPermanentId);
    }

    public UUID getPermanentId(Player player, String cardName) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().getName().equals(cardName)) {
                return p.getId();
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }

    public void passPriority(Player player) {
        gameService.passPriority(gameData, player);
    }

    public void passBothPriorities() {
        gameService.passPriority(gameData, player1);
        gameService.passPriority(gameData, player2);
    }

    public void handleCardChosen(Player player, int cardIndex) {
        gameService.handleCardChosen(gameData, player, cardIndex);
    }

    public void forceActivePlayer(Player player) {
        gameData.activePlayerId = player.getId();
        gameData.startingPlayerId = player.getId();
    }

    public void forceStep(TurnStep step) {
        gameData.currentStep = step;
    }

    public void clearPriorityPassed() {
        gameData.priorityPassedBy.clear();
    }

    public GameData getGameData() {
        return gameData;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public FakeConnection getConn1() {
        return conn1;
    }

    public FakeConnection getConn2() {
        return conn2;
    }

    public GameService getGameService() {
        return gameService;
    }

    public void clearMessages() {
        conn1.clearMessages();
        conn2.clearMessages();
    }
}
