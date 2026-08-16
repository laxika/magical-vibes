package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BeholdAndExileCost;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.GlobalMustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingAttackerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastingAbilityGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesForCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ReturnAnyNumberOfPermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.KeepHandRequest;
import com.github.laxika.magicalvibes.networking.message.MulliganRequest;
import com.github.laxika.magicalvibes.networking.message.ActivateAbilityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.networking.message.TapPermanentRequest;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.combat.CombatHelper;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityContext;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract base class for all AI difficulty levels. Provides message dispatch,
 * mulligan handling, land playing, and shared utility. Delegates interactive
 * choices to {@link AiChoiceHandler}, mana management to {@link AiManaManager},
 * and targeting to {@link AiTargetSelector}.
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #handleGameState} — priority and spell-casting strategy</li>
 *   <li>{@link #handleAttackers} — attacker declaration strategy</li>
 *   <li>{@link #handleBlockers} — blocker declaration strategy</li>
 * </ul>
 */
@Slf4j
public abstract class AiDecisionEngine {

    protected final UUID gameId;
    protected final Player aiPlayer;
    protected final GameRegistry gameRegistry;
    protected final AiGameActions gameActions;
    protected final GameQueryService gameQueryService;
    protected final BlockLegalityService blockLegalityService;
    protected final PredicateEvaluationService predicateEvaluationService;
    protected final CombatAttackService combatAttackService;
    protected final GameActionAvailabilityService actionAvailabilityService;
    protected final CastingCostService castingCostService;
    protected final CastingPermissionService castingPermissionService;

    protected final AiManaManager manaManager;
    protected final AiTargetSelector targetSelector;
    protected final AiChoiceHandler choiceHandler;

    /**
     * Convenience variant for callers holding a {@link GameService} rather than a ready-made
     * {@link AiGameActions}; it builds the latter and defers to the primary constructor.
     */
    public AiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                            GameService gameService, GameQueryService gameQueryService,
                            BlockLegalityService blockLegalityService,
                            CombatAttackService combatAttackService,
                            GameActionAvailabilityService actionAvailabilityService,
                            CastingCostService castingCostService,
                            CastingPermissionService castingPermissionService,
                            TargetValidationService targetValidationService,
                            TargetLegalityService targetLegalityService) {
        this(gameId, aiPlayer, gameRegistry,
                new AiGameActions(gameId, aiPlayer, gameService, gameRegistry),
                gameQueryService, blockLegalityService, combatAttackService,
                actionAvailabilityService, castingCostService, castingPermissionService,
                targetValidationService, targetLegalityService);
    }

    public AiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                            AiGameActions gameActions, GameQueryService gameQueryService,
                            BlockLegalityService blockLegalityService,
                            CombatAttackService combatAttackService,
                            GameActionAvailabilityService actionAvailabilityService,
                            CastingCostService castingCostService,
                            CastingPermissionService castingPermissionService,
                            TargetValidationService targetValidationService,
                            TargetLegalityService targetLegalityService) {
        this.gameId = gameId;
        this.aiPlayer = aiPlayer;
        this.gameRegistry = gameRegistry;
        this.gameActions = gameActions;
        this.gameQueryService = gameQueryService;
        this.blockLegalityService = blockLegalityService;
        this.predicateEvaluationService = new PredicateEvaluationService(gameQueryService);
        this.combatAttackService = combatAttackService;
        this.actionAvailabilityService = actionAvailabilityService;
        this.castingCostService = castingCostService;
        this.castingPermissionService = castingPermissionService;

        this.manaManager = new AiManaManager(gameQueryService, actionAvailabilityService.potentialManaService());
        BoardEvaluator boardEvaluator = new BoardEvaluator(gameQueryService);
        this.targetSelector = new AiTargetSelector(gameQueryService, targetValidationService,
                targetLegalityService, boardEvaluator);
        this.choiceHandler = new AiChoiceHandler(gameId, aiPlayer.getId(), gameQueryService, gameActions);
    }

    // ===== Internal Decision Dispatch =====

    public void handleEvent(AiDecisionKind kind) {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return;
        }

        switch (kind) {
            case GAME_STATE -> handleGameState(gameData);
            case MULLIGAN -> handleInitialMulligan();
            case CARDS_TO_BOTTOM -> choiceHandler.handleBottomCards(gameData);
            case ATTACKER_DECLARATION -> {
                PendingInteraction active = gameData.interaction.activeInteraction();
                if (active instanceof PendingInteraction.AttackerDeclaration
                        && isAuthorizedFor(gameData, active)) {
                    handleAttackers(gameData);
                }
            }
            case BLOCKER_DECLARATION -> {
                PendingInteraction active = gameData.interaction.activeInteraction();
                if (active instanceof PendingInteraction.BlockerDeclaration
                        && isAuthorizedFor(gameData, active)) {
                    handleBlockers(gameData);
                }
            }
            case INTERACTION -> handleInteractionPrompt(gameData);
            case COMBAT_DAMAGE_ASSIGNMENT -> choiceHandler.handleCombatDamageAssignment(gameData);
        }
    }

    // ===== Abstract Methods =====

    protected abstract void handleGameState(GameData gameData);

    protected abstract void handleAttackers(GameData gameData);

    protected abstract void handleBlockers(GameData gameData);

    /**
     * The player whose current interaction this AI is authorized to answer.
     *
     * <p>Normally this is the AI seat itself. During a controlled turn, canonical decision facts
     * are addressed to the controller while the interaction remains owned by the controlled
     * player, so combat evaluation must inspect that player's board and legal choices.
     */
    protected UUID activeDecisionPlayerId(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active != null
                && AiUtils.isRespondingFor(
                        gameData, aiPlayer.getId(), active.decidingPlayerId())) {
            return active.decidingPlayerId();
        }
        return aiPlayer.getId();
    }

    private boolean isAuthorizedFor(GameData gameData, PendingInteraction interaction) {
        return AiUtils.isRespondingFor(gameData, aiPlayer.getId(), interaction.decidingPlayerId());
    }

    // ===== Overridable Choice Handlers =====

    protected void handleCardChoice(GameData gameData) {
        choiceHandler.handleCardChoice(gameData);
    }

    protected void handleMayAbilityChoice(GameData gameData) {
        if (!floatManaForMayCost(gameData)) {
            // Accepting would fizzle on the payment — decline outright.
            log.info("AI: Declining may ability (cannot float its mana cost) in game {}", gameId);
            send(() -> gameActions.answerInteraction(
                    new InteractionAnswer.MayAbilityChosen(false)));
            return;
        }
        choiceHandler.handleMayAbilityChoice(gameData);
    }

    /**
     * Answers a pay-{X} amount prompt. For mana payments belonging to this AI, spare mana
     * is floated first (CR 605.3a opens the tap window during the prompt); the strategy
     * then pays what is actually floating. Non-mana number picks go straight to the
     * strategy default.
     */
    protected void handleXValueChoice(GameData gameData) {
        PendingInteraction.XValueChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        if (choice != null && choice.manaPayment() && aiPlayer.getId().equals(choice.playerId())) {
            int spare = spareManaForPayment(gameData);
            if (spare > 0) {
                manaManager.tapLandsForCost(gameData, aiPlayer.getId(), "{" + spare + "}", 0,
                        manaTapAction(), true);
            }
        }
        choiceHandler.handleActiveInteraction(gameData);
    }

    /**
     * Answers the X announcement of a cast for an alternative cost containing {X} (miracle).
     * The whole cost — coloured part included — is charged from the floating pool, so every
     * available source is tapped first (CR 605.3a) and the strategy then announces the X the
     * pool can cover.
     */
    protected void handleAlternateCastXValueChoice(GameData gameData) {
        PendingInteraction.AlternateCastXValueChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.AlternateCastXValueChoice.class);
        if (choice != null && aiPlayer.getId().equals(choice.playerId())) {
            ManaCost cost = new ManaCost(choice.manaCost());
            int maxX = cost.calculateMaxX(gameData.playerManaPools.get(aiPlayer.getId()));
            if (maxX < choice.maxValue()) {
                manaManager.tapLandsForCost(gameData, aiPlayer.getId(), choice.manaCost(),
                        choice.maxValue(), manaTapAction(), true);
            }
        }
        choiceHandler.handleActiveInteraction(gameData);
    }

    /**
     * Floats mana for the active may-pay prompt when the choice is this AI's and carries a
     * mana cost — the engine pays may-costs from the actual pool, so the mana must be
     * floating before answering yes (CR 605.3a opens the tap window during the prompt).
     * X costs are paid with everything floating, so only spare mana is floated for them.
     * Returns false when the cost could not be floated and accepting would fizzle.
     */
    protected boolean floatManaForMayCost(GameData gameData) {
        PendingInteraction.MayAbilityChoice mayChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        if (mayChoice == null || !aiPlayer.getId().equals(mayChoice.playerId())
                || mayChoice.manaCost() == null || mayChoice.manaCost().isEmpty()) {
            return true;
        }
        ManaCost cost = new ManaCost(mayChoice.manaCost());
        if (cost.hasX()) {
            int spare = spareManaForPayment(gameData);
            if (spare > 0) {
                manaManager.tapLandsForCost(gameData, aiPlayer.getId(), "{" + spare + "}", 0,
                        manaTapAction(), true);
            }
            return gameData.playerManaPools.get(aiPlayer.getId()).getTotal() > 0;
        }
        manaManager.tapLandsForCost(gameData, aiPlayer.getId(), mayChoice.manaCost(), 0,
                manaTapAction(), true);
        return cost.canPay(gameData.playerManaPools.get(aiPlayer.getId()));
    }

    /**
     * Largest mana payment the AI can make right now without denying a spell it could
     * otherwise cast this turn; 0 when every point of mana is spoken for.
     */
    protected int spareManaForPayment(GameData gameData) {
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        for (int amount = virtualPool.getTotal(); amount >= 1; amount--) {
            if (!manaPaymentDeniesACast(gameData, amount, virtualPool)) {
                return amount;
            }
        }
        return 0;
    }

    /**
     * True when reserving the given amount of mana for a payment would deny a spell the AI
     * can otherwise cast this turn — some castable card in hand would no longer be
     * affordable. Held instants count (they are castable off-turn), so counterspell and
     * combat-trick mana stays protected.
     */
    protected boolean manaPaymentDeniesACast(GameData gameData, int reservedManaValue, ManaPool virtualPool) {
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayer.getId(), List.of());
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) continue;
            if (isSpellCastable(gameData, card, virtualPool)
                    && !canAffordSpell(gameData, card, virtualPool, reservedManaValue)) {
                return true;
            }
        }
        // While a trigger resolves the stack is never empty, so sorcery-speed cards in
        // hand always fail the castable-now check above. On the AI's own turn with a
        // main phase still ongoing or ahead, reserve their mana by raw totals instead.
        if (aiPlayer.getId().equals(gameData.activePlayerId)
                && gameData.currentStep != null
                && gameData.currentStep.ordinal() <= TurnStep.POSTCOMBAT_MAIN.ordinal()) {
            int available = virtualPool.getTotal();
            for (Card card : hand) {
                if (card.hasType(CardType.LAND) || card.getManaCost() == null) continue;
                int manaValue = card.getManaValue();
                if (manaValue <= available && manaValue + reservedManaValue > available) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void handleScry(GameData gameData) {
        choiceHandler.handleScry(gameData);
    }

    protected void handleListChoice(GameData gameData) {
        choiceHandler.handleColorChoice(gameData);
    }

    /**
     * Routes the generic interaction prompt by the active interaction kind, preserving the
     * per-difficulty overridable routes (hand picks, color/list picks, may abilities, scry);
     * every other kind is answered by the strategy registry via
     * {@code AiChoiceHandler.handleActiveInteraction}.
     */
    protected void handleInteractionPrompt(GameData gameData) {
        switch (gameData.interaction.activeInteraction()) {
            case PendingInteraction.HandChoice ignored -> handleCardChoice(gameData);
            case PendingInteraction.ColorChoice ignored -> handleListChoice(gameData);
            case PendingInteraction.MayAbilityChoice ignored -> handleMayAbilityChoice(gameData);
            case PendingInteraction.XValueChoice ignored -> handleXValueChoice(gameData);
            case PendingInteraction.AlternateCastXValueChoice ignored ->
                    handleAlternateCastXValueChoice(gameData);
            case PendingInteraction.Scry ignored -> handleScry(gameData);
            case null -> { }
            default -> choiceHandler.handleActiveInteraction(gameData);
        }
    }

    // ===== Mulligan =====

    public void handleInitialMulligan() {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null) return;
        if (shouldKeepHand(gameData)) {
            log.info("AI: Keeping hand in game {}", gameId);
            send(() -> gameActions.handleKeepHand(new KeepHandRequest()));
        } else {
            log.info("AI: Taking mulligan in game {}", gameId);
            send(() -> gameActions.handleMulligan(new MulliganRequest()));
        }
    }

    protected boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) {
            return true;
        }

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        if (mulliganCount >= 3) {
            return true;
        }

        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        if (mulliganCount >= 2) {
            return landCount >= 1;
        }
        if (mulliganCount >= 1) {
            return landCount >= 1 && landCount <= 5;
        }

        return landCount >= 2 && landCount <= 5;
    }

    // ===== Land Playing =====

    protected boolean tryPlayLand(GameData gameData) {
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(aiPlayer.getId(), 0);
        if (landsPlayed >= gameData.getMaxLandsThisTurn(aiPlayer.getId())) {
            return false;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) {
                log.info("AI: Playing land {} in game {}", card.getName(), gameId);
                final int idx = i;
                send(() -> gameActions.handlePlayCard(
                        new PlayCardRequest(idx, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
                // Verify the land was actually played — handlePlayCard silently
                // swallows errors, so we must confirm the state actually changed.
                // Identity check: hand size alone is unreliable because landfall/ETB
                // triggers can add cards to hand, masking a successful play.
                if (hand.contains(card)) {
                    log.warn("AI: Land play failed silently in game {}", gameId);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    // ===== Utility =====

    protected boolean hasPriority(GameData gameData) {
        if (gameData.status != GameStatus.RUNNING) {
            return false;
        }
        synchronized (gameData) {
            if (gameData.interaction.isAwaitingInput()) {
                return false;
            }
            UUID priorityHolder = getPriorityPlayerId(gameData);
            return priorityHolder != null && priorityHolder.equals(aiPlayer.getId());
        }
    }

    protected UUID getPriorityPlayerId(GameData gameData) {
        if (gameData.activePlayerId == null) {
            return null;
        }
        if (!gameData.priorityPassedBy.contains(gameData.activePlayerId)) {
            return gameData.activePlayerId;
        }
        List<UUID> ids = new ArrayList<>(gameData.orderedPlayerIds);
        UUID nonActive = ids.get(0).equals(gameData.activePlayerId) ? ids.get(1) : ids.get(0);
        if (!gameData.priorityPassedBy.contains(nonActive)) {
            return nonActive;
        }
        return null;
    }

    /**
     * Merges must-attack indices into an attacker list, ensuring all creatures
     * with "attacks each combat if able" are included.
     */
    protected List<Integer> enforceMustAttack(List<Integer> attackerIndices, List<Integer> mustAttackIndices) {
        if (mustAttackIndices.isEmpty()) return attackerIndices;
        LinkedHashSet<Integer> merged = new LinkedHashSet<>(attackerIndices);
        merged.addAll(mustAttackIndices);
        return new ArrayList<>(merged);
    }

    /**
     * Ensures the attacker list is non-empty when an opponent's effect forces
     * the player to attack with at least one creature (e.g. Trove of Temptation).
     * If the list is empty and the player is forced, picks the first available attacker.
     */
    protected List<Integer> enforceMustAttackWithAtLeastOne(GameData gameData, List<Integer> attackerIndices,
                                                            List<Integer> availableIndices) {
        if (!attackerIndices.isEmpty() || availableIndices.isEmpty()) return attackerIndices;
        if (!combatAttackService.isOpponentForcedToAttack(
                gameData, activeDecisionPlayerId(gameData))) {
            return attackerIndices;
        }
        List<Integer> legalAvailableIndices = removeUnmetAttackRestrictions(gameData, availableIndices);
        if (legalAvailableIndices.isEmpty()) {
            return attackerIndices;
        }
        List<Integer> forced = new ArrayList<>(attackerIndices);
        forced.add(legalAvailableIndices.getFirst());
        return forced;
    }

    /**
     * Returns the maximum number of attackers the AI can afford given the current
     * attack taxes (e.g. Windborn Muse / Ghostly Prison / Norn's Annex). The AI
     * never treats its last life point as spendable, so it cannot choose an attack
     * declaration that immediately loses the game. Returns {@link Integer#MAX_VALUE}
     * if there is no attack tax.
     */
    protected int getMaxAffordableAttackers(GameData gameData) {
        UUID actingPlayerId = activeDecisionPlayerId(gameData);
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, actingPlayerId);
        List<ManaColor> phyrexianPayments = castingCostService.getPhyrexianAttackPaymentsPerCreature(
                gameData, actingPlayerId);
        if (taxPerCreature <= 0 && phyrexianPayments.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        // Use safe pool that excludes mana sources requiring a color choice
        // (e.g. Birds of Paradise) to avoid overwriting the ATTACKER_DECLARATION state.
        VirtualManaPool virtualPool = manaManager.buildSafeVirtualManaPool(gameData, actingPlayerId);
        int totalMana = virtualPool.getTotal();
        int lifePaymentUnits = gameQueryService.canPlayerLifeChange(gameData, actingPlayerId)
                ? Math.max(0, gameData.getLife(actingPlayerId) - 1) / 2
                : 0;

        int upperBound = Integer.MAX_VALUE;
        if (taxPerCreature > 0) {
            upperBound = totalMana / taxPerCreature;
        }
        if (!phyrexianPayments.isEmpty()) {
            long phyrexianUpperBound = ((long) totalMana + lifePaymentUnits) / phyrexianPayments.size();
            upperBound = (int) Math.min(upperBound, Math.min(Integer.MAX_VALUE, phyrexianUpperBound));
        }

        int low = 0;
        int high = upperBound;
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            if (canAffordAttackTax(candidate, (long) taxPerCreature * candidate, phyrexianPayments,
                    virtualPool, totalMana, lifePaymentUnits)) {
                low = candidate;
            } else {
                high = candidate - 1;
            }
        }
        return low;
    }

    private boolean canAffordAttackTax(int attackerCount, long genericRequired,
                                       List<ManaColor> phyrexianPayments, VirtualManaPool virtualPool,
                                       int totalMana, int lifePaymentUnits) {
        if (genericRequired > totalMana) {
            return false;
        }

        return phyrexianLifePaymentUnits(attackerCount, genericRequired,
                phyrexianPayments, virtualPool, totalMana) <= lifePaymentUnits;
    }

    private long phyrexianLifePaymentUnits(int attackerCount, long genericRequired,
                                            List<ManaColor> phyrexianPayments,
                                            VirtualManaPool virtualPool, int totalMana) {
        long phyrexianRequired = (long) phyrexianPayments.size() * attackerCount;
        if (phyrexianRequired == 0) {
            return 0;
        }

        long matchingColoredMana = 0;
        for (ManaColor color : ManaColor.values()) {
            long symbolsOfColor = phyrexianPayments.stream().filter(color::equals).count();
            if (symbolsOfColor == 0) {
                continue;
            }
            int available = virtualPool.get(color);
            matchingColoredMana += Math.min((long) available, symbolsOfColor * attackerCount);
        }

        long manaLeftAfterGenericTax = Math.max(0, totalMana - genericRequired);
        long phyrexianPaidWithMana = Math.min(matchingColoredMana, manaLeftAfterGenericTax);
        return phyrexianRequired - phyrexianPaidWithMana;
    }

    /**
     * Caps the attacker list to the maximum affordable given the attack tax,
     * then taps lands to pay the tax before the declaration is sent.
     * Uses only mana sources that won't trigger interactive choices (e.g. skips
     * Birds of Paradise) to avoid corrupting the ATTACKER_DECLARATION interaction state.
     *
     * <p>Also enforces CR 508.1c: if the final list is a single attacker with
     * "can't attack alone" (Jackal Familiar, etc.), drops it. Adding another
     * attacker isn't safe once tax has been paid, so we just clear the list —
     * AIs that want to keep Jackal attacking should pair it before calling this.
     */
    protected List<Integer> prepareAttackersForTax(GameData gameData, List<Integer> attackerIndices) {
        UUID actingPlayerId = activeDecisionPlayerId(gameData);
        attackerIndices = enforceCanOnlyAttackAlone(gameData, actingPlayerId, attackerIndices);
        attackerIndices = removeUnmetAttackRestrictions(gameData, attackerIndices);
        attackerIndices = capAttackersToCombatMaximum(gameData, attackerIndices);
        int taxPerCreature = castingCostService.getAttackPaymentPerCreature(gameData, actingPlayerId);
        List<ManaColor> phyrexianPayments = castingCostService.getPhyrexianAttackPaymentsPerCreature(
                gameData, actingPlayerId);
        if (attackerIndices.isEmpty()) {
            return dropLoneCantAttackAlone(gameData, attackerIndices);
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(actingPlayerId);
        boolean hasCreatureSpecificTax = battlefield != null && attackerIndices.stream()
                .filter(index -> index >= 0 && index < battlefield.size())
                .anyMatch(index -> gameQueryService.getCreatureAttackTax(gameData, battlefield.get(index)) > 0);
        if (taxPerCreature <= 0 && phyrexianPayments.isEmpty() && !hasCreatureSpecificTax) {
            return dropLoneCantAttackAlone(gameData, attackerIndices);
        }
        if (battlefield == null) {
            return List.of();
        }

        VirtualManaPool virtualPool = manaManager.buildSafeVirtualManaPool(gameData, actingPlayerId);
        int totalMana = virtualPool.getTotal();
        int lifePaymentUnits = gameQueryService.canPlayerLifeChange(gameData, actingPlayerId)
                ? Math.max(0, gameData.getLife(actingPlayerId) - 1) / 2
                : 0;
        List<Integer> capped = new ArrayList<>();
        long totalGenericTax = 0;
        for (int attackerIndex : attackerIndices) {
            if (attackerIndex < 0 || attackerIndex >= battlefield.size()) {
                continue;
            }
            long candidateGenericTax = totalGenericTax + taxPerCreature
                    + gameQueryService.getCreatureAttackTax(gameData, battlefield.get(attackerIndex));
            int candidateCount = capped.size() + 1;
            if (canAffordAttackTax(candidateCount, candidateGenericTax, phyrexianPayments,
                    virtualPool, totalMana, lifePaymentUnits)) {
                capped.add(attackerIndex);
                totalGenericTax = candidateGenericTax;
            }
        }
        capped = removeUnmetAttackRestrictions(gameData, capped);
        totalGenericTax = capped.stream()
                .mapToLong(index -> (long) taxPerCreature
                        + gameQueryService.getCreatureAttackTax(gameData, battlefield.get(index)))
                .sum();
        if (capped.isEmpty()) {
            return List.of();
        }
        if (!phyrexianPayments.isEmpty() && !isPhyrexianLifePaymentWorthwhile(
                gameData, capped, totalGenericTax, phyrexianPayments)) {
            return List.of();
        }
        // Tap lands to put enough mana in the pool to pay the tax.
        // skipChoiceSources=true avoids mana abilities like Birds of Paradise that
        // require a color choice, which would overwrite the ATTACKER_DECLARATION state.
        StringBuilder taxCost = new StringBuilder();
        if (totalGenericTax > 0) {
            taxCost.append('{').append(totalGenericTax).append('}');
        }
        for (int i = 0; i < capped.size(); i++) {
            for (ManaColor color : phyrexianPayments) {
                taxCost.append('{').append(color.getCode()).append('}');
            }
        }
        String taxCostStr = taxCost.toString();
        manaManager.tapLandsForCost(gameData, actingPlayerId, taxCostStr, 0, manaTapAction(), true);
        // A mana source used to pay the tax may have been one of the selected attackers
        // (e.g. Leaden Myr tapped for mana). Remove any attackers that are now tapped.
        capped = capped.stream()
                .filter(idx -> idx < battlefield.size() && !battlefield.get(idx).isTapped())
                .toList();
        return removeUnmetAttackRestrictions(gameData, capped);
    }

    /**
     * Removes creatures that can only attack alone when the AI selected a larger attacking group.
     * If every selected creature has that restriction, keeps one legal singleton declaration.
     */
    private List<Integer> enforceCanOnlyAttackAlone(GameData gameData, UUID actingPlayerId,
                                                    List<Integer> attackerIndices) {
        if (attackerIndices.size() <= 1) {
            return attackerIndices;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(actingPlayerId);
        if (battlefield == null) {
            return attackerIndices;
        }
        List<Integer> unrestricted = attackerIndices.stream()
                .filter(index -> index >= 0 && index < battlefield.size())
                .filter(index -> !combatAttackService.canOnlyAttackAlone(gameData, battlefield.get(index)))
                .toList();
        return unrestricted.isEmpty() ? List.of(attackerIndices.getFirst()) : unrestricted;
    }

    private boolean isPhyrexianLifePaymentWorthwhile(GameData gameData, List<Integer> attackerIndices,
                                                      long genericTax,
                                                      List<ManaColor> phyrexianPayments) {
        UUID actingPlayerId = activeDecisionPlayerId(gameData);
        VirtualManaPool virtualPool = manaManager.buildSafeVirtualManaPool(gameData, actingPlayerId);
        long lifeCost = 2L * phyrexianLifePaymentUnits(attackerIndices.size(), genericTax,
                phyrexianPayments, virtualPool, virtualPool.getTotal());
        if (lifeCost == 0) {
            return true;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(actingPlayerId);
        if (battlefield == null) {
            return false;
        }
        long totalPower = attackerIndices.stream()
                .filter(index -> index >= 0 && index < battlefield.size())
                .mapToLong(index -> Math.max(0, gameQueryService.getEffectivePower(gameData, battlefield.get(index))))
                .sum();
        UUID opponentId = gameQueryService.getOpponentId(gameData, actingPlayerId);
        boolean potentiallyLethal = totalPower >= gameData.getLife(opponentId);
        return potentiallyLethal || totalPower >= lifeCost;
    }

    /**
     * CR 508.1c: if the only remaining attacker has "can't attack alone",
     * return an empty list. Used as the final legality gate before declaration.
     */
    private List<Integer> dropLoneCantAttackAlone(GameData gameData, List<Integer> attackerIndices) {
        if (attackerIndices.size() != 1) {
            return attackerIndices;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(activeDecisionPlayerId(gameData));
        if (battlefield == null) {
            return attackerIndices;
        }
        Permanent sole = battlefield.get(attackerIndices.getFirst());
        return hasCantAttackOrBlockAlone(sole) ? List.of() : attackerIndices;
    }

    /**
     * Trims a blocker declaration to the blocks whose additional costs the defending player can
     * actually pay, and floats the mana those costs need.
     *
     * <p>CR 509.1d locks in the total cost of every declared block, CR 509.1e then gives the
     * defending player a window to activate mana abilities, and CR 509.1f requires the whole cost
     * to be paid at once — so the engine validates the declaration as a unit and rejects all of it
     * when the pool or life total falls short, costing the AI every block instead of the one it
     * couldn't pay for. Paying is never mandatory (CR 509.1c), so giving up the unaffordable
     * blocks is the legal way out: attackers are considered in order of damage prevented per mana
     * spent, so the biggest threats keep their blockers.
     */
    protected List<BlockerAssignment> prepareBlockersForTax(GameData gameData,
                                                            List<BlockerAssignment> assignments) {
        if (assignments.isEmpty()) {
            return assignments;
        }
        UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
        if (defenderBattlefield == null || attackerBattlefield == null
                || !indicesInRange(assignments, defenderBattlefield, attackerBattlefield)) {
            return assignments;
        }
        assignments = dropIncompleteAllDefendingCreatureBlocks(
                gameData, defenderBattlefield, attackerBattlefield, assignments);
        if (assignments.isEmpty()) {
            return assignments;
        }
        BlockTax tax = new BlockTax(gameData, defenderBattlefield, attackerBattlefield);
        if (tax.mana(assignments) == 0 && tax.life(assignments) == 0) {
            return assignments;
        }

        // Only the defending player can pay, so their own mana sources are tappable only when
        // this AI holds that seat (Melee hands the declaration to the attacking player instead).
        boolean canTapForTax = defenderId.equals(aiPlayer.getId());
        int manaBudget = canTapForTax
                ? manaManager.buildSafeVirtualManaPool(gameData, defenderId).getTotal()
                : gameData.playerManaPools.get(defenderId).getTotal();
        int lifeBudget = gameQueryService.canPlayerLifeChange(gameData, defenderId)
                ? gameData.getLife(defenderId)
                : 0;

        List<BlockerAssignment> affordable = tax.affordableBlocks(assignments, manaBudget, lifeBudget);
        int manaOwed = tax.mana(affordable);
        if (canTapForTax && manaOwed > 0) {
            manaManager.tapLandsForCost(gameData, defenderId, "{" + manaOwed + "}", 0,
                    manaTapAction(), true);
            // A mana source tapped for the cost may have been one of the blockers (Llanowar Elves),
            // and a source can still refuse to tap — re-fit against what actually happened.
            affordable = affordable.stream()
                    .filter(a -> !defenderBattlefield.get(a.blockerIndex()).isTapped())
                    .toList();
            affordable = tax.affordableBlocks(affordable,
                    gameData.playerManaPools.get(defenderId).getTotal(), lifeBudget);
        }
        if (affordable.size() != assignments.size()) {
            log.info("AI: Dropping {} of {} blocks in game {} whose block cost can't be paid",
                    assignments.size() - affordable.size(), assignments.size(), gameId);
        }
        List<BlockerAssignment> legal = dropBlocksLeftUnderfilled(
                gameData, defenderBattlefield, attackerBattlefield, affordable);
        return dropIncompleteAllDefendingCreatureBlocks(
                gameData, defenderBattlefield, attackerBattlefield, legal);
    }

    private boolean indicesInRange(List<BlockerAssignment> assignments, List<Permanent> defenderBattlefield,
                                   List<Permanent> attackerBattlefield) {
        return assignments.stream().allMatch(a ->
                a.blockerIndex() >= 0 && a.blockerIndex() < defenderBattlefield.size()
                        && a.attackerIndex() >= 0 && a.attackerIndex() < attackerBattlefield.size());
    }

    /**
     * Drops blocks left illegal after unaffordable ones were removed: an attacker whose remaining
     * blockers no longer meet its minimum (menace), and a lone blocker that can't block alone
     * (CR 509.1a — an unmet blocking restriction makes the whole declaration illegal).
     */
    private List<BlockerAssignment> dropBlocksLeftUnderfilled(GameData gameData, List<Permanent> defenderBattlefield,
                                                             List<Permanent> attackerBattlefield,
                                                             List<BlockerAssignment> assignments) {
        Map<Integer, Long> blockersPerAttacker = assignments.stream()
                .collect(Collectors.groupingBy(BlockerAssignment::attackerIndex, Collectors.counting()));
        List<BlockerAssignment> kept = assignments.stream()
                .filter(a -> blockersPerAttacker.get(a.attackerIndex()) >= AiUtils.minimumBlockersRequiredToBlock(
                        gameData, gameQueryService, attackerBattlefield.get(a.attackerIndex())))
                .toList();

        Set<Integer> uniqueBlockers = kept.stream()
                .map(BlockerAssignment::blockerIndex)
                .collect(Collectors.toSet());
        if (uniqueBlockers.size() == 1 && hasCantAttackOrBlockAlone(
                defenderBattlefield.get(uniqueBlockers.iterator().next()))) {
            return List.of();
        }
        return kept;
    }

    private List<BlockerAssignment> dropIncompleteAllDefendingCreatureBlocks(
            GameData gameData, List<Permanent> defenderBattlefield, List<Permanent> attackerBattlefield,
            List<BlockerAssignment> assignments) {
        if (assignments.isEmpty()) {
            return assignments;
        }
        BlockLegalityContext blockContext =
                blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
        Set<Integer> incompleteAttackers = assignments.stream()
                .map(BlockerAssignment::attackerIndex)
                .filter(attackerIdx -> isIndexInRange(attackerIdx, attackerBattlefield))
                .filter(attackerIdx -> blockLegalityService.requiresAllDefendingCreaturesToBlock(
                        blockContext, attackerBattlefield.get(attackerIdx)))
                .filter(attackerIdx -> !hasCompleteAllDefendingCreatureBlock(
                        gameData, blockContext, defenderBattlefield, attackerBattlefield, assignments, attackerIdx))
                .collect(Collectors.toSet());
        if (incompleteAttackers.isEmpty()) {
            return assignments;
        }
        return assignments.stream()
                .filter(assignment -> !incompleteAttackers.contains(assignment.attackerIndex()))
                .toList();
    }

    private boolean hasCompleteAllDefendingCreatureBlock(
            GameData gameData, BlockLegalityContext blockContext, List<Permanent> defenderBattlefield,
            List<Permanent> attackerBattlefield, List<BlockerAssignment> assignments, int attackerIdx) {
        Permanent attacker = attackerBattlefield.get(attackerIdx);
        List<Integer> defendingCreatureIndices = defendingCreatureIndices(gameData, defenderBattlefield);
        if (!canAllDefendingCreaturesBlock(
                gameData, blockContext, defenderBattlefield, attackerBattlefield, attacker,
                defendingCreatureIndices)) {
            return false;
        }
        Set<Integer> assignedBlockers = assignments.stream()
                .filter(assignment -> assignment.attackerIndex() == attackerIdx)
                .map(BlockerAssignment::blockerIndex)
                .collect(Collectors.toSet());
        long assignedBlockCount = assignments.stream()
                .filter(assignment -> assignment.attackerIndex() == attackerIdx)
                .count();
        return assignedBlockCount == defendingCreatureIndices.size()
                && assignedBlockers.equals(new HashSet<>(defendingCreatureIndices));
    }

    private boolean hasCantAttackOrBlockAlone(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance);
    }

    /**
     * The additional cost of a set of declared blocks, computed the way
     * {@code CombatBlockService.declareBlockers} computes it: pair-specific mana is summed per
     * blocker-attacker pair, while board-wide mana and life are charged once per blocker; life
     * uses the highest applicable rate (CR 509.1d — one locked-in total).
     */
    private final class BlockTax {

        private final GameData gameData;
        private final List<Permanent> defenderBattlefield;
        private final List<Permanent> attackerBattlefield;

        private BlockTax(GameData gameData, List<Permanent> defenderBattlefield,
                         List<Permanent> attackerBattlefield) {
            this.gameData = gameData;
            this.defenderBattlefield = defenderBattlefield;
            this.attackerBattlefield = attackerBattlefield;
        }

        private int mana(List<BlockerAssignment> assignments) {
            return mana(assignments, Map.of());
        }

        private int mana(List<BlockerAssignment> assignments, Map<UUID, Integer> alreadyCharged) {
            int pairTax = assignments.stream().mapToInt(this::pairMana).sum();
            Map<UUID, Integer> charged = globalManaByBlocker(assignments, alreadyCharged);
            int globalTax = charged.entrySet().stream()
                    .mapToInt(entry -> entry.getValue() - alreadyCharged.getOrDefault(entry.getKey(), 0))
                    .sum();
            return pairTax + globalTax;
        }

        private int pairMana(BlockerAssignment assignment) {
            return gameQueryService.getBlockManaTax(gameData, blocker(assignment), attacker(assignment));
        }

        private Map<UUID, Integer> globalManaByBlocker(List<BlockerAssignment> assignments,
                                                        Map<UUID, Integer> alreadyCharged) {
            Map<UUID, Integer> charged = new HashMap<>(alreadyCharged);
            for (BlockerAssignment assignment : assignments) {
                int manaTax = gameQueryService.getGlobalBlockManaTax(gameData, blocker(assignment));
                if (manaTax > 0) {
                    charged.merge(blocker(assignment).getId(), manaTax, Math::max);
                }
            }
            return charged;
        }

        private int life(List<BlockerAssignment> assignments) {
            return lifeByBlocker(assignments, new HashMap<>()).values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        private Map<UUID, Integer> lifeByBlocker(List<BlockerAssignment> assignments,
                                                 Map<UUID, Integer> alreadyCharged) {
            Map<UUID, Integer> charged = new HashMap<>(alreadyCharged);
            for (BlockerAssignment assignment : assignments) {
                int lifeTax = gameQueryService.getGlobalBlockLifeTax(
                        gameData, blocker(assignment), attacker(assignment));
                if (lifeTax > 0) {
                    charged.merge(blocker(assignment).getId(), lifeTax, Math::max);
                }
            }
            return charged;
        }

        /**
         * The largest subset of {@code assignments} the budgets cover, decided one attacker at a
         * time so no attacker is left blocked by fewer creatures than it may legally be blocked by.
         * Blocks preventing the most damage per mana are considered first, free ones before all of
         * them.
         */
        private List<BlockerAssignment> affordableBlocks(List<BlockerAssignment> assignments,
                                                         int manaBudget, int lifeBudget) {
            Map<Integer, List<BlockerAssignment>> byAttacker = assignments.stream()
                    .collect(Collectors.groupingBy(BlockerAssignment::attackerIndex,
                            LinkedHashMap::new, Collectors.toList()));
            List<List<BlockerAssignment>> ranked = new ArrayList<>(byAttacker.values());
            ranked.sort(Comparator.comparingDouble(group -> -damagePreventedPerMana(group)));

            Set<BlockerAssignment> kept = new LinkedHashSet<>();
            Map<UUID, Integer> lifeCharged = new HashMap<>();
            Map<UUID, Integer> manaCharged = new HashMap<>();
            int manaSpent = 0;
            for (List<BlockerAssignment> group : ranked) {
                List<BlockerAssignment> fitted = fitToBudget(
                        group, manaBudget - manaSpent, lifeBudget, lifeCharged, manaCharged);
                if (fitted.isEmpty()) {
                    continue;
                }
                manaSpent += mana(fitted, manaCharged);
                lifeCharged = lifeByBlocker(fitted, lifeCharged);
                manaCharged = globalManaByBlocker(fitted, manaCharged);
                kept.addAll(fitted);
            }
            return assignments.stream().filter(kept::contains).toList();
        }

        /**
         * The blockers of one attacker that still fit the remaining budgets, cheapest first.
         * Empty when what fits is fewer than the attacker may legally be blocked by (menace), since
         * an underfilled block is an illegal declaration rather than a cheaper one.
         */
        private List<BlockerAssignment> fitToBudget(List<BlockerAssignment> group, int manaLeft,
                                                    int lifeBudget, Map<UUID, Integer> lifeCharged,
                                                    Map<UUID, Integer> manaCharged) {
            Permanent groupAttacker = attacker(group.getFirst());
            if (requiresAllDefendingCreaturesToBlock(groupAttacker)) {
                int groupMana = mana(group, manaCharged);
                int groupLife = lifeByBlocker(group, lifeCharged).values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();
                return groupMana <= manaLeft && groupLife <= lifeBudget ? group : List.of();
            }

            List<BlockerAssignment> fitted = new ArrayList<>();
            Map<UUID, Integer> life = lifeCharged;
            Map<UUID, Integer> globalMana = new HashMap<>(manaCharged);
            int manaSpent = 0;
            for (BlockerAssignment assignment : group.stream()
                    .sorted(Comparator.comparingInt(this::pairMana))
                    .toList()) {
                int manaTax = mana(List.of(assignment), globalMana);
                Map<UUID, Integer> withAssignment = lifeByBlocker(List.of(assignment), life);
                int lifeSpent = withAssignment.values().stream().mapToInt(Integer::intValue).sum();
                if (manaSpent + manaTax > manaLeft || lifeSpent > lifeBudget) {
                    continue;
                }
                manaSpent += manaTax;
                globalMana = globalManaByBlocker(List.of(assignment), globalMana);
                life = withAssignment;
                fitted.add(assignment);
            }
            int minimumBlockers = AiUtils.minimumBlockersRequiredToBlock(
                    gameData, gameQueryService, groupAttacker);
            return fitted.size() >= minimumBlockers ? fitted : List.of();
        }

        private boolean requiresAllDefendingCreaturesToBlock(Permanent attacker) {
            BlockLegalityContext context =
                    blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
            return blockLegalityService.requiresAllDefendingCreaturesToBlock(context, attacker);
        }

        private double damagePreventedPerMana(List<BlockerAssignment> group) {
            int damagePrevented = gameQueryService.getEffectivePower(gameData, attacker(group.getFirst()));
            int manaTax = mana(group);
            return manaTax == 0 ? Double.MAX_VALUE : (double) damagePrevented / manaTax;
        }

        private Permanent blocker(BlockerAssignment assignment) {
            return defenderBattlefield.get(assignment.blockerIndex());
        }

        private Permanent attacker(BlockerAssignment assignment) {
            return attackerBattlefield.get(assignment.attackerIndex());
        }
    }

    /**
     * Declares attackers, recovering from an engine rejection so a legality disagreement can
     * never leave the declare-attackers step waiting forever.
     *
     * <p>The engine validates a declaration as a unit, so a rejection leaves the step still
     * awaiting one — and unlike a rejected cast, there is no later priority pass to try again
     * from. The two families of rejection want opposite fallbacks: restrictions (CR 508.1c —
     * "can't attack alone", "can't attack unless …") are never disobeyed by attacking with
     * nothing, and requirements (CR 508.1d — "attacks each combat if able") are all obeyed by
     * attacking with everything able. Requirements are only enforced when attacking is free
     * ({@code CombatAttackService.getMustAttackIndices} yields nothing under an attack tax), so
     * the all-in fallback never has a cost left to float.
     */
    protected void sendAttackerDeclaration(DeclareAttackersRequest request) {
        GameData gameData = gameRegistry.get(gameId);
        DeclareAttackersRequest requirementLegalRequest = gameData == null
                ? request
                : enforceConditionalAttackRequirements(gameData, request);
        DeclareAttackersRequest targetLegalRequest = gameData == null
                ? requirementLegalRequest
                : removeAttackersThatCannotAttackDefaultTarget(gameData, requirementLegalRequest);
        DeclareAttackersRequest restrictionLegalRequest = gameData == null
                ? targetLegalRequest
                : removeUnmetAttackRestrictions(gameData, targetLegalRequest);
        DeclareAttackersRequest combatLimitLegalRequest = gameData == null
                ? restrictionLegalRequest
                : capAttackersToCombatMaximum(gameData, restrictionLegalRequest);
        if (gameData != null) {
            combatLimitLegalRequest = removeUnmetAttackRestrictions(gameData, combatLimitLegalRequest);
            if (combatLimitLegalRequest.attackerIndices().isEmpty()
                    && combatAttackService.isOpponentForcedToAttack(
                            gameData, activeDecisionPlayerId(gameData))) {
                DeclareAttackersRequest forcedFallback = findLegalFallbackAttackerDeclaration(gameData);
                if (!forcedFallback.attackerIndices().isEmpty()) {
                    combatLimitLegalRequest = forcedFallback;
                }
            }
        }
        String rejection = attemptAttackerDeclaration(combatLimitLegalRequest);
        if (rejection == null) {
            return;
        }
        log.warn("AI: Attacker declaration rejected in game {}: {}; falling back to a legal declaration.",
                gameId, rejection);

        if (!request.attackerIndices().isEmpty()) {
            rejection = attemptAttackerDeclaration(new DeclareAttackersRequest(List.of(), null));
            if (rejection == null) {
                return;
            }
        }

        if (gameData == null) {
            return;
        }
        DeclareAttackersRequest fallback = findLegalFallbackAttackerDeclaration(gameData);
        if (!fallback.attackerIndices().isEmpty()) {
            rejection = attemptAttackerDeclaration(fallback);
            if (rejection == null) {
                return;
            }
        }
        log.error("AI: No legal attacker declaration found in game {}; last rejection: {}", gameId, rejection);
    }

    private DeclareAttackersRequest findLegalFallbackAttackerDeclaration(GameData gameData) {
        UUID attackingPlayerId = activeDecisionPlayerId(gameData);
        UUID defaultTarget = AiUtils.getOpponentId(gameData, attackingPlayerId);
        List<Integer> allAttackable = combatAttackService.getAttackableCreatureIndicesForTarget(
                gameData, attackingPlayerId, defaultTarget);
        if (allAttackable == null) {
            allAttackable = combatAttackService.getAttackableCreatureIndices(gameData, attackingPlayerId);
        }
        if (allAttackable == null || allAttackable.isEmpty()) {
            return new DeclareAttackersRequest(List.of(), null);
        }

        DeclareAttackersRequest fallback = new DeclareAttackersRequest(allAttackable, null);
        fallback = enforceConditionalAttackRequirements(gameData, fallback);
        fallback = removeAttackersThatCannotAttackDefaultTarget(gameData, fallback);
        fallback = removeUnmetAttackRestrictions(gameData, fallback);
        fallback = capAttackersToCombatMaximum(gameData, fallback);
        fallback = removeUnmetAttackRestrictions(gameData, fallback);
        return fallback;
    }

    /**
     * Adds conditional "also attacks if able" requirements for the candidate declaration before
     * the request reaches the engine. These requirements cannot be represented by the ordinary
     * must-attack list because whether they apply depends on the other attackers selected.
     */
    private DeclareAttackersRequest enforceConditionalAttackRequirements(
            GameData gameData, DeclareAttackersRequest request) {
        if (request.attackerIndices().isEmpty()) {
            return request;
        }

        UUID attackingPlayerId = activeDecisionPlayerId(gameData);
        List<Integer> attackableIndices = combatAttackService.getAttackableCreatureIndices(
                gameData, attackingPlayerId);
        List<Integer> requiredIndices = combatAttackService.getMustAttackAlongsideIndices(
                gameData, attackingPlayerId, attackableIndices, request.attackerIndices());
        if (requiredIndices.isEmpty()) {
            return request;
        }

        LinkedHashSet<Integer> mergedIndices = new LinkedHashSet<>(request.attackerIndices());
        mergedIndices.addAll(requiredIndices);
        return new DeclareAttackersRequest(new ArrayList<>(mergedIndices),
                request.attackTargets(), request.bands());
    }

    /**
     * The combat service exposes target-independent attackers for the declaration prompt, while
     * the AI's default request attacks the opponent. Remove creatures barred by a defender-scoped
     * restriction before sending that request so a legal attack is never rejected by the engine.
     */
    private DeclareAttackersRequest removeAttackersThatCannotAttackDefaultTarget(
            GameData gameData, DeclareAttackersRequest request) {
        if (request.attackerIndices().isEmpty()
                || (request.attackTargets() != null && !request.attackTargets().isEmpty())) {
            return request;
        }

        UUID attackingPlayerId = activeDecisionPlayerId(gameData);
        UUID defaultTarget = AiUtils.getOpponentId(gameData, attackingPlayerId);
        List<Integer> targetLegal = combatAttackService.getAttackableCreatureIndicesForTarget(
                gameData, attackingPlayerId, defaultTarget);
        if (targetLegal == null) {
            return request;
        }
        if (request.attackerIndices().stream().allMatch(targetLegal::contains)) {
            return request;
        }

        List<Integer> filtered = request.attackerIndices().stream()
                .filter(targetLegal::contains)
                .toList();
        return new DeclareAttackersRequest(filtered, request.attackTargets(), request.bands());
    }

    private List<Integer> removeUnmetAttackRestrictions(GameData gameData, List<Integer> attackerIndices) {
        if (attackerIndices.isEmpty()) {
            return attackerIndices;
        }

        UUID attackingPlayerId = activeDecisionPlayerId(gameData);
        List<Permanent> battlefield = gameData.playerBattlefields.get(attackingPlayerId);
        if (battlefield == null) {
            return attackerIndices;
        }

        List<Integer> legalAttackers = new ArrayList<>(attackerIndices);
        boolean changed;
        do {
            Set<Integer> invalid = new HashSet<>();
            for (int attackerIndex : legalAttackers) {
                if (attackerIndex < 0 || attackerIndex >= battlefield.size()
                        || hasUnmetAttackRestriction(gameData, battlefield, legalAttackers, attackerIndex)) {
                    invalid.add(attackerIndex);
                }
            }
            changed = legalAttackers.removeIf(invalid::contains);
        } while (changed);
        return legalAttackers;
    }

    private DeclareAttackersRequest removeUnmetAttackRestrictions(
            GameData gameData, DeclareAttackersRequest request) {
        List<Integer> legalAttackers = removeUnmetAttackRestrictions(gameData, request.attackerIndices());
        if (legalAttackers.equals(request.attackerIndices())) {
            return request;
        }
        return new DeclareAttackersRequest(legalAttackers, request.attackTargets(), request.bands());
    }

    /**
     * Applies battlefield-wide limits on the number of attackers before an AI declaration is
     * submitted. If the limit forces a choice, keep required attackers from the active combat
     * prompt ahead of optional attackers.
     */
    private List<Integer> capAttackersToCombatMaximum(GameData gameData, List<Integer> attackerIndices) {
        UUID defaultTarget = AiUtils.getOpponentId(gameData, activeDecisionPlayerId(gameData));
        return capAttackersToCombatMaximum(gameData, attackerIndices, Map.of(), defaultTarget);
    }

    private List<Integer> capAttackersToCombatMaximum(GameData gameData, List<Integer> attackerIndices,
                                                       Map<Integer, UUID> attackTargets, UUID defaultTarget) {
        if (attackerIndices.isEmpty()) {
            return attackerIndices;
        }

        Set<Integer> requested = new HashSet<>(attackerIndices);
        LinkedHashSet<Integer> ordered = new LinkedHashSet<>();
        PendingInteraction.AttackerDeclaration declaration =
                gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class);
        if (declaration != null) {
            for (int requiredIndex : declaration.mustAttackIndices()) {
                if (requested.contains(requiredIndex)) {
                    ordered.add(requiredIndex);
                }
            }
        }
        ordered.addAll(attackerIndices);

        List<Integer> capped = new ArrayList<>(attackerIndices.size());
        for (int attackerIndex : ordered) {
            List<Integer> candidate = new ArrayList<>(capped);
            candidate.add(attackerIndex);
            Map<Integer, UUID> candidateTargets = new HashMap<>();
            for (int index : candidate) {
                candidateTargets.put(index, attackTargets.getOrDefault(index, defaultTarget));
            }
            try {
                CombatHelper.validateMaximumAttackers(gameData, candidate, candidateTargets);
                capped.add(attackerIndex);
            } catch (IllegalStateException ignored) {
                // Keep the largest declaration found so far that satisfies every combat limit.
            }
        }
        return capped;
    }

    private DeclareAttackersRequest capAttackersToCombatMaximum(
            GameData gameData, DeclareAttackersRequest request) {
        UUID defaultTarget = AiUtils.getOpponentId(gameData, activeDecisionPlayerId(gameData));
        Map<Integer, UUID> attackTargets = new HashMap<>();
        if (request.attackTargets() != null) {
            request.attackTargets().forEach((index, targetId) ->
                    attackTargets.put(index, UUID.fromString(targetId)));
        }
        List<Integer> capped = capAttackersToCombatMaximum(
                gameData, request.attackerIndices(), attackTargets, defaultTarget);
        if (capped.equals(request.attackerIndices())) {
            return request;
        }
        return new DeclareAttackersRequest(capped, request.attackTargets(), request.bands());
    }

    private boolean hasUnmetAttackRestriction(GameData gameData, List<Permanent> battlefield,
                                              List<Integer> attackerIndices, int attackerIndex) {
        Permanent attacker = battlefield.get(attackerIndex);
        if (attackerIndices.size() == 1 && hasCantAttackOrBlockAlone(attacker)) {
            return true;
        }
        if (attackerIndices.size() > 1 && combatAttackService.canOnlyAttackAlone(gameData, attacker)) {
            return true;
        }

        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackOrBlockUnlessCountAlsoDoesEffect restriction
                    && attackerIndices.size() - 1 < restriction.otherCount()) {
                return true;
            }
            if (effect instanceof CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect) {
                int power = gameQueryService.getEffectivePower(gameData, attacker);
                boolean hasGreaterPowerAttacker = attackerIndices.stream()
                        .filter(other -> other != attackerIndex)
                        .map(battlefield::get)
                        .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > power);
                if (!hasGreaterPowerAttacker) {
                    return true;
                }
            }
            if (effect instanceof MatchingAttackerRestrictionEffect restriction) {
                boolean hasMatchingAttacker = attackerIndices.stream()
                        .filter(other -> other != attackerIndex)
                        .map(battlefield::get)
                        .anyMatch(other -> predicateEvaluationService.matchesPermanentPredicate(
                                gameData, other, restriction.matchingAttackerPredicate()));
                if (!hasMatchingAttacker) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns the rejection reason, or null when the declaration was accepted or the game is over. */
    private String attemptAttackerDeclaration(DeclareAttackersRequest request) {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return null;
        }
        try {
            return gameActions.handleDeclareAttackers(request);
        } catch (Exception e) {
            log.warn("AI: Attacker declaration threw in game {}: {}", gameId, e.getMessage(), e);
            return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        }
    }

    /**
     * Sends a blocker declaration with automatic fallback to a legal declaration if the original
     * declaration fails server-side validation. Blocks the defending player can't pay the
     * additional cost for are dropped, and their mana floated, before sending.
     */
    protected void sendBlockerDeclaration(DeclareBlockersRequest request) {
        GameData gameData = gameRegistry.get(gameId);
        DeclareBlockersRequest requirementLegal = gameData == null
                ? request
                : new DeclareBlockersRequest(enforceBlockRequirements(gameData, request.blockerAssignments()));
        if (gameData != null) {
            requirementLegal = new DeclareBlockersRequest(
                    capBlockersToLegalMaximum(gameData, requirementLegal.blockerAssignments()));
            requirementLegal = new DeclareBlockersRequest(
                    normalizeBlockerAssignments(gameData, requirementLegal.blockerAssignments()));
        }
        DeclareBlockersRequest affordable = gameData == null
                ? requirementLegal
                : new DeclareBlockersRequest(prepareBlockersForTax(gameData, requirementLegal.blockerAssignments()));
        if (gameData != null) {
            affordable = new DeclareBlockersRequest(
                    normalizeBlockerAssignments(gameData, affordable.blockerAssignments()));
        }

        String rejection;
        try {
            rejection = gameActions.handleDeclareBlockers(affordable);
        } catch (Exception e) {
            log.warn("AI: Blocker declaration threw in game {}: {}. Falling back to a legal declaration.",
                    gameId, e.getMessage(), e);
            sendBlockerFallback();
            return;
        }

        // The engine validates the whole declaration and rejects it as a unit, so a rejection
        // leaves the game still awaiting blockers — fall back to a repaired declaration.
        if (rejection != null && !affordable.blockerAssignments().isEmpty()) {
            log.warn("AI: Blocker declaration rejected in game {}: {}; falling back to a legal declaration.",
                    gameId, rejection);
            sendBlockerFallback();
        }
    }

    private void sendBlockerFallback() {
        GameData gameData = gameRegistry.get(gameId);
        List<BlockerAssignment> fallbackAssignments = gameData == null
                ? List.of()
                : enforceBlockRequirements(gameData, List.of());
        if (gameData != null) {
            fallbackAssignments = capBlockersToLegalMaximum(gameData, fallbackAssignments);
            fallbackAssignments = prepareBlockersForTax(gameData, fallbackAssignments);
            fallbackAssignments = normalizeBlockerAssignments(gameData, fallbackAssignments);
        }
        try {
            String rejection = gameActions.handleDeclareBlockers(new DeclareBlockersRequest(fallbackAssignments));
            if (rejection != null) {
                log.error("AI: Fallback blocker declaration rejected in game {}: {}", gameId, rejection);
            }
        } catch (Exception e) {
            log.error("AI: Fallback blocker declaration also failed in game {}", gameId, e);
        }
    }

    private List<BlockerAssignment> capBlockersToLegalMaximum(
            GameData gameData, List<BlockerAssignment> assignments) {
        if (assignments.isEmpty() || gameData.activePlayerId == null) {
            return assignments;
        }
        UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
        if (defenderBattlefield == null || attackerBattlefield == null
                || !indicesInRange(assignments, defenderBattlefield, attackerBattlefield)) {
            return assignments;
        }

        Map<Integer, List<BlockerAssignment>> assignmentsByAttacker = assignments.stream()
                .collect(Collectors.groupingBy(BlockerAssignment::attackerIndex,
                        LinkedHashMap::new, Collectors.toList()));
        Set<BlockerAssignment> kept = new LinkedHashSet<>();
        for (Map.Entry<Integer, List<BlockerAssignment>> entry : assignmentsByAttacker.entrySet()) {
            int attackerIdx = entry.getKey();
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            int maximumBlockers = maximumLegalBlockersForAttacker(gameData, attacker, attackerBattlefield);
            List<BlockerAssignment> group = entry.getValue();
            if (group.size() <= maximumBlockers) {
                kept.addAll(group);
                continue;
            }

            List<BlockerAssignment> lureBlocks = group.stream()
                    .filter(assignment -> gameQueryService.isRequiredToBlockByLure(
                            gameData, attacker, defenderBattlefield.get(assignment.blockerIndex())))
                    .toList();
            lureBlocks.stream().limit(maximumBlockers).forEach(kept::add);
            if (kept.stream().filter(group::contains).count() < maximumBlockers) {
                group.stream()
                        .filter(assignment -> !kept.contains(assignment))
                        .limit(maximumBlockers - kept.stream().filter(group::contains).count())
                        .forEach(kept::add);
            }
        }
        return assignments.stream().filter(kept::contains).toList();
    }

    private int maximumLegalBlockersForAttacker(
            GameData gameData, Permanent attacker, List<Permanent> attackerBattlefield) {
        return Math.min(
                Math.min(gameQueryService.getMaxBlockersAllowed(gameData, attacker),
                        maximumBlockersForTeam(attackerBattlefield)),
                CombatHelper.getMaximumBlockers(gameData));
    }

    /**
     * Removes blockers whose own "also blocks" restriction became unsatisfied after another
     * blocker was removed by a later legality or affordability pass. Repeats the dependent
     * cleanup because removing one blocker can invalidate another block or its minimum size.
     */
    private List<BlockerAssignment> normalizeBlockerAssignments(
            GameData gameData, List<BlockerAssignment> assignments) {
        if (assignments.isEmpty() || gameData.activePlayerId == null) {
            return assignments;
        }
        UUID defenderId = gameQueryService.getOpponentId(gameData, gameData.activePlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
        if (defenderBattlefield == null || attackerBattlefield == null
                || !indicesInRange(assignments, defenderBattlefield, attackerBattlefield)) {
            return assignments;
        }

        List<BlockerAssignment> normalized = new ArrayList<>(assignments);
        boolean changed;
        do {
            List<BlockerAssignment> previous = new ArrayList<>(normalized);
            Set<Integer> selectedBlockers = normalized.stream()
                    .map(BlockerAssignment::blockerIndex)
                    .collect(Collectors.toSet());
            Set<Integer> invalidRestrictedBlockers = selectedBlockers.stream()
                    .filter(blockerIdx -> hasUnmetBlockPartnerRequirement(
                            gameData, defenderBattlefield, selectedBlockers, blockerIdx))
                    .collect(Collectors.toSet());
            normalized.removeIf(assignment -> invalidRestrictedBlockers.contains(assignment.blockerIndex()));
            normalized = new ArrayList<>(dropBlocksLeftUnderfilled(
                    gameData, defenderBattlefield, attackerBattlefield, normalized));
            normalized = new ArrayList<>(dropIncompleteAllDefendingCreatureBlocks(
                    gameData, defenderBattlefield, attackerBattlefield, normalized));
            changed = !normalized.equals(previous);
        } while (changed);
        return normalized;
    }

    private boolean hasUnmetBlockPartnerRequirement(
            GameData gameData, List<Permanent> battlefield, Set<Integer> selectedBlockers, int blockerIdx) {
        Permanent blocker = battlefield.get(blockerIdx);
        for (CardEffect effect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect) {
                int blockerPower = gameQueryService.getEffectivePower(gameData, blocker);
                boolean hasGreaterPowerPartner = selectedBlockers.stream()
                        .filter(otherIdx -> otherIdx != blockerIdx)
                        .map(battlefield::get)
                        .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > blockerPower);
                if (!hasGreaterPowerPartner) {
                    return true;
                }
            }
            if (effect instanceof CantAttackOrBlockUnlessCountAlsoDoesEffect restriction
                    && selectedBlockers.size() - 1 < restriction.otherCount()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Repairs a declaration that omitted a blocker-side requirement. The combat strategies choose
     * among profitable blocks, but a declaration must also obey requirements attached to the
     * defending creature itself, such as a one-shot "blocks if able" effect or a targeted
     * "must block that creature" effect.
     *
     * <p>The repair is deliberately done at the shared send boundary so every AI strategy,
     * including random and simulation-backed strategies, gets the same legality safeguard. A
     * required blocker is assigned only to a pair the engine's block-legality service accepts,
     * and menace/minimum-blocker requirements are completed before the repaired declaration is
     * returned.
     */
    private List<BlockerAssignment> enforceBlockRequirements(
            GameData gameData, List<BlockerAssignment> assignments) {
        PendingInteraction.BlockerDeclaration pending =
                gameData.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        if (pending == null || gameData.activePlayerId == null) {
            return assignments;
        }

        UUID defenderId = pending.defenderId();
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(gameData.activePlayerId);
        if (defenderBattlefield == null || attackerBattlefield == null) {
            return assignments;
        }

        List<Integer> attackerIndices = pending.attackerIndices().isEmpty()
                ? attackingCreatureIndices(attackerBattlefield)
                : pending.attackerIndices();
        List<Integer> blockerIndices = pending.blockerIndices().isEmpty()
                ? availableBlockerIndices(gameData, defenderBattlefield)
                : pending.blockerIndices();
        if (attackerIndices.isEmpty() || blockerIndices.isEmpty()) {
            return assignments;
        }

        List<BlockerAssignment> repaired = new ArrayList<>(assignments);
        for (int blockerIdx : blockerIndices) {
            if (!isIndexInRange(blockerIdx, defenderBattlefield)) {
                continue;
            }
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            List<Integer> targetedRequirements = requiredAttackerIndices(
                    pending, blockerIdx, blocker, attackerIndices, attackerBattlefield, gameData, defenderBattlefield);
            if (!targetedRequirements.isEmpty()
                    && !hasAssignmentToAny(repaired, blockerIdx, targetedRequirements)) {
                addRequiredBlock(gameData, repaired, blockerIdx, targetedRequirements,
                        blockerIndices, defenderBattlefield, attackerBattlefield);
            }

            if (hasMustBlockIfAbleRequirement(gameData, blocker)
                    && !hasAssignmentForBlocker(repaired, blockerIdx)) {
                List<Integer> legalAttackers = attackerIndices.stream()
                        .filter(attackerIdx -> isIndexInRange(attackerIdx, attackerBattlefield))
                        .filter(attackerIdx -> canBlockPair(gameData, blocker,
                                attackerBattlefield.get(attackerIdx), defenderBattlefield))
                        .toList();
                addRequiredBlock(gameData, repaired, blockerIdx, legalAttackers,
                        blockerIndices, defenderBattlefield, attackerBattlefield);
            }
        }
        enforceLureRequirements(gameData, repaired, attackerIndices, blockerIndices,
                defenderBattlefield, attackerBattlefield);
        return repaired;
    }

    private void enforceLureRequirements(
            GameData gameData,
            List<BlockerAssignment> assignments,
            List<Integer> attackerIndices,
            List<Integer> blockerIndices,
            List<Permanent> defenderBattlefield,
            List<Permanent> attackerBattlefield) {
        for (int attackerIdx : attackerIndices) {
            if (!isIndexInRange(attackerIdx, attackerBattlefield)
                    || !attackerBattlefield.get(attackerIdx).isAttacking()) {
                continue;
            }
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            List<Integer> lureBlockers = blockerIndices.stream()
                    .filter(blockerIdx -> isIndexInRange(blockerIdx, defenderBattlefield))
                    .filter(blockerIdx -> canBlockPair(gameData, defenderBattlefield.get(blockerIdx), attacker,
                            defenderBattlefield))
                    .filter(blockerIdx -> gameQueryService.isRequiredToBlockByLure(
                            gameData, attacker, defenderBattlefield.get(blockerIdx)))
                    .toList();
            int target = Math.min(lureBlockers.size(),
                    maximumLegalBlockersForAttacker(gameData, attacker, attackerBattlefield));
            while (countLureBlocks(assignments, attackerIdx, lureBlockers) < target) {
                int blockerIdx = lureBlockers.stream()
                        .filter(candidate -> !hasAssignmentForBlocker(assignments, candidate))
                        .findFirst()
                        .orElse(-1);
                if (blockerIdx < 0) {
                    return;
                }
                int previousCount = countLureBlocks(assignments, attackerIdx, lureBlockers);
                addRequiredBlock(gameData, assignments, blockerIdx, List.of(attackerIdx), blockerIndices,
                        defenderBattlefield, attackerBattlefield);
                if (countLureBlocks(assignments, attackerIdx, lureBlockers) == previousCount) {
                    return;
                }
            }
        }
    }

    private int countLureBlocks(
            List<BlockerAssignment> assignments, int attackerIdx, List<Integer> lureBlockers) {
        return (int) assignments.stream()
                .filter(assignment -> assignment.attackerIndex() == attackerIdx
                        && lureBlockers.contains(assignment.blockerIndex()))
                .count();
    }

    private List<Integer> defendingCreatureIndices(GameData gameData, List<Permanent> defenderBattlefield) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < defenderBattlefield.size(); i++) {
            if (gameQueryService.isCreature(gameData, defenderBattlefield.get(i))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private boolean canAllDefendingCreaturesBlock(
            GameData gameData, BlockLegalityContext blockContext, List<Permanent> defenderBattlefield,
            List<Permanent> attackerBattlefield, Permanent attacker, List<Integer> defenderIndices) {
        if (defenderIndices.isEmpty()
                || !blockLegalityService.canBeBlockedByAllDefendingCreatures(blockContext, attacker)) {
            return false;
        }
        int maximumBlockers = Math.min(
                gameQueryService.getMaxBlockersAllowed(gameData, attacker),
                CombatHelper.getMaximumBlockers(gameData));
        maximumBlockers = Math.min(maximumBlockers, maximumBlockersForTeam(attackerBattlefield));
        if (defenderIndices.size() > maximumBlockers
                || defenderIndices.size() < AiUtils.minimumBlockersRequiredToBlock(
                        gameData, gameQueryService, attacker)) {
            return false;
        }
        return defenderIndices.stream().allMatch(blockerIdx -> {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            return blockLegalityService.canBlock(blockContext, blocker)
                    && blockLegalityService.canBlockAttacker(blockContext, blocker, attacker);
        });
    }

    private int maximumBlockersForTeam(List<Permanent> attackerBattlefield) {
        int maximumBlockers = Integer.MAX_VALUE;
        for (Permanent permanent : attackerBattlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect restriction) {
                    maximumBlockers = Math.min(maximumBlockers, restriction.maxBlockers());
                }
            }
        }
        return maximumBlockers;
    }

    private List<Integer> attackingCreatureIndices(List<Permanent> battlefield) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).isAttacking()) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> availableBlockerIndices(GameData gameData, List<Permanent> battlefield) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (blockLegalityService.canBlock(gameData, battlefield.get(i))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private List<Integer> requiredAttackerIndices(
            PendingInteraction.BlockerDeclaration pending,
            int blockerIdx,
            Permanent blocker,
            List<Integer> attackerIndices,
            List<Permanent> attackerBattlefield,
            GameData gameData,
            List<Permanent> defenderBattlefield) {
        Set<Integer> required = new LinkedHashSet<>();
        List<Integer> promptedRequirements = pending.mustBlockRequirements().getOrDefault(
                blockerIdx, List.of());
        required.addAll(promptedRequirements);
        for (UUID mustBlockId : blocker.getMustBlockIds()) {
            for (int attackerIdx : attackerIndices) {
                if (isIndexInRange(attackerIdx, attackerBattlefield)
                        && attackerBattlefield.get(attackerIdx).getId().equals(mustBlockId)
                        && canBlockPair(gameData, blocker, attackerBattlefield.get(attackerIdx), defenderBattlefield)) {
                    required.add(attackerIdx);
                }
            }
        }
        return required.stream()
                .filter(attackerIndices::contains)
                .filter(attackerIdx -> isIndexInRange(attackerIdx, attackerBattlefield))
                .filter(attackerIdx -> canBlockPair(
                        gameData, blocker, attackerBattlefield.get(attackerIdx), defenderBattlefield))
                .toList();
    }

    private boolean hasMustBlockIfAbleRequirement(GameData gameData, Permanent blocker) {
        if (blocker.isMustBlockThisTurnIfAble()) {
            return true;
        }
        if (blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(MustBlockEachCombatEffect.class::isInstance)
                || gameQueryService.hasAuraWithEffect(gameData, blocker, MustBlockEachCombatEffect.class)) {
            return true;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield.stream()
                    .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                    .anyMatch(GlobalMustBlockEachCombatEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }

    private void addRequiredBlock(
            GameData gameData,
            List<BlockerAssignment> assignments,
            int blockerIdx,
            List<Integer> candidateAttackerIndices,
            List<Integer> blockerIndices,
            List<Permanent> defenderBattlefield,
            List<Permanent> attackerBattlefield) {
        if (candidateAttackerIndices.isEmpty()) {
            return;
        }

        List<BlockerAssignment> original = new ArrayList<>(assignments);
        assignments.removeIf(assignment -> assignment.blockerIndex() == blockerIdx);
        for (int attackerIdx : candidateAttackerIndices) {
            if (!isIndexInRange(attackerIdx, attackerBattlefield)) {
                continue;
            }
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            if (!canBlockPair(gameData, blocker, attacker, defenderBattlefield)
                    || !hasBlockCapacity(gameData, assignments, attackerIdx, blockerIdx, attacker)) {
                continue;
            }

            assignments.add(new BlockerAssignment(blockerIdx, attackerIdx));
            if (completeMinimumBlockers(gameData, assignments, blockerIndices,
                    attackerIdx, defenderBattlefield, attackerBattlefield)) {
                return;
            }
            assignments.clear();
            assignments.addAll(original);
        }
        assignments.clear();
        assignments.addAll(original);
    }

    private boolean completeMinimumBlockers(
            GameData gameData,
            List<BlockerAssignment> assignments,
            List<Integer> blockerIndices,
            int attackerIdx,
            List<Permanent> defenderBattlefield,
            List<Permanent> attackerBattlefield) {
        Permanent attacker = attackerBattlefield.get(attackerIdx);
        int minimum = AiUtils.minimumBlockersRequiredToBlock(gameData, gameQueryService, attacker);
        while (countBlocksForAttacker(assignments, attackerIdx) < minimum) {
            int additionalBlockerIdx = blockerIndices.stream()
                    .filter(candidate -> isIndexInRange(candidate, defenderBattlefield))
                    .filter(candidate -> !hasAssignmentForBlocker(assignments, candidate))
                    .filter(candidate -> blockLegalityService.canBlock(gameData, defenderBattlefield.get(candidate)))
                    .filter(candidate -> canBlockPair(gameData, defenderBattlefield.get(candidate), attacker,
                            defenderBattlefield))
                    .filter(candidate -> hasBlockCapacity(gameData, assignments, attackerIdx, candidate, attacker))
                    .findFirst()
                    .orElse(-1);
            if (additionalBlockerIdx < 0) {
                return false;
            }
            assignments.add(new BlockerAssignment(additionalBlockerIdx, attackerIdx));
        }
        return true;
    }

    private boolean hasBlockCapacity(GameData gameData, List<BlockerAssignment> assignments,
                                     int attackerIdx, int blockerIdx, Permanent attacker) {
        int maxBlockers = gameQueryService.getMaxBlockersAllowed(gameData, attacker);
        if (maxBlockers <= 0) {
            return false;
        }
        long current = assignments.stream()
                .filter(assignment -> assignment.attackerIndex() == attackerIdx
                        && assignment.blockerIndex() != blockerIdx)
                .count();
        return current < maxBlockers;
    }

    private boolean canBlockPair(GameData gameData, Permanent blocker, Permanent attacker,
                                 List<Permanent> defenderBattlefield) {
        return blockLegalityService.canBlock(gameData, blocker)
                && blockLegalityService.canBlockAttacker(gameData, blocker, attacker, defenderBattlefield);
    }

    private boolean hasAssignmentForBlocker(List<BlockerAssignment> assignments, int blockerIdx) {
        return assignments.stream().anyMatch(assignment -> assignment.blockerIndex() == blockerIdx);
    }

    private boolean hasAssignmentToAny(List<BlockerAssignment> assignments, int blockerIdx,
                                       List<Integer> attackerIndices) {
        return assignments.stream().anyMatch(assignment -> assignment.blockerIndex() == blockerIdx
                && attackerIndices.contains(assignment.attackerIndex()));
    }

    private int countBlocksForAttacker(List<BlockerAssignment> assignments, int attackerIdx) {
        return (int) assignments.stream()
                .filter(assignment -> assignment.attackerIndex() == attackerIdx)
                .count();
    }

    private boolean isIndexInRange(int index, List<?> values) {
        return index >= 0 && index < values.size();
    }

    /**
     * Returns true if the card can be cast right now. Legality comes from the engine's own
     * playability check ({@code GameActionAvailabilityService.isCardPlayable}: timing, permissions,
     * spell limits, affordability with every cost modifier and alternative-cost route, target
     * availability, legendary-sorcery rule) evaluated against the AI's virtual pool, so the
     * AI can never disagree with the server. On top of that, the AI plans ahead for cast-time
     * additional costs the playable check defers (sacrifice and graveyard-exile costs) and
     * skips spells whose X or modal choice would be pointless.
     */
    protected boolean isSpellCastable(GameData gameData, Card card, ManaPool virtualPool) {
        if (!canAffordSpell(gameData, card, virtualPool)) {
            return false;
        }
        if (isUnsupportedAlternateHandOnlyRoute(gameData, card, virtualPool)) {
            return false;
        }
        if (isUnsupportedSharedColorDiscardRoute(gameData, card, virtualPool)) {
            return false;
        }
        // Non-mana additional costs (sacrifice / graveyard-exile) — the engine's single
        // satisfiability query, shared with the MCTS simulator so the two can never disagree.
        if (!castingCostService.canPayAdditionalSpellCosts(gameData, aiPlayer.getId(), card)) {
            return false;
        }
        // For X spells that exile creatures from graveyard, ensure at least 1 creature exists
        ManaCost cost = new ManaCost(card.getManaCost());
        if (cost.hasX() && getMaxXForGraveyardRequirements(gameData, card) <= 0) {
            return false;
        }
        // "Discard X cards" (Abandon Hope): X = 0 is legal but does nothing, so require a spare card.
        if (cost.hasX() && getMaxXForDiscardCost(gameData, card) <= 0) {
            return false;
        }
        if (!hasValidRequiredGraveyardReturnTargets(gameData, card)) {
            return false;
        }
        // For modal spells, ensure at least one mode has valid targets
        if (!hasValidModalMode(gameData, card)) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the card could be played with the given mana pool, using the engine's own
     * playability check (all cost modifiers and alternative-cost routes included).
     */
    protected boolean canAffordSpell(GameData gameData, Card card, ManaPool virtualPool) {
        return canAffordSpell(gameData, card, virtualPool, 0);
    }

    /**
     * Checks if the card could be played with the given mana pool and an extra generic cost
     * (such as targeting tax), using the engine's own playability check. AI policy on top of
     * engine legality: an X spell is only worth casting if X can be at least 1.
     */
    protected boolean canAffordSpell(GameData gameData, Card card, ManaPool virtualPool, int extraCost) {
        int minXPolicy = new ManaCost(card.getManaCost()).hasX() ? 1 : 0;
        return actionAvailabilityService.isCardPlayable(gameData, aiPlayer.getId(), card, virtualPool,
                extraCost + minXPolicy);
    }

    /**
     * Returns true when the engine found only an alternate hand cast that this AI cannot encode in
     * a regular play-card request. The AI supports alternate costs that select one card from hand;
     * unsupported routes are filtered out instead of being sent as ordinary mana casts.
     */
    private boolean isUnsupportedAlternateHandOnlyRoute(GameData gameData, Card card,
                                                         ManaPool virtualPool) {
        AlternateHandCast alternate = card.getCastingOption(AlternateHandCast.class).orElse(null);
        if (alternate == null
                || !castingCostService.canPayAlternateHandCast(gameData, aiPlayer.getId(), card)
                || castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, aiPlayer.getId(), card)
                || canPayPrintedManaCost(gameData, card, virtualPool, null, 0)) {
            return false;
        }
        return !isAlternateHandCastSupportedByAi(alternate);
    }

    /**
     * The engine playability query includes Dream Halls' shared-color discard alternative, but
     * the AI's regular spell request does not select the discarded card. Treat that route as
     * unavailable unless the printed cost or another AI-supported route is payable.
     */
    private boolean isUnsupportedSharedColorDiscardRoute(GameData gameData, Card card,
                                                          ManaPool virtualPool) {
        if (!castingCostService.canPaySharedColorDiscardAlternativeCostFromBattlefield(
                gameData, aiPlayer.getId(), card)
                || castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, aiPlayer.getId(), card)
                || canPayPrintedManaCost(gameData, card, virtualPool, null, 0)) {
            return false;
        }

        AlternateHandCast alternateHandCast = card.getCastingOption(AlternateHandCast.class).orElse(null);
        return alternateHandCast == null
                || !castingCostService.canPayAlternateHandCast(gameData, aiPlayer.getId(), card)
                || !isAlternateHandCastSupportedByAi(alternateHandCast);
    }

    /**
     * Checks the printed or selected modal mana cost against the AI's virtual pool. This is kept
     * separate from the engine playability query because that query also includes alternate costs.
     */
    private boolean canPayPrintedManaCost(GameData gameData, Card card, ManaPool virtualPool,
                                          Integer xValue, int targetingTax) {
        String manaCost = manaCostForSpell(card, xValue);
        if (manaCost == null) {
            return false;
        }
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, aiPlayer.getId(), card, new ManaCost(manaCost));
        int effectiveXValue = cost.hasX() ? (xValue != null ? xValue : 1) : 0;
        int costModifier = xValue == null
                ? castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                : castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card, xValue);
        costModifier += targetingTax;
        return canPayManaCostWithDelve(gameData, card, cost, virtualPool,
                cost.hasX() ? effectiveXValue : null, costModifier);
    }

    protected boolean hasDelveCost(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream().anyMatch(DelveCost.class::isInstance);
    }

    private boolean canPayManaCostWithDelve(GameData gameData, Card card, ManaCost cost,
                                            ManaPool pool, Integer xValue, int costModifier) {
        int effectiveXValue = cost.hasX() ? (xValue != null ? xValue : 0) : 0;
        int maximumDelveReduction = hasDelveCost(card)
                ? castingCostService.maximumDelveReduction(
                        gameData, aiPlayer.getId(), card, effectiveXValue, costModifier)
                : 0;
        for (int delveReduction = 0; delveReduction <= maximumDelveReduction; delveReduction++) {
            if (canPayManaCostWithDelveReduction(
                    card, cost, pool, effectiveXValue, costModifier, delveReduction)) {
                return true;
            }
        }
        return false;
    }

    private boolean canPayManaCostWithDelveReduction(Card card, ManaCost cost, ManaPool pool,
                                                      int xValue, int costModifier,
                                                      int delveReduction) {
        boolean canPay = cost.hasX()
                ? cost.canPayWithAdditionalGenericCost(pool, xValue, costModifier - delveReduction)
                : cost.canPay(pool, costModifier - delveReduction);
        return canPay && (!card.isRequiresCreatureMana()
                || cost.canPayCreatureOnly(pool, costModifier - delveReduction));
    }

    /**
     * Returns whether an alternate hand cost should replace the printed mana cost for this cast.
     * The current request format carries the selected hand card in its discard index field.
     */
    protected boolean shouldUseAlternateHandCast(GameData gameData, Card card, Integer xValue,
                                                 int targetingTax) {
        AlternateHandCast alternate = card.getCastingOption(AlternateHandCast.class).orElse(null);
        if (alternate == null
                || !isAlternateHandCastSupportedByAi(alternate)
                || castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, aiPlayer.getId(), card)
                || !castingCostService.canPayAlternateHandCast(gameData, aiPlayer.getId(), card)) {
            return false;
        }
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        return !canPayPrintedManaCost(gameData, card, virtualPool, xValue, targetingTax);
    }

    private boolean isAlternateHandCastSupportedByAi(AlternateHandCast alternate) {
        ExileCardsFromHandCastingCost exileCost = alternate.getCost(ExileCardsFromHandCastingCost.class).orElse(null);
        RevealCardsFromHandCastingCost revealCost = alternate.getCost(RevealCardsFromHandCastingCost.class).orElse(null);
        if (exileCost == null && revealCost == null) {
            return false;
        }
        if (exileCost != null && (exileCost.count() != 1 || exileCost.manaValueEqualsX())) {
            return false;
        }
        return alternate.getCost(SacrificePermanentsCost.class).isEmpty()
                && alternate.getCost(TapUntappedPermanentsCost.class).isEmpty()
                && alternate.getCost(ReturnPermanentsCost.class).isEmpty()
                && alternate.getCost(RemoveCountersFromControlledCreaturesCastingCost.class).isEmpty();
    }

    /**
     * Checks the selected target against target-dependent costs after the AI has chosen its
     * target. The general playability check can only establish that some legal target enables a
     * cost reduction, so this closes the gap before mana or life is paid for the actual target.
     */
    protected boolean canAffordSelectedSpellTarget(GameData gameData, Card card, ManaPool virtualPool,
                                                    UUID targetId, List<UUID> targetIds,
                                                    int targetingTax, Integer xValue) {
        List<UUID> costReductionTargetIds = targetIds != null && !targetIds.isEmpty()
                ? targetIds
                : targetId != null ? List.of(targetId) : List.of();
        long additionalLifeCost = (long) card.getAdditionalLifeCostPerTarget()
                * costReductionTargetIds.size();
        if (additionalLifeCost > gameData.getLife(aiPlayer.getId())) {
            return false;
        }

        String selectedModeManaCost = selectedModalManaCost(card, xValue);
        if (card.getManaCost() == null && selectedModeManaCost == null) {
            return true;
        }

        if (selectedModeManaCost != null) {
            if (castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, aiPlayer.getId(), card)) {
                return true;
            }
            int targetReduction = castingCostService.computeTargetBasedCostReduction(
                    gameData, aiPlayer.getId(), card, costReductionTargetIds);
            ManaCost validationCost = castingCostService.applyColoredManaCostReductions(
                    gameData, aiPlayer.getId(), card, new ManaCost(selectedModeManaCost));
            int costModifier = xValue == null
                    ? castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                    : castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card, xValue);
            costModifier += targetingTax - targetReduction;
            return canPayManaCostWithDelve(
                    gameData, card, validationCost, virtualPool, xValue, costModifier);
        }

        if (card.getManaCost() == null || !castingCostService.hasTargetBasedCastCostReduction(card)) {
            return true;
        }

        if (castingCostService.computeTargetBasedCostReduction(
                gameData, aiPlayer.getId(), card, costReductionTargetIds) > 0) {
            return true;
        }

        ManaCost validationCost = castingCostService.applyColoredManaCostReductions(
                gameData, aiPlayer.getId(), card, new ManaCost(card.getManaCost()));
        int costModifier = xValue == null
                ? castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                : castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card, xValue);
        return canPayManaCostWithDelve(
                gameData, card, validationCost, virtualPool, xValue, costModifier + targetingTax);
    }

    /**
     * Computes the targeting tax for a spell based on the chosen target(s).
     * Effects like Kopala, Warden of Waves increase the cost of spells that
     * target permanents with certain subtypes; Kaervek's Torch taxes spells that
     * target it while it is on the stack.
     */
    protected int computeTargetingTax(GameData gameData, UUID targetId, List<UUID> multiTargetIds) {
        return castingCostService.getTargetingSubtypeTax(gameData, aiPlayer.getId(), targetId, multiTargetIds)
                + castingCostService.getTargetingStackEntryTax(gameData, targetId, multiTargetIds);
    }

    /**
     * Chooses a hand card for a spell's discard additional cost or a supported alternate hand
     * casting cost. Legal by construction — indices come from the engine's own cost predicates.
     * Returns null when neither route is available.
     */
    protected Integer chooseDiscardCostIndex(GameData gameData, Card card, int spellCardIndex,
                                             Integer xValue, int targetingTax) {
        DiscardCardTypeCost fixedCost = findDiscardCardTypeCost(card);
        if (fixedCost != null && fixedCost.count() > 1) {
            return null;
        }
        List<Integer> valid = castingCostService.validDiscardCostIndices(gameData, aiPlayer.getId(), card);
        if (valid != null) {
            return valid.isEmpty() ? null : valid.get(0);
        }
        if (!shouldUseAlternateHandCast(gameData, card, xValue, targetingTax)) {
            return null;
        }
        return findAlternateHandCastCardIndex(gameData, card, spellCardIndex);
    }

    private Integer findAlternateHandCastCardIndex(GameData gameData, Card card, int spellCardIndex) {
        AlternateHandCast alternate = card.getCastingOption(AlternateHandCast.class).orElse(null);
        if (alternate == null) {
            return null;
        }
        ExileCardsFromHandCastingCost exileCost = alternate.getCost(ExileCardsFromHandCastingCost.class).orElse(null);
        RevealCardsFromHandCastingCost revealCost = alternate.getCost(RevealCardsFromHandCastingCost.class).orElse(null);
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayer.getId(), List.of());
        for (int i = 0; i < hand.size(); i++) {
            Card candidate = hand.get(i);
            if (i == spellCardIndex || candidate.getId().equals(card.getId())) {
                continue;
            }
            if (exileCost != null
                    && (exileCost.predicate() == null
                    || !predicateEvaluationService.matchesCardPredicate(candidate, exileCost.predicate(), candidate.getId()))) {
                continue;
            }
            if (revealCost != null
                    && (revealCost.predicate() == null
                    || !predicateEvaluationService.matchesCardPredicate(candidate, revealCost.predicate(), candidate.getId()))) {
                continue;
            }
            return i;
        }
        return null;
    }

    /**
     * Selects the object to exile for a single {@link BeholdAndExileCost}. Returns an empty
     * selection when the card has no such cost, or null when the cost cannot be paid.
     */
    protected BeholdSelection selectBeholdCost(GameData gameData, Card card) {
        BeholdAndExileCost cost = card.getEffects(EffectSlot.SPELL).stream()
                .filter(BeholdAndExileCost.class::isInstance)
                .map(BeholdAndExileCost.class::cast)
                .findFirst()
                .orElse(null);
        if (cost == null) {
            return new BeholdSelection(null, null);
        }

        PermanentPredicate permanentFilter = new PermanentHasSubtypePredicate(cost.subtype());
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of())) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, permanentFilter)) {
                return new BeholdSelection(permanent.getId(), null);
            }
        }

        CardSubtypePredicate cardFilter = new CardSubtypePredicate(cost.subtype());
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayer.getId(), List.of());
        for (int i = 0; i < hand.size(); i++) {
            Card candidate = hand.get(i);
            if (!candidate.getId().equals(card.getId())
                    && predicateEvaluationService.matchesCardPredicate(candidate, cardFilter, candidate.getId())) {
                return new BeholdSelection(null, i);
            }
        }
        return null;
    }

    /** The single-object selection fields carried by a regular spell cast request. */
    protected record BeholdSelection(UUID permanentId, Integer handCardIndex) {
    }

    /** The optional creatures and generic reduction selected for a spell's alternate cost. */
    protected record CostReductionPlan(List<UUID> permanentIds, int reduction) {
        protected CostReductionPlan {
            permanentIds = permanentIds == null ? List.of() : List.copyOf(permanentIds);
        }

        protected static CostReductionPlan none() {
            return new CostReductionPlan(List.of(), 0);
        }
    }

    /**
     * Builds the common spell cast request, including the selected object for any behold cost.
     * The other additional-cost fields mirror the request shape used by all AI spell paths.
     */
    protected PlayCardRequest buildSpellPlayCardRequest(
            Card card,
            int cardIndex,
            Integer xValue,
            UUID targetId,
            Map<UUID, Integer> damageAssignments,
            List<UUID> targetIds,
            List<UUID> convokeCreatureIds,
            List<UUID> alternateCostSacrificePermanentIds,
            UUID sacrificePermanentId,
            Integer exileGraveyardCardIndex,
            List<Integer> exileGraveyardCardIndices,
            Integer discardHandCardIndex,
            List<Integer> discardHandCardIndices,
            List<UUID> imposedSacrificePermanentIds,
            List<UUID> additionalCostSacrificePermanentIds,
            BeholdSelection beholdSelection) {
        BeholdSelection selection = beholdSelection != null
                ? beholdSelection
                : new BeholdSelection(null, null);
        Integer effectiveXValue = xValue;
        if (effectiveXValue == null && card.hasXScaledTargets()
                && card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(RepeatableAdditionalManaCost.class::isInstance)) {
            effectiveXValue = 1;
        }
        return new PlayCardRequest(
                cardIndex, effectiveXValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                null, sacrificePermanentId, null, null, alternateCostSacrificePermanentIds, null,
                exileGraveyardCardIndex,
                exileGraveyardCardIndices, null, null, null, discardHandCardIndex,
                discardHandCardIndices, imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                List.of(), null,
                null, selection.permanentId(), selection.handCardIndex(), null, null, null, null, null);
    }


    /**
     * Returns the maximum X value allowed by graveyard card requirements.
     * For cards with {@link ExileCreaturesFromGraveyardAndCreateTokensEffect},
     * X cannot exceed the number of creature cards in the caster's graveyard; for cards with
     * {@link ExileXCardsFromGraveyardCost}, it cannot exceed the number of matching cards; for cards with
     * {@link ReturnTargetCardsFromGraveyardToBattlefieldEffect} (Return to the Ranks), it cannot
     * exceed the number of graveyard cards matching that effect's filter. The same cap applies to
     * X-scaled return-to-hand effects. These mirror the engine's own cast-time validation in
     * {@code SpellCastingService}, so the AI never proposes an X the server refuses. Returns
     * {@link Integer#MAX_VALUE} if the card has no such requirement.
     */
    protected int getMaxXForGraveyardRequirements(GameData gameData, Card card) {
        List<CardEffect> spellEffects = card.getEffects(EffectSlot.SPELL);
        boolean needsGraveyardCreatures = spellEffects.stream()
                .anyMatch(ExileCreaturesFromGraveyardAndCreateTokensEffect.class::isInstance);
        ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect = spellEffects.stream()
                .filter(ReturnTargetCardsFromGraveyardToBattlefieldEffect.class::isInstance)
                .map(ReturnTargetCardsFromGraveyardToBattlefieldEffect.class::cast)
                .findFirst().orElse(null);
        ReturnTargetCardsFromGraveyardToHandEffect xScaledToHandEffect = spellEffects.stream()
                .filter(ReturnTargetCardsFromGraveyardToHandEffect.class::isInstance)
                .map(ReturnTargetCardsFromGraveyardToHandEffect.class::cast)
                .filter(ReturnTargetCardsFromGraveyardToHandEffect::xScaled)
                .findFirst().orElse(null);
        ExileXCardsFromGraveyardCost exileXCost = spellEffects.stream()
                .filter(ExileXCardsFromGraveyardCost.class::isInstance)
                .map(ExileXCardsFromGraveyardCost.class::cast)
                .findFirst().orElse(null);
        if (!needsGraveyardCreatures && returnEffect == null
                && xScaledToHandEffect == null && exileXCost == null) {
            return Integer.MAX_VALUE;
        }
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        int maxX = Integer.MAX_VALUE;
        if (exileXCost != null) {
            maxX = (int) graveyard.stream()
                    .filter(c -> exileXCost.requiredType() == null
                            || c.hasType(exileXCost.requiredType()))
                    .count();
        }
        if (needsGraveyardCreatures) {
            maxX = (int) graveyard.stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .count();
        }
        if (returnEffect != null) {
            maxX = Math.min(maxX, (int) graveyard.stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, returnEffect.filter(), card.getId()))
                    .count());
        }
        if (xScaledToHandEffect != null) {
            maxX = Math.min(maxX, (int) graveyard.stream()
                    .filter(c -> predicateEvaluationService.matchesCardPredicate(
                            c, xScaledToHandEffect.filter(), card.getId()))
                    .count());
        }
        return maxX;
    }

    private boolean hasValidRequiredGraveyardReturnTargets(GameData gameData, Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect
                    && !hasEnoughGraveyardReturnTargets(gameData, card, returnEffect)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasEnoughGraveyardReturnTargets(
            GameData gameData, Card card, ReturnTargetCardsFromGraveyardToHandEffect effect) {
        int validTargetCount = targetSelector.findValidGraveyardReturnTargets(
                gameData, card, aiPlayer.getId(), effect).size();
        return validTargetCount >= effect.minTargets()
                && (!effect.requireSharedCreatureType() || validTargetCount >= 2);
    }

    /**
     * Returns the maximum X allowed by a {@link DiscardXCardsCost} additional cast cost (Abandon
     * Hope): how many hand cards other than the spell itself can pay it, since a spell cannot be
     * discarded to pay for itself (CR 601.2a). A cost predicate ("discard X land cards" — Scorched
     * Earth) narrows the count further. Returns {@link Integer#MAX_VALUE} when the card has no such
     * cost.
     */
    protected int getMaxXForDiscardCost(GameData gameData, Card card) {
        DiscardXCardsCost cost = findDiscardXCardsCost(card);
        if (cost == null) {
            return Integer.MAX_VALUE;
        }
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayer.getId(), List.of());
        if (cost.predicate() == null) {
            return Math.max(0, hand.size() - 1);
        }
        // The spell is still in hand here, but it can never match a cost that excludes it; when it
        // does match, one copy must be reserved for the spell being cast.
        long matching = hand.stream()
                .filter(c -> predicateEvaluationService.matchesCardPredicate(c, cost.predicate(), c.getId()))
                .count();
        if (predicateEvaluationService.matchesCardPredicate(card, cost.predicate(), card.getId())) {
            matching--;
        }
        return (int) Math.max(0, matching);
    }

    private DiscardXCardsCost findDiscardXCardsCost(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(DiscardXCardsCost.class::isInstance)
                .map(DiscardXCardsCost.class::cast)
                .findFirst()
                .orElse(null);
    }

    private DiscardCardTypeCost findDiscardCardTypeCost(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(DiscardCardTypeCost.class::isInstance)
                .map(DiscardCardTypeCost.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Picks pre-removal hand indices for a fixed multi-card or X-based discard cost, skipping the
     * spell's own index. Returns null when the card has no list-valued discard cost, so the request
     * field stays empty for every other spell.
     */
    protected List<Integer> chooseDiscardCostIndices(GameData gameData, Card card, int cardIndex, int count) {
        DiscardCardTypeCost fixedCost = findDiscardCardTypeCost(card);
        if (fixedCost != null && fixedCost.count() > 1) {
            List<Integer> valid = castingCostService.validDiscardCostIndices(
                    gameData, aiPlayer.getId(), card);
            if (valid == null || valid.size() < fixedCost.count()) {
                return null;
            }
            return new ArrayList<>(valid.subList(0, fixedCost.count()));
        }

        DiscardXCardsCost cost = findDiscardXCardsCost(card);
        if (cost == null) {
            return null;
        }
        List<Card> hand = gameData.playerHands.getOrDefault(aiPlayer.getId(), List.of());
        List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < hand.size() && indices.size() < count; i++) {
            Card candidate = hand.get(i);
            if (i != cardIndex && (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(candidate, cost.predicate(), candidate.getId()))) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Returns true when the target's mana value must match X (e.g. Entrancing Melody). See
     * {@link AiUtils#hasManaValueEqualsXTarget}, which the MCTS simulator shares.
     */
    protected boolean hasPermanentManaValueEqualsXTarget(Card card) {
        return AiUtils.hasManaValueEqualsXTarget(card);
    }

    /**
     * Returns true when the target's mana value must be no greater than X (e.g. Dominate). See
     * {@link AiUtils#hasManaValueAtMostXTarget}, which the MCTS simulator shares.
     */
    protected boolean hasPermanentManaValueAtMostXTarget(Card card) {
        return AiUtils.hasManaValueAtMostXTarget(card);
    }

    /**
     * Selects a permanent to sacrifice for the card's sacrifice cost, if any.
     * Picks the weakest creature (lowest effective power + toughness) for creature
     * sacrifice costs, or the first matching permanent for other sacrifice types.
     * Returns null if the card has no sacrifice cost.
     */
    protected UUID selectSacrificeTarget(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (!(effect instanceof CostEffect cost)) {
                continue;
            }
            if (declinesOptionalCostForSingleModalMode(card, cost)) {
                continue;
            }
            // Multi-permanent costs ride on additionalCostSacrificePermanentIds — see
            // selectMultiPermanentCostIds.
            if (effect instanceof SacrificeMultiplePermanentsCost
                    || effect instanceof SacrificeAnyNumberOfPermanentsCost
                    || effect instanceof TapAnyNumberOfPermanentsCost
                    || effect instanceof TapMultiplePermanentsCost
                    || effect instanceof ReturnAnyNumberOfPermanentsToHandCost) {
                continue;
            }
            // "Sacrifice a creature" — pick the weakest (lowest effective power + toughness).
            if (cost.sacrificesChosenCreature()) {
                return battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .min(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)
                                + gameQueryService.getEffectiveToughness(gameData, p)))
                        .map(Permanent::getId)
                        .orElse(null);
            }
            // Artifact / filtered-permanent sacrifice — pick the first matching permanent.
            PermanentPredicate filter = cost.consumedPermanentFilter();
            if (filter != null) {
                return battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter))
                        .findFirst()
                        .map(Permanent::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    /**
     * Selects the permanents required by battlefield-imposed cast taxes such as Drought's
     * per-black-symbol Swamp sacrifice. The selection is made after mana payment by callers so
     * a permanent sacrificed while producing that mana cannot also pay this tax.
     *
     * @return the selected permanent ids, an empty list when no imposed tax applies, or null when
     *         the tax became unpayable after mana payment
     */
    protected List<UUID> selectImposedSacrificePermanentIds(
            GameData gameData, Card card, UUID singleConsumedPermanentId,
            List<UUID> otherConsumedPermanentIds) {
        CastingCostService.ImposedSacrificeRequirement requirement =
                castingCostService.getImposedSacrificeRequirementForSpell(gameData, card);
        if (requirement == null || requirement.isEmpty()) {
            return List.of();
        }

        Set<UUID> unavailableIds = new HashSet<>();
        if (singleConsumedPermanentId != null) {
            unavailableIds.add(singleConsumedPermanentId);
        }
        if (otherConsumedPermanentIds != null) {
            unavailableIds.addAll(otherConsumedPermanentIds);
        }

        List<UUID> selectedIds = gameData.playerBattlefields
                .getOrDefault(aiPlayer.getId(), List.of())
                .stream()
                .filter(permanent -> !unavailableIds.contains(permanent.getId()))
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, requirement.filter()))
                .limit(requirement.count())
                .map(Permanent::getId)
                .toList();
        return selectedIds.size() == requirement.count() ? selectedIds : null;
    }

    /**
     * Selects the permanents paying a card's multi-permanent additional cast cost — the ids all
     * ride on {@code PlayCardRequest.additionalCostSacrificePermanentIds}. A multi-permanent
     * sacrifice (Phyrexian Tribute's "sacrifice two creatures") gives up the weakest matching
     * permanents; a "tap any number of permanents you control" cost (Burn at the Stake) taps every
     * untapped matching permanent, and a "return any number" cost (Infernal Harvest) returns every
     * matching permanent, since the count feeds the payoff. Returns an empty list when the card has
     * no such cost or too few matching permanents.
     */
    protected List<UUID> selectMultiPermanentCostIds(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof SacrificeMultiplePermanentsCost cost) {
                List<UUID> chosen = battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                        .sorted(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)
                                + gameQueryService.getEffectiveToughness(gameData, p)))
                        .limit(cost.count())
                        .map(Permanent::getId)
                        .toList();
                return chosen.size() == cost.count() ? chosen : List.of();
            }
            if (effect instanceof SacrificeAnyNumberOfPermanentsCost cost) {
                return battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                        .map(Permanent::getId)
                        .toList();
            }
            if (effect instanceof TapAnyNumberOfPermanentsCost cost) {
                return battlefield.stream()
                        .filter(p -> !p.isTapped())
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                        .map(Permanent::getId)
                        .toList();
            }
            if (effect instanceof TapMultiplePermanentsCost cost && cost.count() instanceof Fixed fixed) {
                List<UUID> chosen = battlefield.stream()
                        .filter(p -> !p.isTapped())
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                        .limit(fixed.value())
                        .map(Permanent::getId)
                        .toList();
                return chosen.size() == fixed.value() ? chosen : List.of();
            }
            if (effect instanceof ReturnAnyNumberOfPermanentsToHandCost cost) {
                return battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                        .map(Permanent::getId)
                        .toList();
            }
        }
        return List.of();
    }

    /**
     * Returns whether an optional permanent cost must be omitted because the modal cast plan uses
     * only one mode and paying the cost would require every mode.
     */
    protected boolean declinesOptionalCostForSingleModalMode(Card card, CostEffect cost) {
        if (!(cost instanceof PutCounterOnControlledCreatureCost putCounterCost)
                || !putCounterCost.optional()) {
            return false;
        }
        ChooseOneEffect modal = findChooseOneEffect(card);
        return modal != null
                && modal.allModesWhenOptionalCostPaid()
                && modal.choicesRequired() < modal.choicesMax();
    }

    /**
     * Finds an ExileXCardsFromGraveyardCost in the card's SPELL effects, if any.
     */
    protected ExileXCardsFromGraveyardCost findExileXGraveyardCost(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileXCardsFromGraveyardCost cost) {
                return cost;
            }
        }
        return null;
    }

    /**
     * Finds an ExileNCardsFromGraveyardCost in the card's SPELL effects, if any.
     */
    protected ExileNCardsFromGraveyardCost findExileNGraveyardCost(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileNCardsFromGraveyardCost cost) {
                return cost;
            }
        }
        return null;
    }

    /**
     * Returns indices for cards in the player's graveyard that satisfy an
     * {@link ExileXCardsFromGraveyardCost}. Returns an empty list when no card can pay the cost.
     */
    protected List<Integer> selectExileXGraveyardIndices(
            GameData gameData, ExileXCardsFromGraveyardCost cost) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            if (cost.requiredType() == null || card.hasType(cost.requiredType())) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Selects exactly the requested number of cards matching an X-card graveyard exile cost.
     * Returns null when the graveyard does not contain enough matching cards.
     */
    protected List<Integer> selectExileXGraveyardIndices(
            GameData gameData, ExileXCardsFromGraveyardCost cost, int count) {
        List<Integer> indices = selectExileXGraveyardIndices(gameData, cost);
        if (count < 0 || indices.size() < count) {
            return null;
        }
        return new ArrayList<>(indices.subList(0, count));
    }

    /**
     * Selects exactly N graveyard card indices matching the required type for
     * {@link ExileNCardsFromGraveyardCost} (e.g. Skaab Ruinator's "exile 3 creature cards").
     * Returns null if the graveyard doesn't have enough matching cards.
     */
    protected List<Integer> selectNGraveyardIndicesToExile(GameData gameData, ExileNCardsFromGraveyardCost cost) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());
        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            Card c = graveyard.get(i);
            if ((cost.requiredType() == null || c.hasType(cost.requiredType()))
                    && (cost.predicate() == null
                    || predicateEvaluationService.matchesCardPredicate(c, cost.predicate(), null))) {
                matchingIndices.add(i);
            }
        }
        if (matchingIndices.size() < cost.count()) {
            return null;
        }
        return new ArrayList<>(matchingIndices.subList(0, cost.count()));
    }

    /** Selects the smallest number of graveyard cards needed to pay a spell's generic cost with Delve. */
    protected List<Integer> selectDelveGraveyardIndices(
            GameData gameData, Card card, Integer xValue, int targetingTax) {
        if (!hasDelveCost(card)) {
            return List.of();
        }
        String manaCost = manaCostForSpell(card, xValue);
        if (manaCost == null) {
            return null;
        }

        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, aiPlayer.getId(), card, new ManaCost(manaCost));
        int effectiveXValue = cost.hasX() ? (xValue != null ? xValue : 0) : 0;
        int costModifier = xValue == null
                ? castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                : castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card, xValue);
        costModifier += targetingTax;
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        int maximumDelveReduction = castingCostService.maximumDelveReduction(
                gameData, aiPlayer.getId(), card, effectiveXValue, costModifier);

        for (int delveReduction = 0; delveReduction <= maximumDelveReduction; delveReduction++) {
            if (canPayManaCostWithDelveReduction(
                    card, cost, virtualPool, effectiveXValue, costModifier, delveReduction)) {
                List<Integer> selectedIndices = new ArrayList<>(delveReduction);
                for (int i = 0; i < delveReduction; i++) {
                    selectedIndices.add(i);
                }
                return selectedIndices;
            }
        }
        return null;
    }

    // ===== Modal Spell Handling (ChooseOneEffect) =====

    /**
     * Internal record for modal spell casting: holds the selected mode encoding
     * (used as xValue in PlayCardRequest), the legacy single target, and any
     * target-slot targets declared by multi-mode or variable-count modes.
     */
    protected record ModalCastPlan(int modeIndex, UUID targetId, List<UUID> targetIds) {
        protected ModalCastPlan {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
        }

        protected ModalCastPlan(int modeIndex, UUID targetId) {
            this(modeIndex, targetId, List.of());
        }
    }

    /**
     * Finds the ChooseOneEffect in the card's SPELL effects, if any.
     */
    protected ChooseOneEffect findChooseOneEffect(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ChooseOneEffect coe) {
                return coe;
            }
        }
        return null;
    }

    /** Returns a selected modal option's own total cost, or null for ordinary modals. */
    protected String selectedModalManaCost(Card card, Integer modeEncoding) {
        if (modeEncoding == null) {
            return null;
        }
        ChooseOneEffect coe = findChooseOneEffect(card);
        if (coe == null) {
            return null;
        }
        List<Integer> selectedModes = coe.decodeModeIndices(modeEncoding);
        if (selectedModes.size() != 1) {
            return null;
        }
        return coe.options().get(selectedModes.getFirst()).manaCost();
    }

    /** Returns the mana cost that the selected modal mode will actually use, when applicable. */
    protected String manaCostForSpell(Card card, Integer modeEncoding) {
        String selectedModeManaCost = selectedModalManaCost(card, modeEncoding);
        return selectedModeManaCost != null ? selectedModeManaCost : card.getManaCost();
    }

    private boolean isModalModeAffordable(GameData gameData, Card card,
                                          ChooseOneEffect.ChooseOneOption option,
                                          ManaPool virtualPool) {
        if (option.manaCost() == null
                || castingCostService.hasAlternativeZeroCostFromBattlefield(gameData, aiPlayer.getId(), card)) {
            return true;
        }
        ManaCost cost = castingCostService.applyColoredManaCostReductions(
                gameData, aiPlayer.getId(), card, new ManaCost(option.manaCost()));
        int costModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card);
        return cost.canPay(virtualPool, costModifier)
                && (!card.isRequiresCreatureMana() || cost.canPayCreatureOnly(virtualPool, costModifier));
    }

    /**
     * Returns true if the card is non-modal, or if at least one modal mode
     * has valid targets available (excluding spell-targeting modes the AI can't handle).
     */
    protected boolean hasValidModalMode(GameData gameData, Card card) {
        ChooseOneEffect coe = findChooseOneEffect(card);
        if (coe == null) return true;

        for (ChooseOneEffect.ChooseOneOption option : coe.options()) {
            if (isModalModeValid(gameData, card, option)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For a ChooseOneEffect card, selects the first valid non-spell mode and
     * finds a target if needed. Returns null if the card is not modal or
     * if no valid mode exists.
     */
    protected ModalCastPlan prepareModalSpellCast(GameData gameData, Card card) {
        ChooseOneEffect coe = findChooseOneEffect(card);
        if (coe == null) return null;
        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Fixed choose-N (e.g. choose two): pick the first N valid modes.
        if (coe.choicesRequired() > 1 && coe.choicesRequired() == coe.choicesMax()) {
            List<Integer> validModes = new ArrayList<>();
            List<UUID> targetIds = new ArrayList<>();
            UUID targetId = null;
            for (int i = 0; i < coe.options().size(); i++) {
                ChooseOneEffect.ChooseOneOption option = coe.options().get(i);
                if (isModalModeValid(gameData, card, option)) {
                    validModes.add(i);
                    List<UUID> modeTargets = findModalModeTargets(gameData, card, option);
                    if (!modeTargets.isEmpty()) {
                        if (option.targetFilter() != null || option.targetFilters() != null) {
                            targetIds.addAll(modeTargets);
                        } else {
                            targetId = modeTargets.getFirst();
                        }
                    }
                    if (validModes.size() == coe.choicesRequired()) {
                        int[] modeIndices = validModes.stream().mapToInt(Integer::intValue).toArray();
                        return new ModalCastPlan(
                                ChooseOneEffect.encodeModeSelection(coe.choicesRequired(), modeIndices),
                                targetId, targetIds);
                    }
                }
            }
            return null;
        }

        // Choose-one and "choose one or more": pick the first valid single mode (avoids escalate).
        for (int i = 0; i < coe.options().size(); i++) {
            ChooseOneEffect.ChooseOneOption option = coe.options().get(i);
            if (!isModalModeValid(gameData, card, option)
                    || !isModalModeAffordable(gameData, card, option, virtualPool)) {
                continue;
            }
            CardEffect effect = option.effect();
            int encoded = coe.variableModeCount()
                    ? ChooseOneEffect.encodeModeSelection(coe.choicesRequired(), coe.choicesMax(), new int[]{i})
                    : i;

            if (EffectResolution.targetsSpellOnStack(effect)) continue;

            if (modeAdmitsTarget(option, TargetPredicate.Kind.PERMANENT)
                    || modeAdmitsTarget(option, TargetPredicate.Kind.PLAYER)) {
                List<UUID> targets = findModalModeTargets(gameData, card, option);
                if (targets.size() < requiredModalTargetCount(option)) {
                    continue;
                }
                if (usesModalTargetSlots(coe, option)) {
                    return new ModalCastPlan(encoded, null, targets);
                }
                if (!targets.isEmpty()) {
                    return new ModalCastPlan(encoded, targets.getFirst());
                }
                continue;
            }

            if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                List<Card> targets = targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId());
                if (!targets.isEmpty()) {
                    UUID target = targets.getFirst().getId();
                    return coe.variableModeCount()
                            ? new ModalCastPlan(encoded, null, List.of(target))
                            : new ModalCastPlan(encoded, target);
                }
                continue;
            }

            // No targeting required — mode is always valid
            return new ModalCastPlan(encoded, null);
        }
        return null;
    }

    private List<UUID> findModalModeTargets(GameData gameData, Card card,
                                             ChooseOneEffect.ChooseOneOption option) {
        CardEffect effect = option.effect();
        if (option.targetFilters() != null
                && effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            return findModalPermanentTargets(gameData, card, option);
        }
        if (modeAdmitsTarget(option, TargetPredicate.Kind.PERMANENT)
                || modeAdmitsTarget(option, TargetPredicate.Kind.PLAYER)) {
            return findModalPlayerOrPermanentTargets(gameData, card, option);
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            List<Card> targets = targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId());
            return targets.isEmpty() ? List.of() : List.of(targets.getFirst().getId());
        }
        return List.of();
    }

    private boolean isModalModeValid(GameData gameData, Card card, ChooseOneEffect.ChooseOneOption option) {
        for (CardEffect modeEffect : option.effects()) {
            if (modeEffect instanceof ReturnTargetCardsFromGraveyardToHandEffect returnEffect
                    && !hasEnoughGraveyardReturnTargets(gameData, card, returnEffect)) {
                return false;
            }
        }
        CardEffect effect = option.effect();
        if (EffectResolution.targetsSpellOnStack(effect)) return false;
        if (modeAdmitsTarget(option, TargetPredicate.Kind.PERMANENT)
                || modeAdmitsTarget(option, TargetPredicate.Kind.PLAYER)) {
            return findModalModeTargets(gameData, card, option).size() >= requiredModalTargetCount(option);
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            return !targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId()).isEmpty();
        }
        return true;
    }

    private boolean modeAdmitsTarget(ChooseOneEffect.ChooseOneOption option, TargetPredicate.Kind kind) {
        return option.effects().stream().anyMatch(effect -> effect.targetSpec().admits(kind));
    }

    private int requiredModalTargetCount(ChooseOneEffect.ChooseOneOption option) {
        return option.targetFilters() != null ? option.targetFilters().size() : option.minTargets();
    }

    private boolean usesModalTargetSlots(ChooseOneEffect coe, ChooseOneEffect.ChooseOneOption option) {
        return coe.variableModeCount() || coe.choicesRequired() > 1
                || option.targetFilters() != null
                || option.minTargets() != 1 || option.maxTargets() != 1;
    }

    private List<UUID> findModalPermanentTargets(GameData gameData, Card card,
                                                 ChooseOneEffect.ChooseOneOption option) {
        if (option.targetFilters() != null) {
            List<UUID> targets = new ArrayList<>();
            for (TargetFilter filter : option.targetFilters()) {
                UUID target = findModalPermanentTarget(gameData, card, filter, targets);
                if (target == null) {
                    return targets;
                }
                targets.add(target);
            }
            return targets;
        }

        int targetLimit = option.xScaledTargets()
                ? Math.max(1, option.minTargets())
                : option.maxTargets();
        return findModalPermanentTargets(gameData, card, option.targetFilter(), targetLimit, List.of());
    }

    private List<UUID> findModalPermanentTargets(GameData gameData, Card card, TargetFilter filter,
                                                 int targetLimit, List<UUID> alreadyChosen) {
        if (targetLimit <= 0) {
            return List.of();
        }

        List<UUID> targets = new ArrayList<>();
        // Evaluate the mode's targeting on an unfrozen runtime copy — the real card is frozen
        // (live cards are shared with simulation copies and must not be mutated, not even
        // temporarily with a restore).
        Card evalCard = card.createRuntimeCopy();
        evalCard.setCastTimeTargetFilter(filter);
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        for (UUID playerId : new UUID[]{opponentId, aiPlayer.getId()}) {
            if (playerId == null) continue;
            for (Permanent p : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (!card.isAllowSharedTargets() && alreadyChosen.contains(p.getId())) {
                    continue;
                }
                if (targetSelector.isValidPermanentTarget(gameData, evalCard, p, aiPlayer.getId())) {
                    targets.add(p.getId());
                    if (targets.size() == targetLimit) {
                        return targets;
                    }
                }
            }
        }
        return targets;
    }

    private UUID findModalPermanentTarget(GameData gameData, Card card, TargetFilter filter,
                                          List<UUID> alreadyChosen) {
        List<UUID> targets = findModalPermanentTargets(gameData, card, filter, 1, alreadyChosen);
        return targets.isEmpty() ? null : targets.getFirst();
    }

    private List<UUID> findModalPlayerOrPermanentTargets(
            GameData gameData, Card card, ChooseOneEffect.ChooseOneOption option) {
        int targetLimit = option.xScaledTargets()
                ? Math.max(1, option.minTargets())
                : option.maxTargets();
        if (targetLimit <= 0) {
            return List.of();
        }

        Card evalCard = card.createRuntimeCopy();
        evalCard.setCastTimeTargetFilter(option.targetFilter());
        List<UUID> targets = new ArrayList<>();
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        for (UUID playerId : new UUID[]{opponentId, aiPlayer.getId()}) {
            if (playerId == null || !modeAdmitsTarget(option, TargetPredicate.Kind.PLAYER)) {
                continue;
            }
            if (targetSelector.isValidModalTarget(gameData, evalCard, option.effects(), playerId, aiPlayer.getId())) {
                targets.add(playerId);
                if (targets.size() == targetLimit) {
                    return targets;
                }
            }
        }

        if (modeAdmitsTarget(option, TargetPredicate.Kind.PERMANENT)) {
            for (UUID playerId : new UUID[]{opponentId, aiPlayer.getId()}) {
                if (playerId == null) continue;
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (targetSelector.isValidModalTarget(
                            gameData, evalCard, option.effects(), permanent.getId(), aiPlayer.getId())) {
                        targets.add(permanent.getId());
                        if (targets.size() == targetLimit) {
                            return targets;
                        }
                    }
                }
            }
        }
        return targets;
    }

    /**
     * Taps lands (and creature-mana producers if needed) to pay for the given spell
     * before sending a PlayCardRequest. Must be called before handlePlayCard so the
     * actual mana pool satisfies the playability check in SpellCastingService.
     *
     * @return true if the payment opened input or put a triggered ability on the stack,
     *         meaning the caller should abort the spell cast and let the new decision resolve first.
     */
    protected boolean tapManaForSpell(GameData gameData, Card card, Integer xValue) {
        return tapManaForSpell(gameData, card, xValue, 0);
    }

    // ===== Activated-Ability Legality =====

    /**
     * Returns the activated abilities currently available on a permanent, in the engine's
     * {@code abilityIndex} order. Delegates to the engine so the list can never drift from
     * what {@code activateAbility} will resolve.
     */
    protected List<ActivatedAbility> buildEffectiveAbilityList(GameData gameData, Permanent permanent) {
        return gameActions.getEffectiveActivatedAbilities(gameData, permanent);
    }

    /** Returns true if an activated ability is a mana ability per CR 605.1a. */
    protected static boolean isManaAbility(ActivatedAbility ability) {
        return AbilityActivationService.isManaAbility(ability);
    }

    /**
     * Checks whether an activated ability could legally be activated right now, by asking the
     * engine's own legality validator ({@code AbilityActivationService}) with mana affordability
     * measured against {@code virtualPool}. On top of engine legality this applies AI policy:
     * never pay life down to 0 (legal, but suicidal), and skip X-based tap costs — the AI
     * doesn't model X, so it would only ever activate them as a pointless X=0 no-op.
     */
    protected boolean canActivateAbility(GameData gameData, Permanent permanent,
                                         ActivatedAbility ability, int abilityIndex,
                                         ManaPool virtualPool) {
        if (!gameActions.canActivateAbility(gameData, permanent, abilityIndex, virtualPool)) {
            return false;
        }
        return acceptsAbilityCosts(gameData, permanent, ability);
    }

    protected boolean canActivateAbility(
            GameData gameData, Permanent permanent, ActivatedAbility ability, int abilityIndex,
            ManaPool virtualPool, UUID targetId, List<UUID> targetIds) {
        if (!gameActions.canActivateAbility(
                gameData, permanent, abilityIndex, virtualPool, targetId, targetIds)) {
            return false;
        }
        return acceptsAbilityCosts(gameData, permanent, ability);
    }

    private boolean acceptsAbilityCosts(GameData gameData, Permanent permanent, ActivatedAbility ability) {
        int life = gameData.getLife(aiPlayer.getId());
        for (CardEffect effect : ability.getEffects()) {
            if (effect instanceof PayLifeCost lifeCost) {
                int counters = lifeCost.perSourceCounter() == null
                        ? 0
                        : permanent.getCounterCount(lifeCost.perSourceCounter());
                int amount = lifeCost.effectiveAmount(life, counters);
                if (amount > 0 && life <= amount) {
                    return false;
                }
            }
            // A tap cost whose count is not a flat number is announced as X at activation, which the
            // AI has no way to choose (Aryel, Knight of Windgrace). Fixed-count tap costs are fine.
            if (effect instanceof TapMultiplePermanentsCost tapCost && !(tapCost.count() instanceof Fixed)) {
                return false;
            }
        }
        return true;
    }

    protected boolean tapManaForSpell(GameData gameData, Card card, Integer xValue, int targetingTax) {
        return tapManaForSpell(gameData, card, xValue, targetingTax, 0);
    }

    /**
     * Selects the smallest set of controlled creatures that makes an optional creature-sacrifice
     * cost reduction payable. Returns an empty plan for ordinary spells and null when even all
     * available creatures cannot make the spell payable.
     */
    protected CostReductionPlan selectCostReductionPlan(
            GameData gameData, Card card, Integer xValue, int targetingTax, int delveReduction,
            ManaPool pool) {
        SacrificeCreaturesForCostReductionEffect reductionEffect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(SacrificeCreaturesForCostReductionEffect.class::isInstance)
                .map(SacrificeCreaturesForCostReductionEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (reductionEffect == null) {
            return CostReductionPlan.none();
        }
        String manaCost = manaCostForSpell(card, xValue);
        if (manaCost == null || pool == null) {
            return null;
        }

        int costModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                + targetingTax - delveReduction;
        ManaCost cost = new ManaCost(manaCost);
        List<Permanent> creatures = gameData.playerBattlefields
                .getOrDefault(aiPlayer.getId(), List.of())
                .stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .sorted(Comparator.comparingInt(permanent ->
                        gameQueryService.getEffectivePower(gameData, permanent)
                                + gameQueryService.getEffectiveToughness(gameData, permanent)))
                .toList();
        boolean sacrificeAllowed = gameQueryService.canPayLifeOrSacrificeCreaturesForCosts(gameData);
        int effectiveXValue = xValue != null ? xValue : 0;
        for (int count = 0; count <= creatures.size(); count++) {
            int reduction = count * reductionEffect.reductionPerCreature();
            int remainingModifier = costModifier - reduction;
            boolean canPay = cost.hasX()
                    ? cost.canPayWithAdditionalGenericCost(pool, effectiveXValue, remainingModifier)
                    : cost.canPay(pool, remainingModifier);
            if (canPay && (!card.isRequiresCreatureMana()
                    || cost.canPayCreatureOnly(pool, remainingModifier))) {
                if (count == 0 || sacrificeAllowed) {
                    return new CostReductionPlan(
                            creatures.subList(0, count).stream().map(Permanent::getId).toList(), reduction);
                }
            }
        }
        return null;
    }

    protected boolean tapManaForSpell(GameData gameData, Card card, Integer xValue,
                                      int targetingTax, int delveReduction) {
        if (shouldUseAlternateHandCast(gameData, card, xValue, targetingTax)) {
            return false;
        }
        String manaCost = manaCostForSpell(card, xValue);
        if (manaCost == null) return false;
        CostReductionPlan costReductionPlan = selectCostReductionPlan(
                gameData, card, xValue, targetingTax, delveReduction,
                manaManager.buildVirtualManaPool(gameData, aiPlayer.getId()));
        if (costReductionPlan == null) return false;
        return tapManaForSpell(gameData, card, xValue, targetingTax, delveReduction,
                costReductionPlan.reduction());
    }

    protected boolean tapManaForSpell(GameData gameData, Card card, Integer xValue,
                                      int targetingTax, int delveReduction, int costReduction) {
        if (shouldUseAlternateHandCast(gameData, card, xValue, targetingTax)) {
            return false;
        }
        String manaCost = manaCostForSpell(card, xValue);
        if (manaCost == null) return false;
        int costModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                + targetingTax - delveReduction - costReduction;
        AiManaManager.ManaTapAction tap = manaTapAction();
        int stackSizeBeforePayment = gameData.stack.size();

        if (card.isRequiresCreatureMana()) {
            manaManager.tapCreaturesForCost(gameData, aiPlayer.getId(), manaCost, costModifier, tap);
            return paymentOpenedDecisionWindow(gameData, stackSizeBeforePayment);
        }

        ManaCost cost = new ManaCost(manaCost);
        if (cost.hasX() && xValue != null) {
            manaManager.tapLandsForXSpell(gameData, aiPlayer.getId(), card, manaCost, xValue, costModifier, tap);
        } else {
            manaManager.tapLandsForCost(gameData, aiPlayer.getId(), manaCost, costModifier, tap);
        }
        return paymentOpenedDecisionWindow(gameData, stackSizeBeforePayment);
    }

    private boolean paymentOpenedDecisionWindow(GameData gameData, int stackSizeBeforePayment) {
        return gameData.status != GameStatus.RUNNING
                || gameData.interaction.isAwaitingInput()
                || gameData.stack.size() > stackSizeBeforePayment;
    }

    /**
     * Selects the smallest prefix of the player's untapped creatures that lets the spell's
     * remaining mana cost be paid with the current mana pool and convoke. Mana is tapped first so
     * creatures that also produce mana cannot be announced for both costs.
     *
     * @return the selected creature IDs, an empty list when no convoke is needed, or {@code null}
     * when the current state cannot pay the cost with any legal convoke selection
     */
    protected List<UUID> selectConvokeCreatureIds(GameData gameData, Card card, Integer xValue,
                                                   int targetingTax) {
        return selectConvokeCreatureIds(gameData, card, xValue, targetingTax, 0);
    }

    protected List<UUID> selectConvokeCreatureIds(GameData gameData, Card card, Integer xValue,
                                                   int targetingTax, int delveReduction) {
        String manaCost = manaCostForSpell(card, xValue);
        if (!hasConvokeAbility(gameData, card) || manaCost == null) {
            return List.of();
        }

        ManaPool pool = gameData.playerManaPools.get(aiPlayer.getId());
        if (pool == null) {
            return null;
        }

        int costModifier = castingCostService.getCastCostModifier(gameData, aiPlayer.getId(), card)
                + targetingTax;
        ManaCost cost = new ManaCost(manaCost);
        int additionalGenericCost = costModifier
                + (cost.hasX() && xValue != null ? xValue : 0) - delveReduction;
        List<ManaColor> contributions = new ArrayList<>();
        if (cost.canPayWithConvoke(pool, additionalGenericCost, contributions)) {
            return List.of();
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            return null;
        }

        List<UUID> selectedIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (permanent.isTapped() || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            selectedIds.add(permanent.getId());
            contributions.add(convokeManaColor(gameData, permanent));
            if (cost.canPayWithConvoke(pool, additionalGenericCost, contributions)) {
                return List.copyOf(selectedIds);
            }
        }
        return null;
    }

    protected int countUntappedConvokeCreatures(GameData gameData, Card card) {
        if (!hasConvokeAbility(gameData, card)) {
            return 0;
        }
        return (int) gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of()).stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .count();
    }

    private boolean hasConvokeAbility(GameData gameData, Card card) {
        if (card.getKeywords().contains(Keyword.CONVOKE)) {
            return true;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            return false;
        }
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof SpellCastingAbilityGrantingEffect grant
                        && grant.grantedAbility() == Keyword.CONVOKE
                        && predicateEvaluationService.matchesCardPredicate(card, grant.filter(), null)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ManaColor convokeManaColor(GameData gameData, Permanent permanent) {
        Set<CardColor> colors = gameQueryService.getEffectiveColors(gameData, permanent);
        if (colors != null) {
            for (CardColor color : colors) {
                ManaColor manaColor = ManaColor.fromCode(color.getCode());
                if (manaColor != null) {
                    return manaColor;
                }
            }
        }
        CardColor color = gameQueryService.getEffectiveColor(gameData, permanent);
        return color != null ? ManaColor.fromCode(color.getCode()) : null;
    }

    protected AiManaManager.ManaTapAction manaTapAction() {
        return (idx, abilityIndex) -> {
            if (abilityIndex != null) {
                send(() -> gameActions.handleActivateAbility(
                        new ActivateAbilityRequest(idx, abilityIndex, null, null, null, null, null)));
            } else {
                send(() -> gameActions.handleTapPermanent(new TapPermanentRequest(idx)));
            }
        };
    }

    protected void send(MessageHandlerAction action) {
        GameData gameData = gameRegistry.get(gameId);
        if (gameData == null || gameData.status == GameStatus.FINISHED) {
            return;
        }
        try {
            action.execute();
        } catch (Exception e) {
            log.error("AI: Error sending message in game {}", gameId, e);
        }
    }

    @FunctionalInterface
    protected interface MessageHandlerAction {
        void execute() throws Exception;
    }
}
