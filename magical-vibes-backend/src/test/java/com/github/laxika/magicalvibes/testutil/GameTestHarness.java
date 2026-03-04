package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.handler.GameMessageHandler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.BounceResolutionService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.battlefield.CopyResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.CounterResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.combat.DamageResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.DestructionResolutionService;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.battlefield.ExileResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.LibraryResolutionService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.LobbyService;
import com.github.laxika.magicalvibes.service.PreventionResolutionService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.ability.ActivatedAbilityExecutionService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.ReconnectionService;
import com.github.laxika.magicalvibes.service.SpellCastingService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.ValidTargetService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.TurnResolutionService;
import com.github.laxika.magicalvibes.service.TargetRedirectionResolutionService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.ColorChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import com.github.laxika.magicalvibes.service.input.XValueChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.effect.CreatureModResolutionService;
import com.github.laxika.magicalvibes.service.effect.CardSpecificResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EquipResolutionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.service.effect.HandlesStaticEffect;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationContext;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.effect.TargetValidatorRegistry;
import com.github.laxika.magicalvibes.service.effect.ValidatesTarget;
import com.github.laxika.magicalvibes.service.effect.WinConditionResolutionService;
import com.github.laxika.magicalvibes.service.validate.BounceTargetValidators;
import com.github.laxika.magicalvibes.service.validate.CreatureModTargetValidators;
import com.github.laxika.magicalvibes.service.validate.DamageTargetValidators;
import com.github.laxika.magicalvibes.service.validate.DestructionTargetValidators;
import com.github.laxika.magicalvibes.service.validate.GraveyardTargetValidators;
import com.github.laxika.magicalvibes.service.validate.LibraryTargetValidators;
import com.github.laxika.magicalvibes.service.validate.LifeTargetValidators;
import com.github.laxika.magicalvibes.service.validate.PermanentControlTargetValidators;
import com.github.laxika.magicalvibes.websocket.WebSocketSessionManager;
import com.github.laxika.magicalvibes.config.JacksonConfig;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    private final LegendRuleService legendRuleService;

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
        StaticEffectHandlerRegistry staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        gameQueryService = new GameQueryService(staticEffectHandlerRegistry);
        StaticEffectResolutionService staticEffectResolutionService = new StaticEffectResolutionService(gameQueryService);
        scanStaticEffectHandlers(staticEffectResolutionService, staticEffectHandlerRegistry);
        PlayerInputService playerInputService = new PlayerInputService(sessionManager, cardViewFactory);
        GameBroadcastService gameBroadcastService = new GameBroadcastService(
                sessionManager, cardViewFactory, permanentViewFactory, stackEntryViewFactory, gameQueryService);
        DraftRegistry draftRegistry = new DraftRegistry();
        legendRuleService = new LegendRuleService(playerInputService);
        TriggeredAbilityQueueService triggeredAbilityQueueService = new TriggeredAbilityQueueService(
                gameQueryService, gameBroadcastService, playerInputService);
        CreatureControlService creatureControlService = new CreatureControlService(gameBroadcastService, gameQueryService);
        GameHelper gameHelper = new GameHelper(
                sessionManager, gameRegistry, cardViewFactory, gameQueryService, gameBroadcastService, playerInputService,
                legendRuleService, triggeredAbilityQueueService, draftRegistry, null, creatureControlService, null);
        AuraAttachmentService auraAttachmentService = new AuraAttachmentService(gameQueryService, gameBroadcastService, gameHelper);
        PermanentRemovalService permanentRemovalService = new PermanentRemovalService(
                gameHelper, auraAttachmentService, gameQueryService, gameBroadcastService);
        TriggerCollectionService triggerCollectionService = new TriggerCollectionService(
                gameHelper, permanentRemovalService, gameQueryService, gameBroadcastService, playerInputService, triggeredAbilityQueueService, creatureControlService);
        gameHelper.setTriggerCollectionService(triggerCollectionService);
        StateBasedActionService stateBasedActionService = new StateBasedActionService(
                gameHelper, gameQueryService, gameBroadcastService, permanentRemovalService);
        CombatService combatService = new CombatService(
                gameHelper, gameQueryService, gameBroadcastService, playerInputService, sessionManager, permanentRemovalService, triggerCollectionService);
        TargetValidatorRegistry targetValidatorRegistry = new TargetValidatorRegistry();
        TargetValidationService targetValidationService = new TargetValidationService(gameQueryService, targetValidatorRegistry);
        List<Object> validatorBeans = List.of(
                new DamageTargetValidators(targetValidationService, gameQueryService),
                new CreatureModTargetValidators(targetValidationService),
                new DestructionTargetValidators(targetValidationService, gameQueryService),
                new GraveyardTargetValidators(targetValidationService, gameQueryService),
                new BounceTargetValidators(targetValidationService),
                new LibraryTargetValidators(targetValidationService),
                new PermanentControlTargetValidators(targetValidationService, gameQueryService),
                new LifeTargetValidators(targetValidationService)
        );
        for (Object bean : validatorBeans) {
            scanTargetValidators(bean, targetValidatorRegistry);
        }
        TargetLegalityService targetLegalityService = new TargetLegalityService(gameQueryService, targetValidationService);
        ValidTargetService validTargetService = new ValidTargetService(gameQueryService);
        EffectHandlerRegistry effectHandlerRegistry = new EffectHandlerRegistry();
        LifeResolutionService lifeResolutionService = new LifeResolutionService(gameQueryService, gameBroadcastService, playerInputService);
        DamageResolutionService damageResolutionService = new DamageResolutionService(gameHelper, gameQueryService, gameBroadcastService, permanentRemovalService, triggerCollectionService, lifeResolutionService);
        ExileResolutionService exileResolutionService = new ExileResolutionService(gameHelper, gameQueryService, gameBroadcastService, permanentRemovalService, playerInputService, cardViewFactory, triggerCollectionService);
        PlayerInteractionResolutionService playerInteractionResolutionService = new PlayerInteractionResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService, sessionManager, cardViewFactory, permanentRemovalService, triggerCollectionService);
        List<Object> effectServices = List.of(
                damageResolutionService,
                new DestructionResolutionService(gameHelper, permanentRemovalService, gameQueryService, gameBroadcastService, playerInputService),
                new LibraryResolutionService(gameHelper, gameBroadcastService, sessionManager, cardViewFactory),
                new PreventionResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new CounterResolutionService(gameHelper, gameBroadcastService, gameQueryService),
                exileResolutionService,
                new CopyResolutionService(gameBroadcastService, validTargetService, gameHelper, gameQueryService),
                new TargetRedirectionResolutionService(gameQueryService, gameBroadcastService, playerInputService, targetLegalityService),
                new GraveyardReturnResolutionService(gameHelper, permanentRemovalService, legendRuleService, gameQueryService, gameBroadcastService, playerInputService),
                new BounceResolutionService(gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService),
                lifeResolutionService,
                new CreatureModResolutionService(gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService, triggerCollectionService),
                playerInteractionResolutionService,
                new PermanentControlResolutionService(gameHelper, legendRuleService, gameQueryService, gameBroadcastService, playerInputService, permanentRemovalService, triggerCollectionService, creatureControlService),
                new TurnResolutionService(gameHelper, combatService, gameBroadcastService, auraAttachmentService),
                new EquipResolutionService(gameQueryService, gameBroadcastService, permanentRemovalService),
                new CardSpecificResolutionService(gameHelper, gameQueryService, gameBroadcastService, sessionManager, cardViewFactory, permanentRemovalService, legendRuleService),
                new WinConditionResolutionService(gameHelper, gameBroadcastService, gameQueryService)
        );
        for (Object service : effectServices) {
            scanEffectHandlers(service, effectHandlerRegistry);
        }
        EffectResolutionService effectResolutionService = new EffectResolutionService(gameQueryService, effectHandlerRegistry, gameBroadcastService, permanentRemovalService);
        StackResolutionService stackResolutionService = new StackResolutionService(
                gameHelper, legendRuleService, stateBasedActionService, gameQueryService, targetLegalityService,
                gameBroadcastService, effectResolutionService, playerInputService, triggerCollectionService, creatureControlService);
        TurnProgressionService turnProgressionService = new TurnProgressionService(
                combatService, gameHelper, gameQueryService, gameBroadcastService, playerInputService, triggerCollectionService, permanentRemovalService, auraAttachmentService, stackResolutionService);
        SpellCastingService spellCastingService = new SpellCastingService(
                gameQueryService, gameHelper, gameBroadcastService, turnProgressionService, targetLegalityService, permanentRemovalService, triggerCollectionService);
        ActivatedAbilityExecutionService activatedAbilityExecutionService = new ActivatedAbilityExecutionService(
                gameHelper, permanentRemovalService, triggerCollectionService, stateBasedActionService, gameQueryService, gameBroadcastService, playerInputService, sessionManager);
        AbilityActivationService abilityActivationService = new AbilityActivationService(
                gameHelper, gameQueryService, gameBroadcastService, targetLegalityService, activatedAbilityExecutionService,
                playerInputService, sessionManager, permanentRemovalService, triggerCollectionService);
        ColorChoiceHandlerService colorChoiceHandlerService = new ColorChoiceHandlerService(
                sessionManager, gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService, legendRuleService);
        CardChoiceHandlerService cardChoiceHandlerService = new CardChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, triggerCollectionService, turnProgressionService, abilityActivationService, effectResolutionService, playerInteractionResolutionService);
        PermanentChoiceHandlerService permanentChoiceHandlerService = new PermanentChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService, abilityActivationService,
                permanentRemovalService, playerInputService, stateBasedActionService, triggerCollectionService, creatureControlService, turnProgressionService, effectResolutionService, damageResolutionService);
        GraveyardChoiceHandlerService graveyardChoiceHandlerService = new GraveyardChoiceHandlerService(
                gameQueryService, gameHelper, legendRuleService, gameBroadcastService, turnProgressionService, permanentRemovalService, triggerCollectionService);
        MayAbilityHandlerService mayAbilityHandlerService = new MayAbilityHandlerService(
                gameQueryService, gameHelper, stateBasedActionService, gameBroadcastService,
                playerInputService, turnProgressionService, targetLegalityService, sessionManager, permanentRemovalService, triggerCollectionService);
        XValueChoiceHandlerService xValueChoiceHandlerService = new XValueChoiceHandlerService(
                gameBroadcastService, stateBasedActionService,
                playerInputService, turnProgressionService, effectResolutionService);
        LibraryChoiceHandlerService libraryChoiceHandlerService = new LibraryChoiceHandlerService(
                sessionManager, gameQueryService, gameHelper, legendRuleService, stateBasedActionService, gameBroadcastService,
                cardViewFactory, turnProgressionService, playerInputService);
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
                mayAbilityHandlerService, xValueChoiceHandlerService, libraryChoiceHandlerService,
                spellCastingService,
                stackResolutionService, abilityActivationService, mulliganService, reconnectionService,
                exileResolutionService);
        lobbyService = new LobbyService(gameRegistry, gameBroadcastService);

        // Create the MessageHandler (GameMessageHandler) for AI tests
        messageHandler = new GameMessageHandler(
                null, gameService, gameBroadcastService, lobbyService, gameRegistry,
                sessionManager, new JacksonConfig().objectMapper(),
                null, null, draftRegistry, null, validTargetService);

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

    public void castArtifact(Player player, int cardIndex, UUID targetPermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPermanentId, null);
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

    public void castSorceryWithSacrifice(Player player, int cardIndex, UUID sacrificePermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, List.of(), List.of(), false, sacrificePermanentId);
    }

    public void castSorceryWithSacrifice(Player player, int cardIndex, UUID targetPermanentId, UUID sacrificePermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPermanentId, null, List.of(), List.of(), false, sacrificePermanentId);
    }

    public void castInstant(Player player, int cardIndex) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null);
    }

    public void castInstant(Player player, int cardIndex, UUID targetPermanentId) {
        gameService.playCard(gameData, player, cardIndex, 0, targetPermanentId, null);
    }

    public void castInstant(Player player, int cardIndex, int xValue, UUID targetPermanentId) {
        gameService.playCard(gameData, player, cardIndex, xValue, targetPermanentId, null);
    }

    public void castInstant(Player player, int cardIndex, List<UUID> targetPermanentIds) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetPermanentIds, List.of());
    }

    public void castInstantWithConvoke(Player player, int cardIndex, List<UUID> targetPermanentIds, List<UUID> convokeCreatureIds) {
        gameService.playCard(gameData, player, cardIndex, 0, null, null, targetPermanentIds, convokeCreatureIds);
    }

    public void castAndResolveInstant(Player player, int cardIndex) {
        castInstant(player, cardIndex);
        passBothPriorities();
    }

    public void castAndResolveInstant(Player player, int cardIndex, UUID targetPermanentId) {
        castInstant(player, cardIndex, targetPermanentId);
        passBothPriorities();
    }

    public void castAndResolveInstant(Player player, int cardIndex, List<UUID> targetPermanentIds) {
        castInstant(player, cardIndex, targetPermanentIds);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, int xValue) {
        castSorcery(player, cardIndex, xValue);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, UUID targetPlayerId) {
        castSorcery(player, cardIndex, targetPlayerId);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, int xValue, UUID targetId) {
        castSorcery(player, cardIndex, xValue, targetId);
        passBothPriorities();
    }

    public void castAndResolveSorcery(Player player, int cardIndex, List<UUID> targetPermanentIds) {
        castSorcery(player, cardIndex, targetPermanentIds);
        passBothPriorities();
    }

    public void sacrificePermanent(Player player, int permanentIndex, UUID targetPermanentId) {
        gameService.sacrificePermanent(gameData, player, permanentIndex, targetPermanentId);
    }

    public void activateAbility(Player player, int permanentIndex, Integer xValue, UUID targetPermanentId) {
        gameService.activateAbility(gameData, player, permanentIndex, 0, xValue, targetPermanentId, null);
    }

    public void activateAbility(Player player, int permanentIndex, Integer xValue, UUID targetPermanentId, Zone Zone) {
        gameService.activateAbility(gameData, player, permanentIndex, 0, xValue, targetPermanentId, Zone);
    }

    public void activateAbility(Player player, int permanentIndex, int abilityIndex, Integer xValue, UUID targetPermanentId) {
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, null);
    }

    public void activateAbility(Player player, int permanentIndex, int abilityIndex, Integer xValue, UUID targetPermanentId, Zone targetZone) {
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetPermanentId, targetZone);
    }

    public void activateAbilityWithMultiTargets(Player player, int permanentIndex, int abilityIndex, List<UUID> targetPermanentIds) {
        gameService.activateAbility(gameData, player, permanentIndex, abilityIndex, null, null, null, targetPermanentIds);
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

    public void handleXValueChosen(Player player, int chosenValue) {
        gameService.handleXValueChosen(gameData, player, chosenValue);
    }

    public void paySearchTax(Player player) {
        gameService.paySearchTax(gameData, player);
    }

    public void handleCombatDamageAssigned(Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        gameService.handleCombatDamageAssigned(gameData, player, attackerIndex, assignments);
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

    public void assertLife(Player player, int expectedLife) {
        assertThat(gameData.playerLifeTotals.get(player.getId())).isEqualTo(expectedLife);
    }

    public void assertOnBattlefield(Player player, String cardName) {
        assertThat(gameData.playerBattlefields.get(player.getId()))
                .anyMatch(p -> p.getCard().getName().equals(cardName));
    }

    public void assertNotOnBattlefield(Player player, String cardName) {
        assertThat(gameData.playerBattlefields.get(player.getId()))
                .noneMatch(p -> p.getCard().getName().equals(cardName));
    }

    public void assertInGraveyard(Player player, String cardName) {
        assertThat(gameData.playerGraveyards.get(player.getId()))
                .anyMatch(c -> c.getName().equals(cardName));
    }

    public void assertNotInGraveyard(Player player, String cardName) {
        assertThat(gameData.playerGraveyards.get(player.getId()))
                .noneMatch(c -> c.getName().equals(cardName));
    }

    public void assertInHand(Player player, String cardName) {
        assertThat(gameData.playerHands.get(player.getId()))
                .anyMatch(c -> c.getName().equals(cardName));
    }

    public void assertNotInHand(Player player, String cardName) {
        assertThat(gameData.playerHands.get(player.getId()))
                .noneMatch(c -> c.getName().equals(cardName));
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

    public LegendRuleService getLegendRuleService() {
        return legendRuleService;
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

    @SuppressWarnings("unchecked")
    private static void scanEffectHandlers(Object bean, EffectHandlerRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            HandlesEffect annotation = method.getAnnotation(HandlesEffect.class);
            if (annotation == null) continue;

            method.setAccessible(true);
            Class<?>[] params = method.getParameterTypes();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

                if (params.length == 3
                        && params[0] == GameData.class
                        && params[1] == StackEntry.class
                        && CardEffect.class.isAssignableFrom(params[2])) {
                    Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[2];
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry, effectParam.cast(effect)); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 2
                        && params[0] == GameData.class
                        && params[1] == StackEntry.class) {
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void scanTargetValidators(Object bean, TargetValidatorRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            ValidatesTarget annotation = method.getAnnotation(ValidatesTarget.class);
            if (annotation == null) continue;

            method.setAccessible(true);
            Class<?>[] params = method.getParameterTypes();
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

                if (params.length == 2
                        && params[0] == TargetValidationContext.class
                        && CardEffect.class.isAssignableFrom(params[1])) {
                    Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[1];
                    registry.register(annotation.value(), (ctx, effect) -> {
                        try { handle.invoke(ctx, effectParam.cast(effect)); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 1
                        && params[0] == TargetValidationContext.class) {
                    registry.register(annotation.value(), (ctx, effect) -> {
                        try { handle.invoke(ctx); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void scanStaticEffectHandlers(Object bean, StaticEffectHandlerRegistry registry) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            HandlesStaticEffect annotation = method.getAnnotation(HandlesStaticEffect.class);
            if (annotation == null) continue;

            method.setAccessible(true);
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method).bindTo(bean);

                if (annotation.selfOnly()) {
                    registry.registerSelfHandler(annotation.value(), (context, effect, accumulator) -> {
                        try { handle.invoke(context, effect, accumulator); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else {
                    registry.register(annotation.value(), (context, effect, accumulator) -> {
                        try { handle.invoke(context, effect, accumulator); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

