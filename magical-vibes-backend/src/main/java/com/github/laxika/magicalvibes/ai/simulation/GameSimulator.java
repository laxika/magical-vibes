package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.ai.BoardEvaluator;
import com.github.laxika.magicalvibes.ai.CombatSimulator;
import com.github.laxika.magicalvibes.ai.SpellEvaluator;
import com.github.laxika.magicalvibes.config.EffectRegistryConfig;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.AbilityActivationService;
import com.github.laxika.magicalvibes.service.ActivatedAbilityExecutionService;
import com.github.laxika.magicalvibes.service.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.BounceResolutionService;
import com.github.laxika.magicalvibes.service.CombatService;
import com.github.laxika.magicalvibes.service.CopyResolutionService;
import com.github.laxika.magicalvibes.service.CounterResolutionService;
import com.github.laxika.magicalvibes.service.DamageResolutionService;
import com.github.laxika.magicalvibes.service.DestructionResolutionService;
import com.github.laxika.magicalvibes.service.DraftRegistry;
import com.github.laxika.magicalvibes.service.EffectResolutionService;
import com.github.laxika.magicalvibes.service.ExileResolutionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.GraveyardReturnResolutionService;
import com.github.laxika.magicalvibes.service.LegendRuleService;
import com.github.laxika.magicalvibes.service.LibraryResolutionService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.PreventionResolutionService;
import com.github.laxika.magicalvibes.service.ReconnectionService;
import com.github.laxika.magicalvibes.service.SpellCastingService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TargetLegalityService;
import com.github.laxika.magicalvibes.service.TargetRedirectionResolutionService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.TurnResolutionService;
import com.github.laxika.magicalvibes.service.effect.CardSpecificResolutionService;
import com.github.laxika.magicalvibes.service.effect.CreatureModResolutionService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.EquipResolutionService;
import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.service.effect.HandlesStaticEffect;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.effect.PermanentControlResolutionService;
import com.github.laxika.magicalvibes.service.effect.PlayerInteractionResolutionService;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.StaticEffectResolutionService;
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
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.ColorChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Provides MCTS simulation capabilities by wiring up all game services with a no-op session manager.
 * Follows the same manual wiring pattern as GameTestHarness.
 *
 * Key responsibilities:
 * - Enumerate legal actions for a given game state and player
 * - Apply actions to a (copied) game state
 * - Auto-resolve opponent decisions using heuristics
 * - Detect terminal states
 */
@Slf4j
public class GameSimulator {

    private final GameService gameService;
    private final GameQueryService gameQueryService;
    private final GameRegistry gameRegistry;
    private final BoardEvaluator boardEvaluator;
    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;

    public GameSimulator(GameQueryService sharedQueryService) {
        NoOpSessionManager noOpSession = new NoOpSessionManager();
        CardViewFactory cardViewFactory = new CardViewFactory();
        PermanentViewFactory permanentViewFactory = new PermanentViewFactory(cardViewFactory);
        StackEntryViewFactory stackEntryViewFactory = new StackEntryViewFactory(cardViewFactory);

        StaticEffectHandlerRegistry staticEffectHandlerRegistry = new StaticEffectHandlerRegistry();
        StaticEffectResolutionService staticEffectResolutionService = new StaticEffectResolutionService(sharedQueryService);
        scanStaticEffectHandlers(staticEffectResolutionService, staticEffectHandlerRegistry);

        this.gameQueryService = sharedQueryService;
        PlayerInputService playerInputService = new PlayerInputService(noOpSession, cardViewFactory);
        GameBroadcastService gameBroadcastService = new GameBroadcastService(
                noOpSession, cardViewFactory, permanentViewFactory, stackEntryViewFactory, gameQueryService);
        DraftRegistry draftRegistry = new DraftRegistry();
        this.gameRegistry = new GameRegistry();

        LegendRuleService legendRuleService = new LegendRuleService(gameQueryService, playerInputService);
        AuraAttachmentService auraAttachmentService = new AuraAttachmentService(gameQueryService, gameBroadcastService);
        TriggeredAbilityQueueService triggeredAbilityQueueService = new TriggeredAbilityQueueService(
                gameQueryService, gameBroadcastService, playerInputService);
        GameHelper gameHelper = new GameHelper(
                noOpSession, gameRegistry, cardViewFactory, gameQueryService, gameBroadcastService, playerInputService,
                legendRuleService, auraAttachmentService, triggeredAbilityQueueService, draftRegistry, null);
        StateBasedActionService stateBasedActionService = new StateBasedActionService(
                gameHelper, gameQueryService, gameBroadcastService);
        CombatService combatService = new CombatService(
                gameHelper, gameQueryService, gameBroadcastService, playerInputService, noOpSession);
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

        EffectHandlerRegistry effectHandlerRegistry = new EffectHandlerRegistry();
        List<Object> effectServices = List.of(
                new DamageResolutionService(gameHelper, gameQueryService, gameBroadcastService),
                new DestructionResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService),
                new LibraryResolutionService(gameHelper, gameBroadcastService, noOpSession, cardViewFactory),
                new PreventionResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new CounterResolutionService(gameHelper, gameBroadcastService, gameQueryService),
                new ExileResolutionService(gameHelper, gameQueryService, gameBroadcastService),
                new CopyResolutionService(gameBroadcastService),
                new TargetRedirectionResolutionService(gameQueryService, gameBroadcastService, playerInputService, targetLegalityService),
                new GraveyardReturnResolutionService(gameHelper, legendRuleService, gameQueryService, gameBroadcastService, playerInputService),
                new BounceResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService),
                new LifeResolutionService(gameQueryService, gameBroadcastService),
                new CreatureModResolutionService(gameQueryService, gameBroadcastService, playerInputService),
                new PlayerInteractionResolutionService(gameHelper, gameQueryService, gameBroadcastService, playerInputService, noOpSession, cardViewFactory),
                new PermanentControlResolutionService(gameHelper, legendRuleService, gameQueryService, gameBroadcastService, playerInputService),
                new TurnResolutionService(gameHelper, combatService, gameBroadcastService),
                new EquipResolutionService(gameQueryService, gameBroadcastService),
                new CardSpecificResolutionService(gameHelper, gameQueryService),
                new WinConditionResolutionService(gameHelper, gameBroadcastService, gameQueryService)
        );
        for (Object service : effectServices) {
            scanEffectHandlers(service, effectHandlerRegistry);
        }

        EffectResolutionService effectResolutionService = new EffectResolutionService(gameHelper, effectHandlerRegistry, gameBroadcastService);
        TurnProgressionService turnProgressionService = new TurnProgressionService(
                combatService, gameHelper, gameQueryService, gameBroadcastService, playerInputService);
        SpellCastingService spellCastingService = new SpellCastingService(
                gameQueryService, gameHelper, gameBroadcastService, turnProgressionService, targetLegalityService);
        ActivatedAbilityExecutionService activatedAbilityExecutionService = new ActivatedAbilityExecutionService(
                gameHelper, stateBasedActionService, gameQueryService, gameBroadcastService, playerInputService, noOpSession);
        AbilityActivationService abilityActivationService = new AbilityActivationService(
                gameHelper, gameQueryService, gameBroadcastService, targetLegalityService, activatedAbilityExecutionService,
                playerInputService, noOpSession);
        ColorChoiceHandlerService colorChoiceHandlerService = new ColorChoiceHandlerService(
                noOpSession, gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService, legendRuleService);
        CardChoiceHandlerService cardChoiceHandlerService = new CardChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService,
                playerInputService, turnProgressionService, abilityActivationService);
        PermanentChoiceHandlerService permanentChoiceHandlerService = new PermanentChoiceHandlerService(
                gameQueryService, gameHelper, gameBroadcastService, abilityActivationService,
                playerInputService, stateBasedActionService, turnProgressionService);
        GraveyardChoiceHandlerService graveyardChoiceHandlerService = new GraveyardChoiceHandlerService(
                gameQueryService, gameHelper, legendRuleService, gameBroadcastService, turnProgressionService);
        MayAbilityHandlerService mayAbilityHandlerService = new MayAbilityHandlerService(
                gameQueryService, gameHelper, stateBasedActionService, gameBroadcastService,
                playerInputService, turnProgressionService, targetLegalityService, noOpSession);
        LibraryChoiceHandlerService libraryChoiceHandlerService = new LibraryChoiceHandlerService(
                noOpSession, gameQueryService, gameHelper, legendRuleService, stateBasedActionService, gameBroadcastService,
                cardViewFactory, turnProgressionService, playerInputService);
        StackResolutionService stackResolutionService = new StackResolutionService(
                gameHelper, legendRuleService, stateBasedActionService, gameQueryService, targetLegalityService,
                gameBroadcastService, effectResolutionService, playerInputService);
        MulliganService mulliganService = new MulliganService(
                noOpSession, gameBroadcastService, turnProgressionService);
        ReconnectionService reconnectionService = new ReconnectionService(
                noOpSession, cardViewFactory, combatService, gameQueryService);

        this.gameService = new GameService(
                gameRegistry, gameQueryService, gameBroadcastService,
                combatService,
                turnProgressionService,
                colorChoiceHandlerService, cardChoiceHandlerService,
                permanentChoiceHandlerService, graveyardChoiceHandlerService,
                mayAbilityHandlerService, libraryChoiceHandlerService,
                spellCastingService,
                stackResolutionService, abilityActivationService, mulliganService, reconnectionService);

        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
    }

    /**
     * Returns the list of legal actions for the given player in the current game state.
     */
    public List<SimulationAction> getLegalActions(GameData gd, UUID playerId) {
        List<SimulationAction> actions = new ArrayList<>();

        AwaitingInput awaitingInput = gd.interaction.awaitingInputType();

        if (awaitingInput == null) {
            // Normal priority — can cast spells or pass
            boolean isMainPhase = gd.currentStep == TurnStep.PRECOMBAT_MAIN
                    || gd.currentStep == TurnStep.POSTCOMBAT_MAIN;
            boolean isActivePlayer = playerId.equals(gd.activePlayerId);

            if (isMainPhase && isActivePlayer && gd.stack.isEmpty()) {
                // Enumerate castable spells
                List<Card> hand = gd.playerHands.get(playerId);
                if (hand != null) {
                    ManaPool virtualPool = buildVirtualManaPool(gd, playerId);
                    for (int i = 0; i < hand.size(); i++) {
                        Card card = hand.get(i);
                        if (card.getType() == CardType.LAND) continue;
                        if (card.getType() == CardType.INSTANT) continue;
                        if (card.getManaCost() == null) continue;
                        ManaCost cost = new ManaCost(card.getManaCost());
                        if (!cost.canPay(virtualPool)) continue;
                        // For targeted spells, try to find a target
                        UUID targetId = null;
                        if (card.isNeedsTarget() || card.isAura()) {
                            targetId = findBestTarget(gd, card, playerId);
                            if (targetId == null) continue; // no valid target
                        }
                        actions.add(new SimulationAction.PlayCard(i, targetId));
                    }
                }
            }
            // Always can pass priority
            actions.add(new SimulationAction.PassPriority());
            return actions;
        }

        switch (awaitingInput) {
            case ATTACKER_DECLARATION -> {
                List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
                List<Integer> availableIndices = new ArrayList<>();
                for (int i = 0; i < battlefield.size(); i++) {
                    Permanent perm = battlefield.get(i);
                    if (!gameQueryService.isCreature(gd, perm)) continue;
                    if (perm.isTapped()) continue;
                    if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
                    if (gameQueryService.hasKeyword(gd, perm, Keyword.DEFENDER)) continue;
                    availableIndices.add(i);
                }
                // Use CombatSimulator to find best attackers, then also offer empty attack
                List<Integer> bestAttackers = combatSimulator.findBestAttackers(gd, playerId, availableIndices);
                actions.add(new SimulationAction.DeclareAttackers(List.of())); // no attack
                if (!bestAttackers.isEmpty()) {
                    actions.add(new SimulationAction.DeclareAttackers(bestAttackers));
                }
                // Also try all-in attack if different from best
                if (!availableIndices.isEmpty() && !availableIndices.equals(bestAttackers)) {
                    actions.add(new SimulationAction.DeclareAttackers(availableIndices));
                }
            }
            case BLOCKER_DECLARATION -> {
                // Use CombatSimulator for blockers — offer best blocking and no blocking
                actions.add(new SimulationAction.DeclareBlockers(List.of()));
                List<int[]> bestBlockers = findBestBlockerAssignments(gd, playerId);
                if (!bestBlockers.isEmpty()) {
                    actions.add(new SimulationAction.DeclareBlockers(bestBlockers));
                }
            }
            case CARD_CHOICE, DISCARD_CHOICE, REVEALED_HAND_CHOICE -> {
                var cardChoice = gd.interaction.cardChoiceContext();
                if (cardChoice != null && cardChoice.validIndices() != null) {
                    for (int idx : cardChoice.validIndices()) {
                        actions.add(new SimulationAction.ChooseCard(idx));
                    }
                }
            }
            case PERMANENT_CHOICE -> {
                var permChoice = gd.interaction.permanentChoiceContextView();
                if (permChoice != null && permChoice.validIds() != null) {
                    for (UUID id : permChoice.validIds()) {
                        actions.add(new SimulationAction.ChoosePermanent(id));
                    }
                }
            }
            case COLOR_CHOICE -> {
                actions.add(new SimulationAction.ChooseColor("WHITE"));
                actions.add(new SimulationAction.ChooseColor("BLUE"));
                actions.add(new SimulationAction.ChooseColor("BLACK"));
                actions.add(new SimulationAction.ChooseColor("RED"));
                actions.add(new SimulationAction.ChooseColor("GREEN"));
            }
            case MAY_ABILITY_CHOICE -> {
                actions.add(new SimulationAction.MayAbilityChoice(true));
                actions.add(new SimulationAction.MayAbilityChoice(false));
            }
            default -> {
                // For complex interactions (library search, reorder, etc.), use heuristic
                actions.add(new SimulationAction.PassPriority());
            }
        }

        if (actions.isEmpty()) {
            actions.add(new SimulationAction.PassPriority());
        }
        return actions;
    }

    /**
     * Applies an action to the game state. The game state is mutated in place.
     * After applying, auto-resolves any pending decisions for other players.
     */
    public void applyAction(GameData gd, UUID playerId, SimulationAction action) {
        // Register the game in the local registry so GameService can find it
        gameRegistry.register(gd);
        Player player = new Player(playerId, gd.playerIdToName.getOrDefault(playerId, "AI"));

        try {
            synchronized (gd) {
                switch (action) {
                    case SimulationAction.PlayCard pc -> {
                        tapLandsForCard(gd, playerId, gd.playerHands.get(playerId).get(pc.handIndex()));
                        gameService.playCard(gd, player, pc.handIndex(), 0, pc.targetPermanentId(), null);
                    }
                    case SimulationAction.PassPriority ignored ->
                            gameService.passPriority(gd, player);
                    case SimulationAction.DeclareAttackers da ->
                            gameService.declareAttackers(gd, player, da.attackerIndices());
                    case SimulationAction.DeclareBlockers db -> {
                        List<BlockerAssignment> assignments = db.blockerAssignments().stream()
                                .map(a -> new BlockerAssignment(a[0], a[1]))
                                .toList();
                        gameService.declareBlockers(gd, player, assignments);
                    }
                    case SimulationAction.ChooseCard cc ->
                            gameService.handleCardChosen(gd, player, cc.cardIndex());
                    case SimulationAction.ChoosePermanent cp ->
                            gameService.handlePermanentChosen(gd, player, cp.permanentId());
                    case SimulationAction.ChooseColor col ->
                            gameService.handleColorChosen(gd, player, col.color());
                    case SimulationAction.MayAbilityChoice mac ->
                            gameService.handleMayAbilityChosen(gd, player, mac.accept());
                    case SimulationAction.ActivateAbility aa ->
                            gameService.activateAbility(gd, player, findPermanentIndex(gd, playerId, aa.permanentId()),
                                    aa.abilityIndex(), 0, aa.targetPermanentId(), null);
                }
            }
        } catch (Exception e) {
            // Simulation may hit edge cases — swallow and continue
            log.trace("Simulation action failed: {}", e.getMessage());
        }

        // Auto-resolve pending decisions for any player (including opponent)
        autoResolveDecisions(gd, playerId, 10);
    }

    /**
     * Returns true if the game is over (finished or a player is at 0 or less life).
     */
    public boolean isTerminal(GameData gd) {
        if (gd.status == GameStatus.FINISHED) return true;
        for (UUID pid : gd.orderedPlayerIds) {
            if (gd.playerLifeTotals.getOrDefault(pid, 20) <= 0) return true;
        }
        return false;
    }

    /**
     * Evaluates the game state from the AI's perspective, normalized to [0, 1].
     * 1.0 = AI wins, 0.0 = opponent wins, 0.5 = even.
     */
    public double evaluate(GameData gd, UUID aiPlayerId) {
        double raw = boardEvaluator.evaluate(gd, aiPlayerId);
        // Normalize using sigmoid-like function: map (-inf, inf) to (0, 1)
        return 1.0 / (1.0 + Math.exp(-raw / 50.0));
    }

    public GameQueryService getGameQueryService() {
        return gameQueryService;
    }

    public BoardEvaluator getBoardEvaluator() {
        return boardEvaluator;
    }

    public SpellEvaluator getSpellEvaluator() {
        return spellEvaluator;
    }

    public CombatSimulator getCombatSimulator() {
        return combatSimulator;
    }

    public GameService getGameService() {
        return gameService;
    }

    public GameRegistry getGameRegistry() {
        return gameRegistry;
    }

    // ===== Private helpers =====

    /**
     * Auto-resolves any pending interactions using heuristic decisions.
     * Loops until no more decisions are pending or max iterations reached.
     */
    private void autoResolveDecisions(GameData gd, UUID mctsPlayerId, int maxIterations) {
        for (int i = 0; i < maxIterations; i++) {
            if (isTerminal(gd)) return;

            AwaitingInput awaiting = gd.interaction.awaitingInputType();
            if (awaiting == null) {
                // Check if we need to pass priority for the non-MCTS player
                UUID priorityHolder = getPriorityPlayer(gd);
                if (priorityHolder != null && !priorityHolder.equals(mctsPlayerId)) {
                    Player oppPlayer = new Player(priorityHolder, "opp");
                    try {
                        synchronized (gd) {
                            gameService.passPriority(gd, oppPlayer);
                        }
                    } catch (Exception e) {
                        return;
                    }
                    continue;
                }
                return; // MCTS player's turn to decide
            }

            // Determine which player the interaction is for
            UUID interactionPlayer = getInteractionPlayer(gd, awaiting);
            if (interactionPlayer == null) return;

            Player resolvePlayer = new Player(interactionPlayer, gd.playerIdToName.getOrDefault(interactionPlayer, "AI"));

            try {
                synchronized (gd) {
                    resolveInteraction(gd, resolvePlayer, awaiting, mctsPlayerId);
                }
            } catch (Exception e) {
                log.trace("Auto-resolve failed: {}", e.getMessage());
                return;
            }
        }
    }

    private void resolveInteraction(GameData gd, Player player, AwaitingInput awaiting, UUID mctsPlayerId) {
        switch (awaiting) {
            case ATTACKER_DECLARATION -> {
                UUID pid = player.getId();
                List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(pid, List.of());
                List<Integer> available = new ArrayList<>();
                for (int i = 0; i < battlefield.size(); i++) {
                    Permanent perm = battlefield.get(i);
                    if (!gameQueryService.isCreature(gd, perm)) continue;
                    if (perm.isTapped()) continue;
                    if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
                    if (gameQueryService.hasKeyword(gd, perm, Keyword.DEFENDER)) continue;
                    available.add(i);
                }
                List<Integer> attackers = combatSimulator.findBestAttackers(gd, pid, available);
                gameService.declareAttackers(gd, player, attackers);
            }
            case BLOCKER_DECLARATION -> {
                List<int[]> blockers = findBestBlockerAssignments(gd, player.getId());
                List<BlockerAssignment> assignments = blockers.stream()
                        .map(a -> new BlockerAssignment(a[0], a[1]))
                        .toList();
                gameService.declareBlockers(gd, player, assignments);
            }
            case CARD_CHOICE, DISCARD_CHOICE -> {
                var cc = gd.interaction.cardChoiceContext();
                if (cc != null && cc.validIndices() != null && !cc.validIndices().isEmpty()) {
                    // Pick lowest value card
                    List<Card> hand = gd.playerHands.get(player.getId());
                    int bestIdx = cc.validIndices().iterator().next();
                    if (hand != null) {
                        bestIdx = cc.validIndices().stream()
                                .min(Comparator.comparingDouble(idx ->
                                        spellEvaluator.estimateSpellValue(gd, hand.get(idx), player.getId())))
                                .orElse(bestIdx);
                    }
                    gameService.handleCardChosen(gd, player, bestIdx);
                }
            }
            case PERMANENT_CHOICE -> {
                var pc = gd.interaction.permanentChoiceContextView();
                if (pc != null && pc.validIds() != null && !pc.validIds().isEmpty()) {
                    UUID chosen = pc.validIds().iterator().next();
                    gameService.handlePermanentChosen(gd, player, chosen);
                }
            }
            case COLOR_CHOICE -> gameService.handleColorChosen(gd, player, "RED");
            case MAY_ABILITY_CHOICE -> gameService.handleMayAbilityChosen(gd, player, true);
            case GRAVEYARD_CHOICE -> {
                var gc = gd.interaction.graveyardChoiceContext();
                if (gc != null && gc.validIndices() != null && !gc.validIndices().isEmpty()) {
                    gameService.handleGraveyardCardChosen(gd, player, gc.validIndices().iterator().next());
                }
            }
            case MULTI_PERMANENT_CHOICE -> {
                var mpc = gd.interaction.multiPermanentChoiceContext();
                if (mpc != null && mpc.validIds() != null && !mpc.validIds().isEmpty()) {
                    List<UUID> chosen = mpc.validIds().stream().limit(mpc.maxCount()).toList();
                    gameService.handleMultiplePermanentsChosen(gd, player, chosen);
                }
            }
            case MULTI_GRAVEYARD_CHOICE -> {
                var mgc = gd.interaction.multiGraveyardChoiceContext();
                if (mgc != null && mgc.validCardIds() != null && !mgc.validCardIds().isEmpty()) {
                    List<UUID> chosen = mgc.validCardIds().stream().limit(mgc.maxCount()).toList();
                    gameService.handleMultipleGraveyardCardsChosen(gd, player, chosen);
                }
            }
            case COMBAT_DAMAGE_ASSIGNMENT -> {
                var cda = gd.interaction.combatDamageAssignmentContext();
                if (cda != null) {
                    Map<UUID, Integer> assignments = autoAssignCombatDamage(cda);
                    gameService.handleCombatDamageAssigned(gd, player, cda.attackerIndex(), assignments);
                }
            }
            case LIBRARY_SEARCH -> {
                var ls = gd.interaction.librarySearchContext();
                if (ls != null && ls.cards() != null && !ls.cards().isEmpty()) {
                    gameService.handleLibraryCardChosen(gd, player, 0);
                }
            }
            case LIBRARY_REORDER -> {
                var lr = gd.interaction.libraryReorderContext();
                if (lr != null && lr.cards() != null) {
                    List<Integer> order = new ArrayList<>();
                    for (int k = 0; k < lr.cards().size(); k++) order.add(k);
                    gameService.handleLibraryCardsReordered(gd, player, order);
                }
            }
            case REVEALED_HAND_CHOICE -> {
                var rhc = gd.interaction.revealedHandChoiceContext();
                if (rhc != null && rhc.validIndices() != null && !rhc.validIndices().isEmpty()) {
                    gameService.handleCardChosen(gd, player, rhc.validIndices().iterator().next());
                }
            }
            case HAND_TOP_BOTTOM_CHOICE -> gameService.handleHandTopBottomChosen(gd, player, 0, 1);
            case LIBRARY_REVEAL_CHOICE -> {
                var lrc = gd.interaction.libraryRevealChoiceContext();
                if (lrc != null && lrc.validCardIds() != null && !lrc.validCardIds().isEmpty()) {
                    gameService.handleLibraryCardChosen(gd, player, 0);
                }
            }
            default -> {
                // Unknown interaction — skip
            }
        }
    }

    private UUID getInteractionPlayer(GameData gd, AwaitingInput awaiting) {
        var ctx = gd.interaction.currentContext();
        if (ctx == null) return null;
        return switch (ctx) {
            case InteractionContext.AttackerDeclaration ad -> ad.activePlayerId();
            case InteractionContext.BlockerDeclaration bd -> bd.defenderId();
            case InteractionContext.CardChoice cc -> cc.playerId();
            case InteractionContext.PermanentChoice pc -> pc.playerId();
            case InteractionContext.GraveyardChoice gc -> gc.playerId();
            case InteractionContext.ColorChoice cc -> cc.playerId();
            case InteractionContext.MayAbilityChoice mc -> mc.playerId();
            case InteractionContext.MultiPermanentChoice mpc -> mpc.playerId();
            case InteractionContext.MultiGraveyardChoice mgc -> mgc.playerId();
            case InteractionContext.LibraryReorder lr -> lr.playerId();
            case InteractionContext.LibrarySearch ls -> ls.playerId();
            case InteractionContext.LibraryRevealChoice lrc -> lrc.playerId();
            case InteractionContext.HandTopBottomChoice htbc -> htbc.playerId();
            case InteractionContext.RevealedHandChoice rhc -> rhc.choosingPlayerId();
            case InteractionContext.CombatDamageAssignment cda -> cda.playerId();
        };
    }

    private UUID getPriorityPlayer(GameData gd) {
        if (gd.activePlayerId == null) return null;
        if (!gd.priorityPassedBy.contains(gd.activePlayerId)) {
            return gd.activePlayerId;
        }
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(gd.activePlayerId) && !gd.priorityPassedBy.contains(id)) {
                return id;
            }
        }
        return null;
    }

    private List<int[]> findBestBlockerAssignments(GameData gd, UUID playerId) {
        UUID opponentId = getOpponentId(gd, playerId);
        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < oppBattlefield.size(); i++) {
            if (oppBattlefield.get(i).isAttacking()) attackerIndices.add(i);
        }
        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (!gameQueryService.isCreature(gd, perm)) continue;
            if (perm.isTapped()) continue;
            blockerIndices.add(i);
        }
        return combatSimulator.findBestBlockers(gd, playerId, attackerIndices, blockerIndices);
    }

    private ManaPool buildVirtualManaPool(GameData gd, UUID playerId) {
        ManaPool virtual = new ManaPool();
        ManaPool current = gd.playerManaPools.get(playerId);
        if (current != null) {
            for (ManaColor color : ManaColor.values()) {
                for (int i = 0; i < current.get(color); i++) virtual.add(color);
            }
        }
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (Permanent perm : battlefield) {
            if (perm.isTapped()) continue;
            if (gameQueryService.isCreature(gd, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.ON_TAP)) {
                if (effect instanceof AwardManaEffect me) virtual.add(me.color());
                else if (effect instanceof AwardAnyColorManaEffect) virtual.add(ManaColor.COLORLESS);
            }
        }
        return virtual;
    }

    private void tapLandsForCard(GameData gd, UUID playerId, Card card) {
        if (card.getManaCost() == null) return;
        ManaCost cost = new ManaCost(card.getManaCost());
        ManaPool currentPool = gd.playerManaPools.get(playerId);
        if (cost.canPay(currentPool)) return;

        Player player = new Player(playerId, "sim");
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (perm.isTapped()) continue;
            if (gameQueryService.isCreature(gd, perm) && perm.isSummoningSick()
                    && !gameQueryService.hasKeyword(gd, perm, Keyword.HASTE)) continue;
            boolean producesMana = perm.getCard().getEffects(EffectSlot.ON_TAP).stream()
                    .anyMatch(e -> e instanceof AwardManaEffect || e instanceof AwardAnyColorManaEffect);
            if (!producesMana) continue;
            gameService.tapPermanent(gd, player, i);
            currentPool = gd.playerManaPools.get(playerId);
            if (cost.canPay(currentPool)) return;
        }
    }

    private UUID findBestTarget(GameData gd, Card card, UUID playerId) {
        UUID opponentId = getOpponentId(gd, playerId);
        List<Permanent> oppBattlefield = gd.playerBattlefields.getOrDefault(opponentId, List.of());

        // Prefer creatures that pass the target filter
        UUID creatureTarget = oppBattlefield.stream()
                .filter(p -> gameQueryService.isCreature(gd, p))
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gd, p)))
                .map(Permanent::getId)
                .orElse(null);
        if (creatureTarget != null) {
            return creatureTarget;
        }

        // Fall back to any permanent that passes the target filter (e.g., artifacts/enchantments for Naturalize)
        return oppBattlefield.stream()
                .filter(p -> passesTargetFilter(gd, card, p, playerId))
                .findFirst()
                .map(Permanent::getId)
                .orElse(null);
    }

    private boolean passesTargetFilter(GameData gd, Card card, Permanent target, UUID controllerId) {
        if (card.getTargetFilter() == null) {
            return true;
        }
        try {
            gameQueryService.validateTargetFilter(card.getTargetFilter(), target,
                    FilterContext.of(gd)
                            .withSourceCardId(card.getId())
                            .withSourceControllerId(controllerId));
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private int findPermanentIndex(GameData gd, UUID playerId, UUID permanentId) {
        List<Permanent> battlefield = gd.playerBattlefields.getOrDefault(playerId, List.of());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getId().equals(permanentId)) return i;
        }
        return -1;
    }

    private Map<UUID, Integer> autoAssignCombatDamage(InteractionContext.CombatDamageAssignment cda) {
        Map<UUID, Integer> assignments = new HashMap<>();
        int remaining = cda.totalDamage();
        for (var target : cda.validTargets()) {
            if (target.isPlayer()) continue;
            int lethal = target.effectiveToughness() - target.currentDamage();
            int dmg = Math.min(remaining, lethal);
            if (dmg > 0) {
                assignments.put(target.id(), dmg);
                remaining -= dmg;
            }
        }
        if (remaining > 0) {
            for (var target : cda.validTargets()) {
                if (target.isPlayer()) {
                    assignments.put(target.id(), remaining);
                    break;
                }
            }
        }
        return assignments;
    }

    private UUID getOpponentId(GameData gd, UUID playerId) {
        for (UUID id : gd.orderedPlayerIds) {
            if (!id.equals(playerId)) return id;
        }
        return null;
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
                        && params[1] == com.github.laxika.magicalvibes.model.StackEntry.class
                        && CardEffect.class.isAssignableFrom(params[2])) {
                    Class<? extends CardEffect> effectParam = (Class<? extends CardEffect>) params[2];
                    registry.register(annotation.value(), (gd, entry, effect) -> {
                        try { handle.invoke(gd, entry, effectParam.cast(effect)); }
                        catch (RuntimeException re) { throw re; }
                        catch (Throwable t) { throw new RuntimeException(t); }
                    });
                } else if (params.length == 2
                        && params[0] == GameData.class
                        && params[1] == com.github.laxika.magicalvibes.model.StackEntry.class) {
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
