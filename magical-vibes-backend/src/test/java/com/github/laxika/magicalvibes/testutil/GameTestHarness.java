package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.handler.GameMessageHandler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetZone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.BounceResolutionService;
import com.github.laxika.magicalvibes.service.CombatService;
import com.github.laxika.magicalvibes.service.CopyResolutionService;
import com.github.laxika.magicalvibes.service.CounterResolutionService;
import com.github.laxika.magicalvibes.service.DamageResolutionService;
import com.github.laxika.magicalvibes.service.DestructionResolutionService;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.ExileResolutionService;
import com.github.laxika.magicalvibes.service.EffectResolutionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.LibraryResolutionService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.LobbyService;
import com.github.laxika.magicalvibes.service.PreventionResolutionService;
import com.github.laxika.magicalvibes.service.AbilityActivationService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.ReconnectionService;
import com.github.laxika.magicalvibes.service.SpellCastingService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.TurnResolutionService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.ColorChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.effect.CreatureModResolutionService;
import com.github.laxika.magicalvibes.service.effect.EquipResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.WinConditionResolutionService;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameTestHarness {

    private static boolean oracleLoaded = false;

    private final GameRegistry gameRegistry;
    private final WebSocketSessionManager sessionManager;
    private final GameService gameService;
    private final GameQueryService gameQueryService;
    private final MessageHandler messageHandler;
    private final LobbyService lobbyService;
    private final GameData gameData;
    private final Player player1;
    private final Player player2;
    private final FakeConnection conn1;
    private final FakeConnection conn2;

    public GameTestHarness() {
        if (!oracleLoaded) {
            ScryfallOracleLoader.loadAll("./scryfall-cache");
            oracleLoaded = true;
        }

        gameRegistry = new GameRegistry();
        sessionManager = new WebSocketSessionManager(new JacksonConfig().objectMapper());
        CardViewFactory cardViewFactory = new CardViewFactory();
        PermanentViewFactory permanentViewFactory = new PermanentViewFactory(cardViewFactory);
        StackEntryViewFactory stackEntryViewFactory = new StackEntryViewFactory(cardViewFactory);
        gameQueryService = new GameQueryService(
                List.of(new StaticEffectResolutionService()));
        gameQueryService.init();
        PlayerInputService playerInputService = new PlayerInputService(sessionManager, cardViewFactory);
        GameBroadcastService gameBroadcastService = new GameBroadcastService(
                sessionManager, cardViewFactory, permanentViewFactory, stackEntryViewFactory, gameQueryService);
        DraftRegistry draftRegistry = new DraftRegistry();
        GameHelper gameHelper = new GameHelper(
                sessionManager, gameRegistry, cardViewFactory, gameQueryService, gameBroadcastService, playerInputService,
                draftRegistry, null);
        CombatService combatService = new CombatService(
                gameHelper, gameQueryService, gameBroadcastService, playerInputService, sessionManager);
        List<EffectHandlerProvider> providers = List.of(
                new DamageResolutionService(gameHelper, gameQueryService, gameBroadcastService),
                new DestructionResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService),
                new LibraryResolutionService(gameHelper, gameBroadcastService, sessionManager, cardViewFactory),
                new PreventionResolutionService(gameQueryService, gameBroadcastService),
                new CounterResolutionService(gameHelper, gameBroadcastService),
                new ExileResolutionService(gameHelper, gameQueryService, gameBroadcastService),
                new CopyResolutionService(gameBroadcastService),
                new GraveyardReturnResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService),
                new BounceResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService),
                new LifeResolutionService(gameQueryService, gameBroadcastService),
                new CreatureModResolutionService(gameQueryService, gameBroadcastService),
                new PlayerInteractionResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService, sessionManager, cardViewFactory),
                new PermanentControlResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService),
                new TurnResolutionService(gameHelper, combatService, gameBroadcastService),
                new EquipResolutionService(gameQueryService, gameBroadcastService),
                new WinConditionResolutionService(gameHelper, gameBroadcastService)
        );
        EffectResolutionService effectResolutionService = new EffectResolutionService(gameHelper, providers);
        effectResolutionService.init();
        TurnProgressionService turnProgressionService = new TurnProgressionService(
                combatService, gameHelper, gameQueryService, gameBroadcastService, playerInputService);
        TargetValidationService targetValidationService = new TargetValidationService(gameQueryService);
        SpellCastingService spellCastingService = new SpellCastingService(
                gameQueryService, gameHelper, gameBroadcastService, turnProgressionService, targetValidationService);
        AbilityActivationService abilityActivationService = new AbilityActivationService(
                gameHelper, gameQueryService, gameBroadcastService, targetValidationService,
                playerInputService, sessionManager);
        ColorChoiceHandlerService colorChoiceHandlerService = new ColorChoiceHandlerService(
                sessionManager, gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService);
        CardChoiceHandlerService cardChoiceHandlerService = new CardChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService, abilityActivationService);
        PermanentChoiceHandlerService permanentChoiceHandlerService = new PermanentChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService);
        GraveyardChoiceHandlerService graveyardChoiceHandlerService = new GraveyardChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService, turnProgressionService);
        MayAbilityHandlerService mayAbilityHandlerService = new MayAbilityHandlerService(
                gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService);
        LibraryChoiceHandlerService libraryChoiceHandlerService = new LibraryChoiceHandlerService(
                sessionManager, gameQueryService, gameHelper, gameBroadcastService,
                cardViewFactory, turnProgressionService);
        StackResolutionService stackResolutionService = new StackResolutionService(
                gameHelper, gameQueryService, gameBroadcastService, effectResolutionService, playerInputService);
        MulliganService mulliganService = new MulliganService(
                sessionManager, gameBroadcastService, turnProgressionService);
        ReconnectionService reconnectionService = new ReconnectionService(
                sessionManager, cardViewFactory, combatService, gameQueryService);
        gameService = new GameService(
                gameRegistry, gameQueryService, gameBroadcastService,
                combatService,
                turnProgressionService,
                colorChoiceHandlerService, cardChoiceHandlerService,
                permanentChoiceHandlerService, graveyardChoiceHandlerService,
                mayAbilityHandlerService, libraryChoiceHandlerService,
                spellCastingService,
                stackResolutionService, abilityActivationService, mulliganService, reconnectionService);
        lobbyService = new LobbyService(gameRegistry, gameBroadcastService);

        // Create the MessageHandler (GameMessageHandler) for AI tests
        messageHandler = new GameMessageHandler(
                null, gameService, gameBroadcastService, lobbyService, gameRegistry,
                sessionManager, new JacksonConfig().objectMapper(),
                null, null, draftRegistry);

        player1 = new Player(UUID.randomUUID(), "Alice");
        player2 = new Player(UUID.randomUUID(), "Bob");
        conn1 = new FakeConnection("conn-1");
        conn2 = new FakeConnection("conn-2");

        sessionManager.registerPlayer(conn1, player1.getId(), player1.getUsername());
        sessionManager.registerPlayer(conn2, player2.getId(), player2.getUsername());

        lobbyService.createGame("Test Game", player1, "cho-mannos-resolve");
        GameData gd = gameRegistry.getGameForPlayer(player1.getId());
        lobbyService.joinGame(gd, player2, "cho-mannos-resolve");

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

    public void addMana(Player player, ManaColor color, int amount) {
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        for (int i = 0; i < amount; i++) {
            pool.add(color);
        }
    }

    public void addToBattlefield(Player player, Card card) {
        gameData.playerBattlefields.get(player.getId()).add(new Permanent(card));
    }

    public void setGraveyard(Player player, List<Card> cards) {
        gameData.playerGraveyards.put(player.getId(), new ArrayList<>(cards));
    }

    public void setLife(Player player, int life) {
        gameData.playerLifeTotals.put(player.getId(), life);
    }

    public void castCreature(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castEnchantment(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castEnchantment(Player player, int cardIndex, UUID targetPermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPermanentId, null);
    }

    public void castArtifact(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void playGraveyardLand(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), true);
    }

    public void castPlaneswalker(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castSorcery(Player player, int cardIndex, int xValue) {
        gameService.playCard(gameData, player, cardIndex, xValue, null, null);
    }

    public void castSorcery(Player player, int cardIndex, UUID targetPlayerId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPlayerId, null);
    }

    public void castSorcery(Player player, int cardIndex, int xValue, UUID targetId) {
        gameService.playCard(gameData, player, cardIndex, xValue, targetId, null);
    }

    public void castSorcery(Player player, int cardIndex, List<UUID> targetPermanentIds) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetPermanentIds, List.of());
    }

    public void castInstant(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castInstant(Player player, int cardIndex, UUID targetPermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPermanentId, null);
    }

    public void castInstant(Player player, int cardIndex, List<UUID> targetPermanentIds) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetPermanentIds, List.of());
    }

    public void castInstantWithConvoke(Player player, int cardIndex, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetPermanentIds, convokeCreatureIds);
    }

    public void sacrificePermanent(Player player, int permanentIndex, UUID targetPermanentId) {
        gameService.sacrificePermanent(gameData, player, permanentIndex, targetPermanentId);
    }

    public void activateAbility(Player player, int permanentIndex, Integer xValue, UUID targetPermanentId) {
        gameService.activateAbility(gameData, player, permanentIndex, 0, xValue, targetPermanentId, null);
    }

    public void activateAbility(Player player, int permanentIndex, Integer xValue, UUID targetPermanentId, TargetZone targetZone) {
        gameService.activateAbility(gameData, player, permanentIndex, 0, xValue, targetPermanentId, targetZone);
    }

    public void activateAbility(Player player, int permanentIndex, int abilityIndex, Integer xValue, UUID targetPermanentId) {
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, null);
    }

    public void handlePermanentChosen(Player player, UUID permanentId) {
        gameService.handlePermanentChosen(gameData, player, permanentId);
    }

    public void handleMultiplePermanentsChosen(Player player, List<UUID> permanentIds) {
        gameService.handleMultiplePermanentsChosen(gameData, player, permanentIds);
    }

    public void handleMultipleGraveyardCardsChosen(Player player, List<UUID> cardIds) {
        gameService.handleMultipleGraveyardCardsChosen(gameData, player, cardIds);
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
        // Determine priority order based on active player
        Player first, second;
        if (gameData.activePlayerId != null && gameData.activePlayerId.equals(player2.getId())) {
            first = player2;
            second = player1;
        } else {
            first = player1;
            second = player2;
        }

        TurnStep stepBefore = gameData.currentStep;
        int stackSizeBefore = gameData.stack.size();

        gameService.passPriority(gameData, first);

        // After auto-pass rework, the first pass may trigger an auto-pass cascade
        // that handles the second player too (advancing the step or resolving the stack).
        // Only pass for the second player if the game state hasn't changed.
        if (gameData.currentStep != stepBefore || gameData.stack.size() != stackSizeBefore) {
            return;
        }

        gameService.passPriority(gameData, second);
    }

    public void handleCardChosen(Player player, int cardIndex) {
        gameService.handleCardChosen(gameData, player, cardIndex);
    }

    public void handleGraveyardCardChosen(Player player, int cardIndex) {
        gameService.handleGraveyardCardChosen(gameData, player, cardIndex);
    }

    public void handleColorChosen(Player player, String colorName) {
        gameService.handleColorChosen(gameData, player, colorName);
    }

    public void handleMayAbilityChosen(Player player, boolean accepted) {
        gameService.handleMayAbilityChosen(gameData, player, accepted);
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

    public GameRegistry getGameRegistry() {
        return gameRegistry;
    }

    public GameQueryService getGameQueryService() {
        return gameQueryService;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public WebSocketSessionManager getSessionManager() {
        return sessionManager;
    }

    public void clearMessages() {
        conn1.clearMessages();
        conn2.clearMessages();
    }
}
