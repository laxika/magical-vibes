package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPaymentIntent;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceUpReplacementEffect;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.cast.ManaChoiceNarrowingService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GameService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SpellCastingService spellCastingService;
    private final StackResolutionService stackResolutionService;
    private final AbilityActivationService abilityActivationService;
    private final MulliganService mulliganService;
    private final GameOutcomeService gameOutcomeService;
    private final GameMutationCoordinator mutationCoordinator;
    private final ManaChoiceNarrowingService manaChoiceNarrowingService;
    private final CardRevealService cardRevealService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final TriggerCollectionService triggerCollectionService;
    private final CastingCostService castingCostService;
    private final PotentialManaService potentialManaService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final TurnFaceUpCopyService turnFaceUpCopyService;

    @Autowired
    public GameService(GameQueryService gameQueryService, GameLogService gameLogService,
                       CombatService combatService, TurnProgressionService turnProgressionService,
                       InteractionHandlerRegistry interactionHandlerRegistry,
                       SpellCastingService spellCastingService, StackResolutionService stackResolutionService,
                       AbilityActivationService abilityActivationService, MulliganService mulliganService,
                       GameOutcomeService gameOutcomeService, GameMutationCoordinator mutationCoordinator,
                       ManaChoiceNarrowingService manaChoiceNarrowingService,
                       CardRevealService cardRevealService, PredicateEvaluationService predicateEvaluationService,
                       AmountEvaluationService amountEvaluationService,
                       ConditionEvaluationService conditionEvaluationService,
                       PermanentCounterSupport permanentCounterSupport,
                       TriggerCollectionService triggerCollectionService,
                       CastingCostService castingCostService,
                       PotentialManaService potentialManaService,
                       @Lazy GraveyardTargetingService graveyardTargetingService,
                       TurnFaceUpCopyService turnFaceUpCopyService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.combatService = combatService;
        this.turnProgressionService = turnProgressionService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.spellCastingService = spellCastingService;
        this.stackResolutionService = stackResolutionService;
        this.abilityActivationService = abilityActivationService;
        this.mulliganService = mulliganService;
        this.gameOutcomeService = gameOutcomeService;
        this.mutationCoordinator = mutationCoordinator;
        this.manaChoiceNarrowingService = manaChoiceNarrowingService;
        this.cardRevealService = cardRevealService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.amountEvaluationService = amountEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.permanentCounterSupport = permanentCounterSupport;
        this.triggerCollectionService = triggerCollectionService;
        this.castingCostService = castingCostService;
        this.potentialManaService = potentialManaService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.turnFaceUpCopyService = turnFaceUpCopyService;
    }

    /** Compatibility constructor for focused service tests that do not exercise morph reveals. */
    public GameService(GameQueryService gameQueryService, GameLogService gameLogService,
                       CombatService combatService, TurnProgressionService turnProgressionService,
                       InteractionHandlerRegistry interactionHandlerRegistry,
                       SpellCastingService spellCastingService, StackResolutionService stackResolutionService,
                       AbilityActivationService abilityActivationService, MulliganService mulliganService,
                       GameOutcomeService gameOutcomeService, GameMutationCoordinator mutationCoordinator,
                       ManaChoiceNarrowingService manaChoiceNarrowingService) {
        this(gameQueryService, gameLogService, combatService, turnProgressionService,
                interactionHandlerRegistry, spellCastingService, stackResolutionService,
                abilityActivationService, mulliganService, gameOutcomeService, mutationCoordinator,
                manaChoiceNarrowingService, null, null, null, null, null, null, null, null, null, null);
    }

    private boolean runAsActionIfNeeded(GameData gameData, Runnable action) {
        if (mutationCoordinator.isInAction(gameData)) {
            return false;
        }
        mutationCoordinator.mutate(gameData, action);
        return true;
    }

    /**
     * Combat declaration/assignment validation failures preserve their public exception while
     * re-opening the same logical decision in a fresh successful action. Facts recorded by the
     * failed action are deliberately discarded by the coordinator, so retry delivery cannot be
     * emitted from inside the failing mutation.
     */
    private boolean runAsCombatActionIfNeeded(GameData gameData, Runnable action) {
        if (mutationCoordinator.isInAction(gameData)) {
            return false;
        }
        try {
            mutationCoordinator.mutate(gameData, action);
        } catch (IllegalStateException | IllegalArgumentException failure) {
            mutationCoordinator.mutate(
                    gameData, () -> interactionHandlerRegistry.requestActiveDecision(gameData));
            throw failure;
        }
        return true;
    }

    /**
     * Validates that the game is running, no interaction is awaiting input, and the given player
     * currently holds priority. Must be called inside the {@code synchronized(gameData)} block
     * after {@link #resolveActingPlayer(GameData, Player)}.
     *
     * @throws IllegalStateException if the game is not running, input is awaited, or the player
     *                               does not have priority
     */
    private void requirePriority(GameData gameData, Player player) {
        if (gameData.status != GameStatus.RUNNING) {
            throw new IllegalStateException("Game is not running");
        }
        if (gameData.interaction.isAwaitingInput()) {
            throw new IllegalStateException("Cannot perform this action while awaiting input");
        }
        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (priorityHolder == null || !priorityHolder.equals(player.getId())) {
            throw new IllegalStateException("You do not have priority");
        }
    }

    /**
     * Sen Triplets / City of Solitude: a player locked out can't activate any ability, including mana
     * and sacrifice abilities. The standard activated-ability path is gated in
     * {@code AbilityActivationService.validateActivationLegality}; this guards the special-action entry
     * points (mana taps, sacrifice, graveyard/hand abilities) that bypass that check.
     */
    private void requireCanActivateAbilities(GameData gameData, Player player) {
        if (gameData.playersCantActivateAbilitiesThisTurn.contains(player.getId())) {
            throw new IllegalStateException("You can't activate abilities this turn");
        }
        if (gameQueryService.isLockedOutByOwnTurnOnlyRestriction(gameData, player.getId())) {
            throw new IllegalStateException(
                    "You can only cast spells and activate abilities during your own turn");
        }
    }

    /**
     * Returns true if the game is currently in attacker declaration and the given player
     * is the declaring player. Per CR 508.1i, mana abilities may be activated during this window.
     */
    private boolean isAttackTaxManaPayment(GameData gameData, Player player) {
        return gameData.interaction.activeInteraction() instanceof PendingInteraction.AttackerDeclaration ad
                && ad.activePlayerId().equals(player.getId());
    }

    /**
     * Returns true if the game is currently in blocker declaration and the given player is the
     * defending player. CR 509.1d locks in the total cost of the blocks being declared and
     * CR 509.1e then gives that player a chance to activate mana abilities to pay it, so this
     * window is open even though nobody holds priority during the declaration.
     */
    private boolean isBlockCostManaPayment(GameData gameData, Player player) {
        return gameData.interaction.activeInteraction() instanceof PendingInteraction.BlockerDeclaration bd
                && bd.defenderId().equals(player.getId());
    }

    /**
     * Returns true if the player may activate mana abilities to pay a combat cost they are
     * currently being charged — the attack tax (CR 508.1i) or a block cost (CR 509.1e).
     */
    private boolean isCombatCostManaPayment(GameData gameData, Player player) {
        return isAttackTaxManaPayment(gameData, player) || isBlockCostManaPayment(gameData, player);
    }

    /**
     * Returns true if the given player is being asked to pay mana mid-resolution — a
     * "may pay" ability prompt or a pay-{X} amount prompt. Per CR 605.3a, mana abilities
     * may be activated whenever a rule or effect asks for a mana payment, even in the
     * middle of resolving a spell or ability.
     */
    private boolean isMayCostManaPayment(GameData gameData, Player player) {
        return switch (gameData.interaction.activeInteraction()) {
            case PendingInteraction.MayAbilityChoice mc ->
                    mc.playerId().equals(player.getId()) && mc.manaCost() != null;
            case PendingInteraction.XValueChoice xc ->
                    xc.playerId().equals(player.getId()) && xc.manaPayment();
            case PendingInteraction.AlternateCastXValueChoice ax ->
                    ax.playerId().equals(player.getId());
            case PendingInteraction.TurnFaceUpXValueChoice tfu ->
                    tfu.playerId().equals(player.getId());
            case null, default -> false;
        };
    }

    /**
     * the controlled player when the controlled player should be acting (has priority
     * or is the expected respondent for an interaction).
     * If the controller is acting as themselves (e.g., passing their own priority as
     * non-active player), the original player is returned.
     */
    private Player resolveActingPlayer(GameData gameData, Player player) {
        if (gameData.mindControllerPlayerId == null) return player;
        if (!player.getId().equals(gameData.mindControllerPlayerId)) return player;
        UUID controlledId = gameData.mindControlledPlayerId;
        if (controlledId == null) return player;

        // Check if the controlled player is the expected respondent for an interaction
        if (gameData.interaction.isAwaitingInput()) {
            UUID activeDecider = interactionHandlerRegistry.activeDecidingPlayerId(gameData);
            if (controlledId.equals(activeDecider)) {
                return new Player(controlledId, gameData.playerIdToName.get(controlledId));
            }
            return player;
        }

        // Check if the controlled player currently holds priority
        UUID priorityHolder = gameQueryService.getPriorityPlayerId(gameData);
        if (controlledId.equals(priorityHolder)) {
            return new Player(controlledId, gameData.playerIdToName.get(controlledId));
        }

        return player; // Controller acts as themselves
    }

    public void passPriority(GameData gameData, Player player) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData, () -> passPriority(gameData, actionPlayer))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);

            // Passing priority closes the window in which mana-ability activations could
            // still be undone by the cancel-casting UI.
            gameData.revertableManaActivations.clear();

            // CR 603.3: Flush triggers deferred from mana-ability activations.
            // They go on the stack now (the next time a player would receive priority)
            // and both players must pass again before the top resolves.
            if (!gameData.pendingManaAbilityTriggers.isEmpty()) {
                gameData.stack.addAll(gameData.pendingManaAbilityTriggers);
                gameData.pendingManaAbilityTriggers.clear();
                gameData.priorityPassedBy.clear();
            }

            gameData.priorityPassedBy.add(player.getId());
            log.info("Game {} - {} passed priority on step {} (passed: {}/2)",
                    gameData.id, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size());

            if (gameData.priorityPassedBy.size() >= 2) {
                if (!gameData.stack.isEmpty()) {
                    stackResolutionService.resolveTopOfStack(gameData);
                } else {
                    turnProgressionService.advanceStep(gameData);
                }
            } else {
                invalidateForAllPlayers(gameData);
            }

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    public void paySearchTax(GameData gameData, Player player) {
        if (runAsActionIfNeeded(gameData, () -> paySearchTax(gameData, player))) return;
        synchronized (gameData) {
            requirePriority(gameData, player);

            // Find unpaid CantSearchLibrariesEffect permanents
            List<UUID> unpaidArbiterIds = new java.util.ArrayList<>();
            gameData.forEachPermanent((playerId, permanent) -> {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof CantSearchLibrariesEffect restriction && restriction.payableToIgnore()) {
                        Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds.get(player.getId());
                        if (paidSet == null || !paidSet.contains(permanent.getId())) {
                            unpaidArbiterIds.add(permanent.getId());
                        }
                    }
                }
            });

            if (unpaidArbiterIds.isEmpty()) {
                throw new IllegalStateException("No unpaid search tax to pay");
            }

            int totalCost = unpaidArbiterIds.size() * 2;
            ManaCost cost = new ManaCost("{" + totalCost + "}");
            ManaPool pool = gameData.playerManaPools.get(player.getId());

            if (pool == null || !cost.canPay(pool)) {
                throw new IllegalStateException("Not enough mana to pay search tax");
            }

            cost.pay(pool);

            Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds
                    .computeIfAbsent(player.getId(), k -> ConcurrentHashMap.newKeySet());
            paidSet.addAll(unpaidArbiterIds);

            String logEntry = player.getUsername() + " pays {" + totalCost + "} for Leonin Arbiter search tax.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} pays {{}} for Leonin Arbiter search tax (special action)",
                    gameData.id, player.getUsername(), totalCost);

            invalidateForAllPlayers(gameData);
        }
    }

    /** Exiles a foretell card from the active player's hand as a special action. */
    public void foretellCard(GameData gameData, Player player, int cardIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData, () -> foretellCard(gameData, actionPlayer, cardIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            boolean activePlayerTurn = player.getId().equals(gameData.activePlayerId);
            boolean canForetellDuringAnyTurn = castingCostService != null
                    && castingCostService.canForetellDuringAnyTurn(gameData, player.getId());
            if (!activePlayerTurn && !canForetellDuringAnyTurn) {
                throw new IllegalStateException("Foretell can only be used during your turn");
            }

            List<Card> hand = gameData.playerHands.get(player.getId());
            if (hand == null || cardIndex < 0 || cardIndex >= hand.size()) {
                throw new IllegalArgumentException("Invalid card index");
            }
            Card card = hand.get(cardIndex);
            ManaCost foretellCost = castingCostService == null
                    ? card.getCastingOption(ForetellCast.class)
                    .map(ForetellCast::manaCostString)
                    .filter(java.util.Objects::nonNull)
                    .map(ManaCost::new)
                    .orElse(null)
                    : castingCostService.getForetellCost(gameData, player.getId(), card);
            if (foretellCost == null) {
                throw new IllegalStateException("Card does not have foretell");
            }

            ManaPool pool = gameData.playerManaPools.get(player.getId());
            ManaCost cost = castingCostService == null
                    ? new ManaCost("{2}")
                    : castingCostService.getForetellActionCost(gameData, player.getId());
            if (pool == null || !cost.canPayForForetell(pool)) {
                throw new IllegalStateException("Not enough mana to foretell");
            }
            if (cost.canPay(pool)) {
                cost.pay(pool);
            } else {
                cost.payForForetell(pool);
            }
            hand.remove(cardIndex);
            gameData.addForetoldCardToExile(player.getId(), card, foretellCost);
            triggerCollectionService.checkControllerForetellTriggers(gameData, player.getId(), card);
            gameData.priorityPassedBy.clear();
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " foretells ", card, "."));
            log.info("Game {} - {} foretells {}", gameData.id, player.getUsername(), card.getName());
            invalidateForAllPlayers(gameData);
        }
    }

    public void surrender(GameData gameData, Player player) {
        if (runAsActionIfNeeded(gameData, () -> surrender(gameData, player))) return;
        synchronized (gameData) {
            if (gameData.status == GameStatus.FINISHED) {
                throw new IllegalStateException("Game is already finished");
            }
            UUID opponentId = gameQueryService.getOpponentId(gameData, player.getId());
            String logEntry = player.getUsername() + " surrenders!";
            gameLogService.append(gameData, GameLog.text(logEntry));
            gameOutcomeService.declareWinner(gameData, opponentId);
        }
    }

    public void advanceStep(GameData gameData) {
        if (runAsActionIfNeeded(gameData, () -> advanceStep(gameData))) return;
        turnProgressionService.advanceStep(gameData);
    }

    public void keepHand(GameData gameData, Player player) {
        if (runAsActionIfNeeded(gameData, () -> keepHand(gameData, player))) return;
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.keepHand(gameData, player);
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
        if (runAsActionIfNeeded(gameData, () -> bottomCards(gameData, player, cardIndices))) return;
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.bottomCards(gameData, player, cardIndices);
        }
    }

    public void mulligan(GameData gameData, Player player) {
        if (runAsActionIfNeeded(gameData, () -> mulligan(gameData, player))) return;
        synchronized (gameData) {
            if (gameData.status != GameStatus.MULLIGAN) {
                throw new IllegalStateException("Game is not in mulligan phase");
            }
            mulliganService.mulligan(gameData, player);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, List.of(), List.of(), false, null);
        }
    }

    /**
     * Casts a modal spell that also has an {@code {X}} cost: {@code xValue} selects the mode while
     * {@code modalXValue} carries the real X paid (e.g. Alabaster Potion).
     */
    public void playModalXCard(GameData gameData, Player player, int cardIndex, int modeIndex, int modalXValue, UUID targetId) {
        playModalXCard(gameData, player, cardIndex, modeIndex, modalXValue, targetId, List.of());
    }

    /**
     * Casts a modal {@code {X}} spell with an optional graveyard/spell {@code targetId} and
     * permanent/player {@code targetIds} (e.g. Profane Command's choose-two + X).
     * {@code modeIndex} is a 0-based mode for choose-one, or a negative bitmask from
     * {@link com.github.laxika.magicalvibes.model.effect.ChooseOneEffect#encodeModeSelection}.
     */
    public void playModalXCard(GameData gameData, Player player, int cardIndex, int modeIndex, int modalXValue,
                               UUID targetId, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playModalXCard(gameData, actionPlayer, cardIndex, modeIndex, modalXValue, targetId, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, modeIndex, targetId, null, targetIds, List.of(),
                    false, null, null, List.of(), null, null, false, null, modalXValue);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, false, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, null);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, false);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex, exileGraveyardCardIndices, kicked))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices, kicked);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices, List<UUID> imposedSacrificePermanentIds) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, imposedSacrificePermanentIds, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices, List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                        discardHandCardIndex, discardHandCardIndices,
                        imposedSacrificePermanentIds, additionalCostSacrificePermanentIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                    alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    kicked, discardHandCardIndex, discardHandCardIndices, null,
                    imposedSacrificePermanentIds, additionalCostSacrificePermanentIds);
        }
    }

    /**
     * Cast entry point that also carries {@code repeatedAdditionalCosts} — the caster's chosen
     * payments for a repeatable additional mana cost ("you may pay {1}{R} and/or {1}{G} any number
     * of times", Primitive Justice), one entry per repetition — and {@code buyback}, whether the
     * caster pays the spell's optional buyback cost (CR 702.27).
     */
    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices, List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds, List<String> repeatedAdditionalCosts, boolean buyback) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, convokeCreatureIds,
                fromGraveyard, sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                discardHandCardIndices, imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                repeatedAdditionalCosts, buyback, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                         Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                         List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                         Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                         Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                         boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                         List<UUID> imposedSacrificePermanentIds,
                         List<UUID> additionalCostSacrificePermanentIds,
                         List<String> repeatedAdditionalCosts, boolean buyback,
                         UUID beholdPermanentId, Integer beholdHandCardIndex) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                beholdPermanentId, beholdHandCardIndex, List.of(), List.of(), null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId, Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds, Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices, boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices, List<UUID> imposedSacrificePermanentIds, List<UUID> additionalCostSacrificePermanentIds, List<String> repeatedAdditionalCosts, boolean buyback, Integer sharedColorDiscardHandCardIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                        discardHandCardIndex, discardHandCardIndices,
                        imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                        repeatedAdditionalCosts, buyback, sharedColorDiscardHandCardIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                    alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                    kicked, discardHandCardIndex, discardHandCardIndices, null,
                    imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                    repeatedAdditionalCosts, buyback, sharedColorDiscardHandCardIndex);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                         Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                         List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                         Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                         Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                         boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                         List<UUID> imposedSacrificePermanentIds,
                         List<UUID> additionalCostSacrificePermanentIds,
                         List<String> repeatedAdditionalCosts, boolean buyback,
                         UUID beholdPermanentId, Integer beholdHandCardIndex,
                         List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                         CardSubtype beholdChosenSubtype) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                beholdChosenSubtype, null);
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                         Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                         List<UUID> convokeCreatureIds, boolean fromGraveyard, UUID sacrificePermanentId,
                         Integer phyrexianLifeCount, List<UUID> alternateCostSacrificePermanentIds,
                         Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                         boolean kicked, Integer discardHandCardIndex, List<Integer> discardHandCardIndices,
                         List<UUID> imposedSacrificePermanentIds,
                         List<UUID> additionalCostSacrificePermanentIds,
                         List<String> repeatedAdditionalCosts, boolean buyback,
                         UUID beholdPermanentId, Integer beholdHandCardIndex,
                         List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                         CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                        discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                        additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                        beholdPermanentId, beholdHandCardIndex, beholdPermanentIds,
                        beholdHandCardIndices, beholdChosenSubtype, chosenCreatureType))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                    phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex,
                    exileGraveyardCardIndices, kicked, discardHandCardIndex, discardHandCardIndices,
                    null, imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                    repeatedAdditionalCosts, buyback, beholdPermanentId, beholdHandCardIndex,
                    beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype, chosenCreatureType);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, List.of(), null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue, UUID targetId, List<UUID> targetIds) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, null, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds, exileGraveyardCardIndices, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, retraceDiscardHandCardIndex, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, null);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, Map<UUID, Integer> damageAssignments) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, List.of(), damageAssignments);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    Map<UUID, Integer> damageAssignments) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playFlashbackSpell(gameData, actionPlayer, graveyardCardIndex, xValue, targetId,
                        targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                        retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                        damageAssignments))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                    exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds, retraceDiscardHandCardIndex,
                    sacrificePermanentId, additionalCostSacrificePermanentIds, damageAssignments);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> beholdPermanentIds,
                                    List<Integer> beholdHandCardIndices) {
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, List.of(), beholdPermanentIds,
                beholdHandCardIndices);
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playFlashbackSpell(gameData, actionPlayer, graveyardCardIndex, xValue, targetId,
                        targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                        retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                        beholdPermanentIds,
                        beholdHandCardIndices))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId,
                    targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                    retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                    beholdPermanentIds,
                    beholdHandCardIndices);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    Map<UUID, Integer> damageAssignments,
                                    List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                                    List<Integer> discardHandCardIndices) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playFlashbackSpell(gameData, actionPlayer, graveyardCardIndex, xValue, targetId,
                        targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                        retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                        damageAssignments, beholdPermanentIds, beholdHandCardIndices,
                        discardHandCardIndices))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId,
                    targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                    retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                    damageAssignments, beholdPermanentIds, beholdHandCardIndices, discardHandCardIndices);
        }
    }

    public void playFlashbackSpell(GameData gameData, Player player, UUID graveyardCardId, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playFlashbackSpell(gameData, actionPlayer, graveyardCardId, xValue, targetId,
                        targetIds, exileGraveyardCardIndices, chosenGraveyardType))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardId, xValue, targetId, targetIds, exileGraveyardCardIndices, chosenGraveyardType);
        }
    }

    public void playGraveyardLand(GameData gameData, Player player, UUID graveyardCardId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playGraveyardLand(gameData, actionPlayer, graveyardCardId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playGraveyardLand(gameData, player, graveyardCardId, 0);
        }
    }

    public void playCardWithEvoke(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithEvoke(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithEvoke(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of());
        }
    }

    public void playCardWithProwl(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                  Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithProwl(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithProwl(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of());
        }
    }

    /**
     * Casts a card for an alternative cost that carries no cast-request payload and nothing to
     * choose (e.g. Spinning Darkness's "exile the top three black cards of your graveyard").
     */
    public void playCardWithAlternateCost(GameData gameData, Player player, int cardIndex, Integer xValue,
                                          UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithAlternateCost(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithAlternateCost(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds != null ? targetIds : List.of());
        }
    }

    public void playAdventureCard(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playAdventureCard(gameData, actionPlayer, cardIndex, xValue, targetId, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playAdventureCard(gameData, player, cardIndex, xValue, targetId,
                    targetIds != null ? targetIds : List.of());
        }
    }

    public void playCardWithMorph(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds) {
        playCardWithMorph(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, null);
    }

    public void playCardWithMorph(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                  Integer revealedHandCardIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithMorph(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, revealedHandCardIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithMorph(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds != null ? targetIds : List.of(), revealedHandCardIndex);
        }
    }

    public void turnFaceUp(GameData gameData, Player player, int permanentIndex) {
        turnFaceUp(gameData, player, permanentIndex, null);
    }

    public void turnFaceUp(GameData gameData, Player player, int permanentIndex, Integer revealedHandCardIndex) {
        turnFaceUp(gameData, player, permanentIndex, revealedHandCardIndex, List.of());
    }

    public void turnFaceUp(GameData gameData, Player player, int permanentIndex, Integer revealedHandCardIndex,
                           List<UUID> morphAdditionalCostPermanentIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> turnFaceUp(gameData, actionPlayer, permanentIndex, revealedHandCardIndex,
                        morphAdditionalCostPermanentIds))) return;
        turnFaceUpInternal(gameData, player, permanentIndex, revealedHandCardIndex,
                morphAdditionalCostPermanentIds, null, false);
    }

    /** Completes a face-up payment after the player has chosen X for a disguise cost. */
    public void completeTurnFaceUpXChoice(GameData gameData, Player player, UUID permanentId, int xValue) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> completeTurnFaceUpXChoice(gameData, actionPlayer, permanentId, xValue))) return;
        synchronized (gameData) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null) {
                throw new IllegalStateException("Permanent is no longer on the battlefield");
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
            if (battlefield == null || !battlefield.contains(permanent)) {
                throw new IllegalStateException("Permanent is no longer controlled by the player");
            }
            turnFaceUpInternal(gameData, player, battlefield.indexOf(permanent), null,
                    List.of(), xValue, true);
        }
    }

    private void turnFaceUpInternal(GameData gameData, Player player, int permanentIndex,
                                     Integer revealedHandCardIndex, List<UUID> morphAdditionalCostPermanentIds,
                                     Integer xValue,
                                     boolean completingXChoice) {
        synchronized (gameData) {
            if (!completingXChoice) {
                player = resolveActingPlayer(gameData, player);
                requirePriority(gameData, player);
            }
            List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
            if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
                throw new IllegalArgumentException("Invalid permanent index");
            }
            Permanent permanent = battlefield.get(permanentIndex);
            if (!permanent.isFaceDown()) {
                throw new IllegalStateException("Permanent is not face down");
            }
            if (gameQueryService.isTurnFaceUpPrevented(gameData, permanent)) {
                throw new IllegalStateException("Permanent can't be turned face up during this turn");
            }
            boolean cloaked = permanent.isCloaked();
            String morphCost = permanent.getCard().getMorphCost();
            String faceUpCost = cloaked ? permanent.getCard().getManaCost() : morphCost;
            if ((!cloaked && morphCost == null) || (cloaked && (faceUpCost == null
                    || !permanent.getCard().hasType(CardType.CREATURE)))
                    || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.computeStaticBonus(gameData, permanent).losesAllAbilities()) {
                throw new IllegalStateException("Permanent cannot be turned face up");
            }
            List<UUID> additionalCostPermanentIds = morphAdditionalCostPermanentIds != null
                    ? morphAdditionalCostPermanentIds : List.of();
            ReturnPermanentsCost morphAdditionalCost = permanent.getCard().getMorphAdditionalCost();
            if (morphAdditionalCost != null) {
                spellCastingService.validateMorphAdditionalCost(
                        gameData, player, morphAdditionalCost, additionalCostPermanentIds);
            }
            RevealCardsFromHandCastingCost morphRevealCost = permanent.getCard().getMorphRevealCost();
            if (!cloaked && morphRevealCost != null) {
                List<Card> hand = gameData.playerHands.get(player.getId());
                if (revealedHandCardIndex == null || hand == null
                        || revealedHandCardIndex < 0 || revealedHandCardIndex >= hand.size()) {
                    throw new IllegalStateException("Must reveal " + morphRevealLabel(morphRevealCost)
                            + " from your hand to turn the permanent face up");
                }
                Card toReveal = hand.get(revealedHandCardIndex);
                if (morphRevealCost.predicate() != null
                        && !predicateEvaluationService.matchesCardPredicate(toReveal,
                        morphRevealCost.predicate(), toReveal.getId())) {
                    throw new IllegalStateException("Revealed card must be " + morphRevealLabel(morphRevealCost));
                }
                cardRevealService.revealToAllPlayers(gameData, player.getId(),
                        GameEventFact.RevealZone.HAND, List.of(toReveal));
                gameLogService.append(gameData, GameLog.textCardText(
                        player.getUsername() + " reveals ", toReveal, " to turn the permanent face up."));
            } else {
                ManaCost cost = new ManaCost(faceUpCost);
                DynamicAmount morphCostReduction = permanent.getCard().getMorphCostReduction();
                if (!cloaked && morphCostReduction != null && amountEvaluationService != null) {
                    int reduction = amountEvaluationService.evaluate(gameData, morphCostReduction,
                            AmountContext.forCasting(player.getId()));
                    cost = cost.reducedBy(new ManaCost("{" + reduction + "}"));
                }
                ManaPool pool = gameData.playerManaPools.get(player.getId());
                if (pool == null) {
                    pool = new ManaPool();
                }
                ManaPool.FaceDownSpellsOrTurnFaceUpManaState restrictedMana =
                        pool.promoteFaceDownSpellsOrTurnFaceUpMana();
                try {
                    if (cost.hasX() && xValue == null) {
                        ManaPool potentialPool = potentialManaService != null
                                ? potentialManaService.buildVirtualManaPool(gameData, player.getId()) : pool;
                        int maxX = cost.calculateMaxX(potentialPool);
                        if (maxX <= 0 && !cost.canPay(pool, 0)) {
                            throw new IllegalStateException("Not enough mana to turn the permanent face up");
                        }
                        beginTurnFaceUpXChoice(gameData, player, permanent, faceUpCost, maxX);
                        return;
                    }
                    int effectiveXValue = xValue != null ? xValue : 0;
                    if (effectiveXValue < 0 || !cost.canPay(pool, effectiveXValue)) {
                        ManaPool potentialPool = potentialManaService != null
                                ? potentialManaService.buildVirtualManaPool(gameData, player.getId()) : pool;
                        if (cost.canPay(potentialPool, effectiveXValue)) {
                            beginTurnFaceUpXChoice(gameData, player, permanent, faceUpCost,
                                    cost.calculateMaxX(potentialPool));
                            return;
                        }
                        throw new IllegalStateException("Not enough mana to turn the permanent face up");
                    }
                    cost.pay(pool, effectiveXValue);
                } finally {
                    pool.restorePromotedFaceDownSpellsOrTurnFaceUpMana(restrictedMana);
                }
            }
            if (morphAdditionalCost != null) {
                spellCastingService.payMorphAdditionalCost(
                        gameData, player, permanent.getCard(), morphAdditionalCost, additionalCostPermanentIds);
            }
            finishTurningFaceUp(gameData, permanent, player.getId(), xValue, true);
        }
    }

    /** Turns a targeted face-down creature face up without using its morph or disguise action. */
    public void turnPermanentFaceUpWithoutPayingManaCost(GameData gameData, Permanent permanent) {
        synchronized (gameData) {
            if (permanent == null || !permanent.isFaceDown()
                    || !gameQueryService.isCreature(gameData, permanent)
                    || gameQueryService.isTurnFaceUpPrevented(gameData, permanent)) {
                return;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId == null) {
                return;
            }
            finishTurningFaceUp(gameData, permanent, controllerId, null, false);
        }
    }

    private void finishTurningFaceUp(GameData gameData, Permanent permanent, UUID controllerId,
                                     Integer xValue, boolean autoPass) {
        permanent.turnFaceUp();
        List<TurnFaceUpReplacementEffect> replacements = permanent.getCard()
                .getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(TurnFaceUpReplacementEffect.class::isInstance)
                .map(TurnFaceUpReplacementEffect.class::cast)
                .toList();
        for (TurnFaceUpReplacementEffect replacement : replacements) {
            int counterCount = amountEvaluationService.evaluate(gameData, replacement.counterAmount(),
                    AmountContext.forEnteringPermanent(controllerId, permanent, xValue != null ? xValue : 0));
            permanentCounterSupport.applyPlusOnePlusOneCounters(
                    gameData, null, permanent, counterCount);
        }
        if (turnFaceUpCopyService != null
                && turnFaceUpCopyService.prepareChoice(gameData, permanent, controllerId)) {
            return;
        }
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is turned face up."));

        if (triggerCollectionService != null) {
            triggerCollectionService.checkSelfOrAllyPermanentTurnsFaceUpTriggers(
                    gameData, controllerId, permanent);
            triggerCollectionService.checkSelfOrAllyCreatureTurnsFaceUpTriggers(
                    gameData, controllerId, permanent);
        }

        List<CardEffect> effects = permanent.getCard().getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(effect -> !(effect instanceof TurnFaceUpReplacementEffect))
                .filter(effect -> turnedFaceUpTriggerConditionIsMet(gameData, permanent, controllerId, effect))
                .toList();
        if (!effects.isEmpty()) {
            if (effects.size() == 1 && effects.getFirst() instanceof ChooseOneEffect modal) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        permanent.getCard(), controllerId, modal, permanent.getId()));
                if (autoPass) {
                    turnProgressionService.resolveAutoPass(gameData);
                }
                return;
            }
            BattlefieldAndGraveyardCardChoosingEffect mixedZoneChoice = effects.stream()
                    .filter(BattlefieldAndGraveyardCardChoosingEffect.class::isInstance)
                    .map(BattlefieldAndGraveyardCardChoosingEffect.class::cast)
                    .findFirst().orElse(null);
            if (mixedZoneChoice != null && graveyardTargetingService != null) {
                graveyardTargetingService.handleBattlefieldAndGraveyardExileETBTargeting(
                        gameData, controllerId, permanent.getCard(), effects, permanent.getId(),
                        mixedZoneChoice, xValue != null ? xValue : 0);
                if (autoPass) {
                    turnProgressionService.resolveAutoPass(gameData);
                }
                return;
            }
            boolean targetsSpell = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.SPELL));
            boolean targetsPlayer = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean targetsPermanent = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (targetsSpell) {
                StackEntryPredicate spellFilter = null;
                boolean includeAbilities = false;
                if (permanent.getCard().getTargetFilter() instanceof StackEntryPredicateTargetFilter filter) {
                    spellFilter = filter.predicate();
                    includeAbilities = TriggerCollectionService.predicateContainsHasTarget(filter.predicate());
                }
                gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                        permanent.getCard(), controllerId, effects, spellFilter, includeAbilities,
                        permanent.getId()));
            } else if (targetsPlayer || targetsPermanent) {
                boolean multiTarget = permanent.getCard().getSpellTargets().size() > 1
                        || permanent.getCard().getSpellTargets().stream()
                        .anyMatch(group -> group.getMaxTargets() > 1 || group.getMinTargets() == 0
                                || group.getDynamicMinTargets() != null);
                if (multiTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            permanent.getCard(), controllerId, effects, permanent.getId(), List.of(), 0, 0));
                } else {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            permanent.getCard(), controllerId, effects,
                            !targetsPermanent, permanent.getCard().getTargetFilter(), 0, permanent.getId()));
                }
            } else {
                gameData.stack.add(new com.github.laxika.magicalvibes.model.StackEntry(
                        com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(), controllerId, permanent.getCard().getName() + "'s ability",
                        effects, permanent.getId(), List.of()));
            }
        }
        if (autoPass) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void beginTurnFaceUpXChoice(GameData gameData, Player player, Permanent permanent,
                                        String morphCost, int maxX) {
        String prompt = "Choose a value for X to turn " + permanent.getCard().getName()
                + " face up (" + morphCost + ").";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.TurnFaceUpXValueChoice(
                player.getId(), permanent.getId(), morphCost, maxX, prompt,
                permanent.getCard().getName()));
    }

    private boolean turnedFaceUpTriggerConditionIsMet(GameData gameData, Permanent source, UUID controllerId,
                                                      CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()
                || conditionEvaluationService == null) {
            return true;
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId));
    }
    private String morphRevealLabel(RevealCardsFromHandCastingCost cost) {
        return cost.label() != null ? cost.label() + " card" : "a card";
    }

    /** Casts a card for its overload cost (CR 702.96a). Overloaded spells never take targets (CR 702.96b). */
    public void playCardWithOverload(GameData gameData, Player player, int cardIndex, Integer xValue) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithOverload(gameData, actionPlayer, cardIndex, xValue))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithOverload(gameData, player, cardIndex, xValue);
        }
    }

    public void playCardWithConspire(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                     Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> conspireCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithConspire(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, conspireCreatureIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithConspire(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(), conspireCreatureIds != null ? conspireCreatureIds : List.of());
        }
    }

    public void playCardWithSplice(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                   Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                   List<Integer> spliceHandCardIndices) {
        playCardWithSplice(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                spliceHandCardIndices, List.of());
    }

    public void playCardWithSplice(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId,
                                   Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                   List<Integer> spliceHandCardIndices, List<UUID> splicePermanentIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithSplice(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, spliceHandCardIndices, splicePermanentIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithSplice(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(),
                    spliceHandCardIndices != null ? spliceHandCardIndices : List.of(),
                    splicePermanentIds != null ? splicePermanentIds : List.of());
        }
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetId) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId, List.of());
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                exileCounterCostPermanentIds, List.of());
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds,
                                  List<UUID> convokeCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardFromExile(gameData, actionPlayer, exileCardId, xValue, targetId,
                        exileCounterCostPermanentIds, convokeCreatureIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                    exileCounterCostPermanentIds != null ? exileCounterCostPermanentIds : List.of(),
                    convokeCreatureIds != null ? convokeCreatureIds : List.of());
        }
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardFromLibraryTop(gameData, actionPlayer, xValue, targetId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromLibraryTop(gameData, player, xValue, targetId);
        }
    }

    public void tapPermanent(GameData gameData, Player player, int permanentIndex) {
        tapPermanent(gameData, player, permanentIndex, null);
    }

    /**
     * @param paymentIntent what the player is tapping this source for, so an "any colour" prompt can
     *                      grey out the colours that would strand it; {@code null} when unknown.
     */
    public void tapPermanent(GameData gameData, Player player, int permanentIndex, ManaPaymentIntent paymentIntent) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> tapPermanent(gameData, actionPlayer, permanentIndex, paymentIntent))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!isCombatCostManaPayment(gameData, player) && !isMayCostManaPayment(gameData, player)) {
                requirePriority(gameData, player);
            }
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.tapPermanent(gameData, player, permanentIndex);
            manaChoiceNarrowingService.narrowActiveManaColorChoice(gameData, player.getId(), paymentIntent);
        }
    }

    /**
     * Undoes the player's still-revertable mana-ability activations (MTGO-style cancel while
     * paying for a spell): tapped sources untap and the mana they produced leaves the pool.
     * Allowed whenever the player could have activated the abilities in the first place —
     * holding priority, paying a combat cost during attacker or blocker declaration, or
     * answering a "may pay" prompt.
     */
    public void revertManaActivations(GameData gameData, Player player) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> revertManaActivations(gameData, actionPlayer))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!isCombatCostManaPayment(gameData, player) && !isMayCostManaPayment(gameData, player)) {
                requirePriority(gameData, player);
            }
            abilityActivationService.revertManaActivations(gameData, player);
        }
    }

    public void sacrificePermanent(GameData gameData, Player player, int permanentIndex, UUID targetId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> sacrificePermanent(gameData, actionPlayer, permanentIndex, targetId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.sacrificePermanent(gameData, player, permanentIndex, targetId);
        }
    }

    public void tapForeignLandForMana(GameData gameData, Player player, UUID permanentId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> tapForeignLandForMana(gameData, actionPlayer, permanentId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.tapForeignLandForMana(gameData, player, permanentId);
        }
    }

    public void payLifeForColorlessMana(GameData gameData, Player player) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> payLifeForColorlessMana(gameData, actionPlayer))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.payLifeForColorlessMana(gameData, player);
        }
    }

    public void payGuardianAngel(GameData gameData, Player player, UUID targetId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> payGuardianAngel(gameData, actionPlayer, targetId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.payGuardianAngel(gameData, player, targetId);
        }
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone) {
        activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds) {
        activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, null);
    }

    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments, null);
    }

    /**
     * @param paymentIntent what the player is activating this mana ability for, so an "any colour"
     *                      prompt can grey out the colours that would strand it; {@code null} when
     *                      unknown or when the ability is not being activated to pay for something.
     */
    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments, ManaPaymentIntent paymentIntent) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateAbility(gameData, actionPlayer, permanentIndex, abilityIndex, xValue,
                        targetId, targetZone, targetIds, damageAssignments, paymentIntent))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (isAttackTaxManaPayment(gameData, player)) {
                // CR 508.1i: only mana abilities allowed during attacker declaration
                if (!abilityActivationService.isManaAbilityAt(gameData, player.getId(), permanentIndex, abilityIndex)) {
                    throw new IllegalStateException("Only mana abilities can be activated during attacker declaration");
                }
            } else if (isBlockCostManaPayment(gameData, player)) {
                // CR 509.1e: only mana abilities allowed during blocker declaration
                if (!abilityActivationService.isManaAbilityAt(gameData, player.getId(), permanentIndex, abilityIndex)) {
                    throw new IllegalStateException("Only mana abilities can be activated during blocker declaration");
                }
            } else {
                requirePriority(gameData, player);
            }
            abilityActivationService.activateAbility(gameData, player, permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments);
            manaChoiceNarrowingService.narrowActiveManaColorChoice(gameData, player.getId(), paymentIntent);
        }
    }

    public void activateStackAbility(GameData gameData, Player player, UUID stackCardId,
                                     Integer abilityIndex, Integer discardHandCardIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateStackAbility(gameData, actionPlayer, stackCardId, abilityIndex,
                        discardHandCardIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.activateStackAbility(gameData, player, stackCardId,
                    abilityIndex, discardHandCardIndex);
        }
    }

    public void activateExiledAbility(GameData gameData, Player player, UUID exiledCardId,
                                      Integer abilityIndex, Integer xValue, UUID targetId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateExiledAbility(gameData, actionPlayer, exiledCardId, abilityIndex, xValue, targetId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateExiledAbility(gameData, player, exiledCardId, abilityIndex, xValue, targetId);
        }
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex) {
        activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, null, null);
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex, Integer xValue) {
        activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, xValue, null);
    }

    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex,
                                         Integer xValue, UUID targetId) {
        activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, xValue, targetId, null);
    }

    /**
     * Activates a graveyard ability, optionally targeting cards in graveyards (e.g. Soul of Innistrad's
     * "Return up to three target creature cards from your graveyard to your hand").
     */
    public void activateGraveyardAbility(GameData gameData, Player player, int graveyardCardIndex, Integer abilityIndex,
                                         Integer xValue, UUID targetId, List<UUID> graveyardTargetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateGraveyardAbility(gameData, actionPlayer, graveyardCardIndex, abilityIndex,
                        xValue, targetId, graveyardTargetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateGraveyardAbility(gameData, player, graveyardCardIndex, abilityIndex, xValue,
                    targetId, graveyardTargetIds);
        }
    }

    public void activateHandAbility(GameData gameData, Player player, int handCardIndex, Integer abilityIndex, UUID targetId) {
        activateHandAbility(gameData, player, handCardIndex, abilityIndex, targetId, null);
    }

    public void activateHandAbility(GameData gameData, Player player, int handCardIndex, Integer abilityIndex, UUID targetId, Integer xValue) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateHandAbility(gameData, actionPlayer, handCardIndex, abilityIndex, targetId,
                        xValue))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateHandAbility(gameData, player, handCardIndex, abilityIndex, targetId, xValue);
        }
    }

    public void activateHandAbilityWithGraveyardTargets(GameData gameData, Player player, int handCardIndex, Integer abilityIndex, List<UUID> graveyardCardIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateHandAbilityWithGraveyardTargets(gameData, actionPlayer, handCardIndex,
                        abilityIndex, graveyardCardIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateHandAbilityWithGraveyardTargets(gameData, player, handCardIndex, abilityIndex, graveyardCardIds);
        }
    }

    /**
     * Pure legality query: could {@code playerId} activate the ability at {@code abilityIndex} on
     * {@code permanent} right now? Runs the engine's own activation checks (everything except
     * target choice, with X assumed 0) against the given mana pool, which may be hypothetical.
     * Never mutates game state. Exposed so AI players share the engine's legality rules instead
     * of re-implementing them.
     */
    public boolean canActivateAbility(GameData gameData, UUID playerId, Permanent permanent, int abilityIndex, ManaPool manaPool) {
        synchronized (gameData) {
            return abilityActivationService.canActivateAbility(gameData, playerId, permanent, abilityIndex, manaPool);
        }
    }

    public boolean canActivateAbility(
            GameData gameData, UUID playerId, Permanent permanent, int abilityIndex,
            ManaPool manaPool, UUID targetId, List<UUID> targetIds) {
        synchronized (gameData) {
            return abilityActivationService.canActivateAbility(
                    gameData, playerId, permanent, abilityIndex, manaPool, targetId, targetIds);
        }
    }

    public int getActivatedAbilityAdditionalGenericCost(
            GameData gameData, UUID playerId, Permanent permanent, int abilityIndex,
            UUID targetId, List<UUID> targetIds) {
        synchronized (gameData) {
            return abilityActivationService.getActivatedAbilityAdditionalGenericCost(
                    gameData, playerId, permanent, abilityIndex, targetId, targetIds);
        }
    }

    /**
     * Returns the activated abilities currently available on a permanent (own + static-granted +
     * temporary), in {@code abilityIndex} order. Read-only.
     */
    public List<ActivatedAbility> getEffectiveActivatedAbilities(GameData gameData, Permanent permanent) {
        synchronized (gameData) {
            return abilityActivationService.getEffectiveActivatedAbilities(gameData, permanent);
        }
    }

    /** The graveyard activated abilities a card in the given owner's graveyard currently offers. */
    public List<ActivatedAbility> getEffectiveGraveyardAbilities(GameData gameData, Card card, UUID ownerId) {
        synchronized (gameData) {
            return abilityActivationService.effectiveGraveyardAbilities(gameData, card, ownerId);
        }
    }

    public void setAutoStops(GameData gameData, Player player, List<TurnStep> stops) {
        if (runAsActionIfNeeded(gameData, () -> setAutoStops(gameData, player, stops))) return;
        synchronized (gameData) {
            if (gameData.status != GameStatus.RUNNING) {
                throw new IllegalStateException("Game is not running");
            }
            Set<TurnStep> stopSet = ConcurrentHashMap.newKeySet();
            stopSet.addAll(stops);
            stopSet.add(TurnStep.PRECOMBAT_MAIN);
            stopSet.add(TurnStep.POSTCOMBAT_MAIN);
            gameData.playerAutoStopSteps.put(player.getId(), stopSet);
            invalidateForAllPlayers(gameData);
        }
    }


    /**
     * Applies a player's answer to the active pending interaction. The single entry point for
     * every interaction kind: the {@link InteractionAnswer} shape identifies the payload and the
     * registry routes it to the active interaction's handler.
     */
    public void handleInteractionAnswer(GameData gameData, Player player, InteractionAnswer answer) {
        Player actionPlayer = player;
        Runnable action = () -> handleInteractionAnswer(gameData, actionPlayer, answer);
        boolean delegated = isCombatAnswer(answer)
                ? runAsCombatActionIfNeeded(gameData, action)
                : runAsActionIfNeeded(gameData, action);
        if (delegated) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player, answer)) {
                throw new IllegalStateException(
                        "Not awaiting " + answer.getClass().getSimpleName() + " input");
            }
        }
    }


    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices) {
        declareAttackers(gameData, player, attackerIndices, null, null);
    }

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        declareAttackers(gameData, player, attackerIndices, attackTargets, null);
    }

    public void declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices,
                                 Map<Integer, UUID> attackTargets, List<List<Integer>> bands) {
        Player actionPlayer = player;
        if (runAsCombatActionIfNeeded(gameData,
                () -> declareAttackers(gameData, actionPlayer, attackerIndices, attackTargets, bands))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.AttackersDeclared(attackerIndices, attackTargets, bands))) {
                return;
            }
            // No declaration is active — preserve the legacy stray-message path (the combat
            // flow rejects with "Not awaiting attacker declaration" and re-sends).
            try {
                turnProgressionService.handleCombatResult(
                        combatService.declareAttackers(
                                gameData, player, attackerIndices, attackTargets, bands),
                        gameData);
            } catch (IllegalStateException failure) {
                combatService.handleDeclareAttackersStep(gameData);
                throw failure;
            }
        }
    }

    public void declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        Player actionPlayer = player;
        if (runAsCombatActionIfNeeded(gameData,
                () -> declareBlockers(gameData, actionPlayer, blockerAssignments))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.BlockersDeclared(blockerAssignments))) {
                return;
            }
            // No declaration is active — preserve the legacy stray-message path (the combat
            // flow rejects with "Not awaiting blocker declaration").
            turnProgressionService.handleCombatResult(combatService.declareBlockers(gameData, player, blockerAssignments), gameData);
        }
    }

    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        Player actionPlayer = player;
        if (runAsCombatActionIfNeeded(gameData,
                () -> handleCombatDamageAssigned(gameData, actionPlayer, attackerIndex, assignments))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player,
                    new InteractionAnswer.CombatDamageAssigned(attackerIndex, assignments))) {
                // No assignment prompt is active — preserve the legacy stray-message path
                // (the combat flow itself rejects with "Not in combat damage assignment
                // phase" and re-sends; the legacy entry never consulted the interaction).
                try {
                    combatService.handleCombatDamageAssigned(
                            gameData, player, attackerIndex, assignments);
                } catch (IllegalStateException failure) {
                    combatService.resolveCombatDamage(gameData);
                    throw failure;
                }
                turnProgressionService.handleCombatResult(
                        combatService.resolveCombatDamage(gameData), gameData);
            }
        }
    }

    private void invalidateForAllPlayers(GameData gameData) {
        mutationCoordinator.emit(gameData,
                new GameEventFact.StateInvalidated(
                        GameEventFact.StateSection.PRIVATE_PLAYER_VIEW),
                GameEventAudience.allPlayers());
    }

    private static boolean isCombatAnswer(InteractionAnswer answer) {
        return answer instanceof InteractionAnswer.AttackersDeclared
                || answer instanceof InteractionAnswer.BlockersDeclared
                || answer instanceof InteractionAnswer.CombatDamageAssigned;
    }

}



