package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingManaActivation;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaActivation;
import com.github.laxika.magicalvibes.model.ManaPaymentIntent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
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
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GameService {
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.planar.PlanechaseService planechaseService;


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

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.planar.PlanarAbilityService planarAbilities;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.state.StateBasedActionService stateBasedActionService;

    public void activatePlanarAbility(GameData gameData, Player player, UUID sourceId, int index,
                                      Integer x, UUID targetId, com.github.laxika.magicalvibes.model.Zone zone) {
        Player actor = player;
        if (runAsActionIfNeeded(gameData, () -> activatePlanarAbility(gameData, actor, sourceId, index, x, targetId, zone))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            if (!planarAbilities.isSpecialAction(gameData, sourceId, index)) {
                requireCanActivateAbilities(gameData, player);
            }
            planarAbilities.activate(gameData, player.getId(), sourceId, index, x == null ? 0 : x, targetId, zone);
            stateBasedActionService.performStateBasedActions(gameData);
            mutationCoordinator.invalidateAllPlayerViews(gameData);
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SecretMissionService secretMissionService;

    /**
     * Special action: reveal the chosen mission of a Secret Mission conspiracy in the command zone during
     * your end step and turn it face down, which triggers its reward.
     */
    public void revealSecretMission(GameData gameData, Player player, UUID cardId) {
        Player actor = player;
        if (runAsActionIfNeeded(gameData, () -> revealSecretMission(gameData, actor, cardId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            secretMissionService.reveal(gameData, player.getId(), cardId);
            mutationCoordinator.invalidateAllPlayerViews(gameData);
        }
    }

    public void rollPlanarDie(GameData gameData, Player player) {
        Player actor = player;
        if (runAsActionIfNeeded(gameData, () -> rollPlanarDie(gameData, actor))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            planechaseService.rollSpecialAction(gameData, player.getId());
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.processNextETBTokenMultiTargetTrigger(gameData);
            }
            stateBasedActionService.performStateBasedActions(gameData);
            mutationCoordinator.invalidateAllPlayerViews(gameData);
        }
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
        // CR 117.3c: a player who acts while holding priority receives priority afterward, so
        // anchor priority on them before the action clears the recorded passes.
        gameData.priorityHolderId = player.getId();
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
            case PendingInteraction.ColorChoice cc ->
                    cc.playerId().equals(player.getId()) && cc.context() instanceof ChoiceContext.ForgottenLorePaymentChoice;
            case PendingInteraction.MultiPermanentChoice mc ->
                    mc.playerId().equals(player.getId())
                            && (mc.context() instanceof com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.FadeAwayKeep
                            || mc.context() instanceof com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.PayManaPerCreatureUntap);
            case null, default -> false;
        };
    }

    private boolean isWordOfCommandManaPayment(GameData gameData, Player player) {
        return gameData.wordOfCommandCastingCard
                && gameData.wordOfCommandControlledPlayerId != null
                && gameData.wordOfCommandControlledPlayerId.equals(player.getId())
                && gameData.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice choice
                && choice.playerId().equals(player.getId())
                && choice.manaCost() != null;
    }

    private void requireWordOfCommandLandManaSource(GameData gameData, Player player, int permanentIndex) {
        if (!isWordOfCommandManaPayment(gameData, player)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(player.getId(), List.of());
        if (permanentIndex < 0 || permanentIndex >= battlefield.size()
                || !gameQueryService.isLand(gameData, battlefield.get(permanentIndex))
                || !player.getId().equals(gameQueryService.findPermanentController(
                gameData, battlefield.get(permanentIndex).getId()))) {
            throw new IllegalStateException("Only mana abilities of lands you control may be activated");
        }
        Card chosen = gameData.playerHands.getOrDefault(player.getId(), List.of()).stream()
                .filter(card -> card.getId().equals(gameData.wordOfCommandCardId))
                .findFirst().orElse(null);
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (chosen != null && chosen.getManaCost() != null && pool != null) {
            com.github.laxika.magicalvibes.model.ManaCost cost =
                    new com.github.laxika.magicalvibes.model.ManaCost(chosen.getManaCost());
            if (!cost.hasX() && cost.canPay(pool)) {
                throw new IllegalStateException(
                        "Mana can only be produced to be spent on " + chosen.getName());
            }
        }
    }

    /**
     * the controlled player when the controlled player should be acting (has priority
     * or is the expected respondent for an interaction).
     * If the controller is acting as themselves (e.g., passing their own priority as
     * non-active player), the original player is returned.
     */
    /**
     * Rejects a decision made directly by a player whose turn another player is controlling (CR 723.1);
     * the controlling player's own request is translated to the controlled player by
     * {@link #resolveActingPlayer}.
     */
    private void requireNotControlledByAnotherPlayer(GameData gameData, Player player) {
        if (gameData.mindControllerPlayerId != null
                && player.getId().equals(gameData.mindControlledPlayerId)) {
            throw new IllegalStateException("Another player is making your decisions this turn");
        }
    }

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
            log.info("Game {} - {} passed priority on step {} (passed: {}/{})",
                    gameData.id, player.getUsername(), gameData.currentStep, gameData.priorityPassedBy.size(),
                    gameData.orderedPlayerIds.size());

            // CR 117.4: the top object resolves only once every player has passed in succession.
            if (gameData.priorityPassedBy.size() >= gameData.orderedPlayerIds.size()) {
                if (!gameData.stack.isEmpty()) {
                    stackResolutionService.resolveTopOfStack(gameData);
                    if (gameData.waitingForSubgame) return;
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
                for (CardEffect effect : permanent.getCard().getStaticEffects()) {
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

            // Each restriction is ignored separately, so a player who can't afford every unpaid one
            // pays for as many as the pool covers.
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            int payableCount = 0;
            if (pool != null) {
                for (int count = unpaidArbiterIds.size(); count >= 1; count--) {
                    if (new ManaCost("{" + (count * 2) + "}").canPayForSpecialAction(pool)) {
                        payableCount = count;
                        break;
                    }
                }
            }
            if (payableCount == 0) {
                throw new IllegalStateException("Not enough mana to pay search tax");
            }

            int totalCost = payableCount * 2;
            new ManaCost("{" + totalCost + "}").payForSpecialAction(pool);

            Set<UUID> paidSet = gameData.paidSearchTaxPermanentIds
                    .computeIfAbsent(player.getId(), k -> ConcurrentHashMap.newKeySet());
            paidSet.addAll(unpaidArbiterIds.subList(0, payableCount));

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
            if (foretellCost == null && (castingCostService == null
                    || !castingCostService.canForetellCard(gameData, player.getId(), card))) {
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
            gameData.playersWhoForetoldThisTurn.add(player.getId());
            triggerCollectionService.checkControllerForetellTriggers(gameData, player.getId(), card);
            triggerCollectionService.checkControllerCardsExiledFromHandTriggers(gameData, player.getId(), 1);
            gameData.priorityPassedBy.clear();
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " foretells ", card, "."));
            log.info("Game {} - {} foretells {}", gameData.id, player.getUsername(), card.getName());
            invalidateForAllPlayers(gameData);
        }
    }

    /** Unlocks one door of a Room as a special action during the controller's main phase. */
    public void unlockRoomDoor(GameData gameData, Player player, int permanentIndex, int doorIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> unlockRoomDoor(gameData, actionPlayer, permanentIndex, doorIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            if (!player.getId().equals(gameData.activePlayerId)
                    || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                    && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)) {
                throw new IllegalStateException("Room doors can only be unlocked during your main phase");
            }
            if (!gameData.stack.isEmpty()) {
                throw new IllegalStateException("Room doors can only be unlocked with an empty stack");
            }

            List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
            if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
                throw new IllegalArgumentException("Invalid permanent index");
            }
            Permanent room = battlefield.get(permanentIndex);
            if (!room.getCard().hasType(CardType.ENCHANTMENT)
                    || !room.getCard().getSubtypes().contains(CardSubtype.ROOM)
                    || room.getCard().getRoomDoorManaCosts().size() != 2) {
                throw new IllegalStateException("Permanent is not a Room");
            }
            if (doorIndex < 0 || doorIndex > 1) {
                throw new IllegalArgumentException("Invalid Room door index");
            }
            if (room.isRoomDoorUnlocked(doorIndex)) {
                throw new IllegalStateException("Room door is already unlocked");
            }

            ManaCost cost = castingCostService == null
                    ? new ManaCost(room.getCard().getRoomDoorManaCosts().get(doorIndex))
                    : castingCostService.getRoomUnlockCost(
                    gameData, player.getId(), room.getCard(), doorIndex);
            ManaPool pool = gameData.playerManaPools.get(player.getId());
            ManaPool.SpecialActionManaState specialActionMana = pool != null
                    ? pool.promoteSpecialActionMana() : null;
            ManaPool.EnchantmentOrRoomUnlockOrTurnFaceUpManaState restrictedMana = pool != null
                    ? pool.promoteEnchantmentOrRoomUnlockOrTurnFaceUpMana() : null;
            ManaPool.RoomSpellsOrUnlocksManaState roomMana = pool != null
                    ? pool.promoteRoomSpellsOrUnlocksMana() : null;
            try {
                if (pool == null || !cost.canPay(pool)) {
                    throw new IllegalStateException("Not enough mana to unlock that Room door");
                }
                spellCastingService.payManaCostWithPhyrexianAlternatives(
                        gameData, player.getId(), cost, pool, 0);
            } finally {
                if (roomMana != null) {
                    pool.restorePromotedRoomSpellsOrUnlocksMana(roomMana);
                }
                if (restrictedMana != null) {
                    pool.restorePromotedEnchantmentOrRoomUnlockOrTurnFaceUpMana(restrictedMana);
                }
                if (specialActionMana != null) {
                    pool.restorePromotedSpecialActionMana(specialActionMana);
                }
            }

            boolean wasFullyUnlocked = room.isRoomFullyUnlocked();
            room.unlockRoomDoor(doorIndex);
            room.setTimestamp(gameData.nextTimestamp());
            triggerCollectionService.checkSelfRoomDoorUnlockedTriggers(
                    gameData, player.getId(), room, doorIndex);
            if (!wasFullyUnlocked && room.isRoomFullyUnlocked()) {
                triggerCollectionService.checkAllyRoomFullyUnlockedTriggers(
                        gameData, player.getId(), room);
            }
            triggerCollectionService.processNextSelfTriggeredAbilityTarget(gameData);
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.processNextETBTokenMultiTargetTrigger(gameData);
            }
            gameData.priorityPassedBy.clear();
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " unlocks a door of ", room.getCard(), "."));
            log.info("Game {} - {} unlocks door {} of {}", gameData.id, player.getUsername(),
                    doorIndex, room.getCard().getName());
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
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, List.of(), List.of(), false, null);
        }
    }

    public void playCardWithGift(GameData gameData, Player player, int cardIndex, Integer xValue,
                                 UUID targetId, Map<UUID, Integer> damageAssignments,
                                 List<UUID> targetIds, boolean giftPromised) {
        playCardWithGift(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                targetIds, null, giftPromised);
    }

    public void playCardWithGift(GameData gameData, Player player, int cardIndex, Integer xValue,
                                 UUID targetId, Map<UUID, Integer> damageAssignments,
                                 List<UUID> targetIds, Integer discardHandCardIndex,
                                 boolean giftPromised) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithGift(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, discardHandCardIndex, giftPromised))) {
            return;
        }
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithGift(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds, discardHandCardIndex, giftPromised);
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
        playModalXCard(gameData, player, cardIndex, modeIndex, modalXValue, targetId, targetIds, null);
    }

    /**
     * Casts a modal {@code {X}} spell with divided-damage assignments in addition to its ordinary
     * permanent/player target list.
     */
    public void playModalXCard(GameData gameData, Player player, int cardIndex, int modeIndex, int modalXValue,
                               UUID targetId, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playModalXCard(gameData, actionPlayer, cardIndex, modeIndex, modalXValue, targetId,
                        targetIds, damageAssignments))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, modeIndex, targetId, damageAssignments,
                    targetIds, List.of(),
                    false, null, null, List.of(), null, null, false, null, modalXValue);
        }
    }

    public void playCard(GameData gameData, Player player, int cardIndex, Integer xValue, UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds, List<UUID> convokeCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                beholdChosenSubtype, chosenCreatureType,
                additionalCostSacrificePermanentIds != null && !additionalCostSacrificePermanentIds.isEmpty(),
                null, null);
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
                         CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                         UUID chosenAdditionalCostObjectId) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                beholdChosenSubtype, chosenCreatureType,
                additionalCostSacrificePermanentIds != null && !additionalCostSacrificePermanentIds.isEmpty(),
                null, chosenAdditionalCostObjectId);
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
                         CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                         boolean waterbendPaid) {
        playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds,
                convokeCreatureIds, fromGraveyard, sacrificePermanentId, phyrexianLifeCount,
                alternateCostSacrificePermanentIds, exileGraveyardCardIndex, exileGraveyardCardIndices,
                kicked, discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                beholdChosenSubtype, chosenCreatureType, waterbendPaid, null, null);
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
                         CardSubtype beholdChosenSubtype, CardSubtype chosenCreatureType,
                         boolean waterbendPaid, Boolean payLifeForAdditionalCost,
                         UUID chosenAdditionalCostObjectId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCard(gameData, actionPlayer, cardIndex, xValue, targetId, damageAssignments,
                        targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                        phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex, exileGraveyardCardIndices, kicked,
                        discardHandCardIndex, discardHandCardIndices, imposedSacrificePermanentIds,
                        additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                        beholdPermanentId, beholdHandCardIndex, beholdPermanentIds,
                        beholdHandCardIndices, beholdChosenSubtype, chosenCreatureType, waterbendPaid,
                        payLifeForAdditionalCost, chosenAdditionalCostObjectId))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCard(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                    phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex,
                    exileGraveyardCardIndices, kicked, discardHandCardIndex, discardHandCardIndices,
                    null, imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                    repeatedAdditionalCosts, buyback, beholdPermanentId, beholdHandCardIndex,
                    beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype, chosenCreatureType,
                    waterbendPaid, payLifeForAdditionalCost, chosenAdditionalCostObjectId);
        }
    }

    public void playCardWithCasualty(GameData gameData, Player player, int cardIndex, Integer xValue,
                                     UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                     List<UUID> convokeCreatureIds, boolean fromGraveyard,
                                     UUID sacrificePermanentId, Integer phyrexianLifeCount,
                                     List<UUID> alternateCostSacrificePermanentIds,
                                     Integer exileGraveyardCardIndex, List<Integer> exileGraveyardCardIndices,
                                     boolean kicked, Integer discardHandCardIndex,
                                     List<Integer> discardHandCardIndices,
                                     List<UUID> imposedSacrificePermanentIds,
                                     List<UUID> additionalCostSacrificePermanentIds,
                                     List<String> repeatedAdditionalCosts, boolean buyback,
                                     Integer sharedColorDiscardHandCardIndex,
                                     UUID beholdPermanentId, Integer beholdHandCardIndex,
                                     List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                                     CardSubtype beholdChosenSubtype, List<UUID> casualtyCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithCasualty(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, convokeCreatureIds, fromGraveyard,
                        sacrificePermanentId, phyrexianLifeCount, alternateCostSacrificePermanentIds,
                        exileGraveyardCardIndex, exileGraveyardCardIndices, kicked, discardHandCardIndex,
                        discardHandCardIndices, imposedSacrificePermanentIds,
                        additionalCostSacrificePermanentIds, repeatedAdditionalCosts, buyback,
                        sharedColorDiscardHandCardIndex, beholdPermanentId, beholdHandCardIndex,
                        beholdPermanentIds, beholdHandCardIndices, beholdChosenSubtype,
                        casualtyCreatureIds))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithCasualty(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds, convokeCreatureIds, fromGraveyard, sacrificePermanentId,
                    phyrexianLifeCount, alternateCostSacrificePermanentIds, exileGraveyardCardIndex,
                    exileGraveyardCardIndices, kicked, discardHandCardIndex, discardHandCardIndices,
                    imposedSacrificePermanentIds, additionalCostSacrificePermanentIds,
                    repeatedAdditionalCosts, buyback, sharedColorDiscardHandCardIndex,
                    beholdPermanentId, beholdHandCardIndex, beholdPermanentIds, beholdHandCardIndices,
                    beholdChosenSubtype, casualtyCreatureIds);
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
        playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId, targetIds,
                exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                damageAssignments, beholdPermanentIds, beholdHandCardIndices, discardHandCardIndices, List.of());
    }

    public void playFlashbackSpell(GameData gameData, Player player, int graveyardCardIndex, Integer xValue,
                                    UUID targetId, List<UUID> targetIds,
                                    List<Integer> exileGraveyardCardIndices, CardType chosenGraveyardType,
                                    List<UUID> tapPermanentIds, Integer retraceDiscardHandCardIndex,
                                    UUID sacrificePermanentId, List<UUID> additionalCostSacrificePermanentIds,
                                    Map<UUID, Integer> damageAssignments,
                                    List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices,
                                    List<Integer> discardHandCardIndices, List<UUID> convokeCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playFlashbackSpell(gameData, actionPlayer, graveyardCardIndex, xValue, targetId,
                        targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                        retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                        damageAssignments, beholdPermanentIds, beholdHandCardIndices,
                        discardHandCardIndices, convokeCreatureIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playFlashbackSpell(gameData, player, graveyardCardIndex, xValue, targetId,
                    targetIds, exileGraveyardCardIndices, chosenGraveyardType, tapPermanentIds,
                    retraceDiscardHandCardIndex, sacrificePermanentId, additionalCostSacrificePermanentIds,
                    damageAssignments, beholdPermanentIds, beholdHandCardIndices, discardHandCardIndices,
                    convokeCreatureIds);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
        playCardWithAlternateCost(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, null);
    }

    public void playCardWithAlternateCost(GameData gameData, Player player, int cardIndex, Integer xValue,
                                          UUID targetId, Map<UUID, Integer> damageAssignments, List<UUID> targetIds,
                                          UUID sacrificePermanentId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithAlternateCost(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, sacrificePermanentId))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithAlternateCost(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds != null ? targetIds : List.of(), null, sacrificePermanentId);
        }
    }

    public void playCardWithAdventure(GameData gameData, Player player, int cardIndex, Integer xValue,
                                      UUID targetId, Map<UUID, Integer> damageAssignments,
                                      List<UUID> targetIds) {
        playCardWithAdventure(gameData, player, cardIndex, xValue, targetId, damageAssignments, targetIds, null);
    }

    public void playCardWithAdventure(GameData gameData, Player player, int cardIndex, Integer xValue,
                                      UUID targetId, Map<UUID, Integer> damageAssignments,
                                      List<UUID> targetIds, UUID sacrificePermanentId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithAdventure(gameData, actionPlayer, cardIndex, xValue, targetId,
                        damageAssignments, targetIds, sacrificePermanentId))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithAdventure(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds != null ? targetIds : List.of(), sacrificePermanentId);
        }
    }

    public void playAdventureCard(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, List<UUID> targetIds) {
        playAdventureCard(gameData, player, cardIndex, xValue, targetId, targetIds, null);
    }

    public void playAdventureCard(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, List<UUID> targetIds,
                                  Map<UUID, Integer> damageAssignments) {
        playAdventureCard(gameData, player, cardIndex, xValue, targetId, targetIds, damageAssignments, null);
    }

    public void playAdventureCard(GameData gameData, Player player, int cardIndex, Integer xValue,
                                  UUID targetId, List<UUID> targetIds,
                                  Map<UUID, Integer> damageAssignments, UUID sacrificePermanentId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playAdventureCard(gameData, actionPlayer, cardIndex, xValue, targetId, targetIds,
                        damageAssignments, sacrificePermanentId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playAdventureCard(gameData, player, cardIndex, xValue, targetId,
                    targetIds != null ? targetIds : List.of(), damageAssignments, sacrificePermanentId);
        }
    }

    public void playAdventureCardFromGraveyard(GameData gameData, Player player, int cardIndex,
                                                Integer xValue, UUID targetId, List<UUID> targetIds,
                                                Map<UUID, Integer> damageAssignments) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playAdventureCardFromGraveyard(gameData, actionPlayer, cardIndex, xValue,
                        targetId, targetIds, damageAssignments))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playAdventureCardFromGraveyard(gameData, player, cardIndex, xValue,
                    targetId, targetIds != null ? targetIds : List.of(), damageAssignments);
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
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithMorph(gameData, player, cardIndex, xValue, targetId,
                    damageAssignments, targetIds != null ? targetIds : List.of(), revealedHandCardIndex);
        }
    }

    /** Casts a face-down spellmorph card from the battlefield. */
    public void playCardWithSpellmorph(GameData gameData, Player player, int permanentIndex,
                                       Integer xValue, UUID targetId, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardWithSpellmorph(gameData, actionPlayer, permanentIndex, xValue,
                        targetId, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
            if (battlefield == null || permanentIndex < 0 || permanentIndex >= battlefield.size()) {
                throw new IllegalArgumentException("Invalid permanent index");
            }
            spellCastingService.playCardWithSpellmorph(gameData, player, battlefield.get(permanentIndex),
                    xValue, targetId, targetIds != null ? targetIds : List.of());
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
        // Turning a permanent face up is a special action; state-based actions are checked before
        // the player next receives priority (CR 704.3), e.g. an aura it no longer may enchant falls off.
        // While a face-up choice is still pending (Vesuvan Shapeshifter's copy choice), the permanent is not
        // yet in its final state, so the check waits until the choice completes.
        if (!gameData.interaction.isAwaitingInput()) {
            stateBasedActionService.performStateBasedActions(gameData);
        }
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
            stateBasedActionService.performStateBasedActions(gameData);
        }
    }

    /**
     * Turns a face-down permanent face up again after its morph cost stopped to ask the death replacements of the
     * permanents it sacrifices (CR 616.1); the sacrifices consume the answers.
     */
    public void completeTurnFaceUpAfterCostSacrificeAnswers(GameData gameData, GameData.TurnFaceUpReplay replay) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(replay.player().getId());
        Permanent permanent = gameQueryService.findPermanentById(gameData, replay.permanentId());
        if (battlefield == null || permanent == null || !battlefield.contains(permanent)) {
            throw new IllegalStateException("Permanent is no longer controlled by the player");
        }
        turnFaceUpInternal(gameData, replay.player(), battlefield.indexOf(permanent), replay.revealedHandCardIndex(),
                replay.costPermanentIds(), replay.xValue(), true, replay.payMorphCost());
        if (!gameData.interaction.isAwaitingInput()) {
            stateBasedActionService.performStateBasedActions(gameData);
        }
    }

    private void turnFaceUpInternal(GameData gameData, Player player, int permanentIndex,
                                     Integer revealedHandCardIndex, List<UUID> morphAdditionalCostPermanentIds,
                                     Integer xValue,
                                     boolean completingXChoice) {
        turnFaceUpInternal(gameData, player, permanentIndex, revealedHandCardIndex,
                morphAdditionalCostPermanentIds, xValue, completingXChoice, null);
    }

    /** Completes the choice of a manifested or cloaked creature's face-up payment method. */
    public void completeTurnFaceUpCostChoice(GameData gameData, Player player, UUID permanentId,
                                             boolean payMorphCost) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> completeTurnFaceUpCostChoice(gameData, actionPlayer, permanentId, payMorphCost))) return;
        synchronized (gameData) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (battlefield == null || permanent == null || !battlefield.contains(permanent)) {
                throw new IllegalStateException("Permanent is no longer controlled by the player");
            }
            turnFaceUpInternal(gameData, player, battlefield.indexOf(permanent), null,
                    List.of(), null, true, payMorphCost);
        }
    }

    private void turnFaceUpInternal(GameData gameData, Player player, int permanentIndex,
                                     Integer revealedHandCardIndex, List<UUID> morphAdditionalCostPermanentIds,
                                     Integer xValue, boolean completingXChoice, Boolean payMorphCost) {
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
            boolean manifestedOrCloaked = permanent.isManifested() || cloaked;
            String morphCost = permanent.getCard().getMorphCost();
            if (manifestedOrCloaked && morphCost != null) {
                boolean normalPayable = permanent.getCard().hasType(CardType.CREATURE)
                        && canPayTurnFaceUpManaCost(gameData, player.getId(), permanent, false);
                boolean morphPayable = canPayTurnFaceUpManaCost(gameData, player.getId(), permanent, true);
                if (payMorphCost == null && normalPayable && morphPayable) {
                    interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                            player.getId(), permanent.getId(), null,
                            new ChoiceContext.TurnFaceUpCostChoice(permanent.getId()),
                            List.of("Mana cost", "Morph cost"), "Choose how to turn the creature face up."));
                    return;
                }
                if (Boolean.TRUE.equals(payMorphCost) || (payMorphCost == null && !normalPayable && morphPayable)) {
                    manifestedOrCloaked = false;
                }
            }
            String faceUpCost = manifestedOrCloaked ? permanent.getCard().getManaCost() : morphCost;
            if (manifestedOrCloaked && !permanent.getCard().hasType(CardType.CREATURE)) {
                throw new IllegalStateException("Face-down permanent is not a creature card");
            }
            if (!manifestedOrCloaked && permanent.getCard().isSpellmorph()) {
                throw new IllegalStateException("Spellmorph cards are cast from the battlefield instead");
            }
            if ((!manifestedOrCloaked && morphCost == null) || (manifestedOrCloaked && faceUpCost == null)
                    || (!manifestedOrCloaked && (permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.computeStaticBonus(gameData, permanent).losesAllAbilities()))) {
                throw new IllegalStateException("Permanent cannot be turned face up");
            }
            List<UUID> additionalCostPermanentIds = morphAdditionalCostPermanentIds != null
                    ? morphAdditionalCostPermanentIds : List.of();
            ReturnPermanentsCost morphAdditionalCost = permanent.getCard().getMorphAdditionalCost();
            if (!manifestedOrCloaked && morphAdditionalCost != null) {
                spellCastingService.validateMorphAdditionalCost(
                        gameData, player, morphAdditionalCost, additionalCostPermanentIds);
            }
            SacrificePermanentsCost morphSacrificeCost = permanent.getCard().getMorphSacrificeCost();
            if (!manifestedOrCloaked && morphSacrificeCost != null) {
                spellCastingService.validateMorphSacrificeCost(
                        gameData, player, permanent.getId(), morphSacrificeCost, additionalCostPermanentIds);
            }
            DiscardCardTypeCost morphDiscardCost = permanent.getCard().getMorphDiscardCost();
            if (!manifestedOrCloaked && morphDiscardCost != null) {
                spellCastingService.validateMorphDiscardCost(
                        gameData, player, permanent.getCard(), morphDiscardCost, revealedHandCardIndex);
            }
            LifeCastingCost morphLifeCost = permanent.getCard().getMorphLifeCost();
            if (!manifestedOrCloaked && morphLifeCost != null) {
                spellCastingService.validateMorphLifeCost(gameData, player, morphLifeCost);
            }
            if (spellCastingService.askTurnFaceUpLifeCostsFirst(gameData, player,
                    manifestedOrCloaked ? null : morphLifeCost,
                    !manifestedOrCloaked && permanent.getCard().getMorphDiscardCost() != null ? null : faceUpCost,
                    xValue == null ? 0 : xValue)) {
                gameData.pendingDeathReplacementChoiceReplay = new GameData.TurnFaceUpReplay(player,
                        permanent.getId(), revealedHandCardIndex, List.copyOf(additionalCostPermanentIds),
                        xValue, payMorphCost);
                mutationCoordinator.invalidateAllPlayerViews(gameData);
                return;
            }
            RevealCardsFromHandCastingCost morphRevealCost = permanent.getCard().getMorphRevealCost();
            if (!manifestedOrCloaked && morphRevealCost != null) {
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
            } else if (manifestedOrCloaked || morphDiscardCost == null
                    || discardMorphCarriesCostIncrease(gameData, player.getId(), permanent)) {
                // A discard-cost morph has no mana of its own; only a cost increase (Exiled Doomsayer)
                // is paid here, as generic mana alongside the discard.
                ManaCost cost = new ManaCost(!manifestedOrCloaked && morphDiscardCost != null ? "{0}" : faceUpCost);
                int morphCostModifier = 0;
                DynamicAmount morphCostReduction = permanent.getCard().getMorphCostReduction();
                if (!manifestedOrCloaked && morphCostReduction != null && amountEvaluationService != null) {
                    int reduction = amountEvaluationService.evaluate(gameData, morphCostReduction,
                            AmountContext.forCasting(player.getId()));
                    morphCostModifier -= reduction;
                }
                if (castingCostService != null) {
                    morphCostModifier += castingCostService.getTurnFaceUpCostModifier(
                            gameData, player.getId(), permanent.getCard(), permanent.getId());
                    if (!manifestedOrCloaked && !permanent.isFaceDownWard()) {
                        // Disguise is a separate keyword from morph, so "morph costs" effects leave it alone.
                        morphCostModifier += castingCostService.getMorphCostModifier(
                                gameData, player.getId(), permanent.getCard());
                    }
                }
                if (morphCostModifier > 0) {
                    cost = cost.increasedBy(new ManaCost("{" + morphCostModifier + "}"));
                } else if (morphCostModifier < 0) {
                    cost = cost.reducedBy(new ManaCost("{" + -morphCostModifier + "}"));
                }
                if (castingCostService != null) {
                    cost = castingCostService.applyManaCostPaymentAlternatives(
                            gameData, player.getId(), cost);
                }
                ManaPool pool = gameData.playerManaPools.get(player.getId());
                if (pool == null) {
                    pool = new ManaPool();
                }
                ManaPool.SpecialActionManaState specialActionMana = pool.promoteSpecialActionMana();
                ManaPool.FaceDownSpellsOrTurnFaceUpManaState restrictedMana =
                        restrictedManaPaysTurnFaceUp(permanent, !manifestedOrCloaked)
                                ? pool.promoteFaceDownSpellsOrTurnFaceUpMana() : pool.promoteTurnCreaturesFaceUpMana();
                ManaPool.EnchantmentOrRoomUnlockOrTurnFaceUpManaState enchantmentOrRoomUnlockOrTurnFaceUpMana =
                        pool.promoteEnchantmentOrRoomUnlockOrTurnFaceUpMana();
                ManaPool.TurnPermanentsFaceUpManaState turnPermanentsFaceUpMana =
                        pool.promoteTurnPermanentsFaceUpMana();
                try {
                    if (manifestedOrCloaked && cost.hasX() && xValue == null) {
                        // CR 107.3g: a card off the stack treats the {X} in its mana cost as 0, so a manifested or
                        // cloaked creature turned face up for its mana cost never asks for X.
                        xValue = 0;
                    }
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
                    if (!manifestedOrCloaked && morphSacrificeCost != null
                            && spellCastingService.askMorphSacrificeCostFirst(
                            gameData, player, additionalCostPermanentIds)) {
                        gameData.pendingDeathReplacementChoiceReplay = new GameData.TurnFaceUpReplay(player,
                                permanent.getId(), revealedHandCardIndex, List.copyOf(additionalCostPermanentIds),
                                xValue, payMorphCost);
                        mutationCoordinator.invalidateAllPlayerViews(gameData);
                        return;
                    }
                    spellCastingService.payManaCostWithPhyrexianAlternatives(
                            gameData, player.getId(), cost, pool, effectiveXValue);
                } finally {
                    pool.restorePromotedTurnPermanentsFaceUpMana(turnPermanentsFaceUpMana);
                    pool.restorePromotedEnchantmentOrRoomUnlockOrTurnFaceUpMana(
                            enchantmentOrRoomUnlockOrTurnFaceUpMana);
                    if (restrictedMana != null) {
                        pool.restorePromotedFaceDownSpellsOrTurnFaceUpMana(restrictedMana);
                    }
                    pool.restorePromotedSpecialActionMana(specialActionMana);
                }
            }
            if (!manifestedOrCloaked && morphAdditionalCost != null) {
                spellCastingService.payMorphAdditionalCost(
                        gameData, player, permanent.getCard(), morphAdditionalCost, additionalCostPermanentIds);
            }
            if (!manifestedOrCloaked && morphSacrificeCost != null) {
                spellCastingService.payMorphSacrificeCost(
                        gameData, player, permanent.getId(), permanent.getCard(), morphSacrificeCost,
                        additionalCostPermanentIds);
            }
            if (!manifestedOrCloaked && morphDiscardCost != null) {
                spellCastingService.payMorphDiscardCost(
                        gameData, player, permanent.getCard(), morphDiscardCost, revealedHandCardIndex);
            }
            if (!manifestedOrCloaked && morphLifeCost != null) {
                spellCastingService.payMorphLifeCost(gameData, player, permanent.getCard(), morphLifeCost);
            }
            finishTurningFaceUp(gameData, permanent, player.getId(), xValue, !manifestedOrCloaked, !manifestedOrCloaked);
        }
    }

    private boolean discardMorphCarriesCostIncrease(GameData gameData, UUID playerId, Permanent permanent) {
        if (castingCostService == null || permanent.isManifested() || permanent.isCloaked()
                || permanent.isFaceDownWard()) {
            return false;
        }
        return castingCostService.getTurnFaceUpCostModifier(
                gameData, playerId, permanent.getCard(), permanent.getId())
                + castingCostService.getMorphCostModifier(gameData, playerId, permanent.getCard()) > 0;
    }

    private boolean canPayTurnFaceUpManaCost(GameData gameData, UUID playerId, Permanent permanent,
                                             boolean morphPayment) {
        String manaCost = morphPayment ? permanent.getCard().getMorphCost() : permanent.getCard().getManaCost();
        if (manaCost == null) return false;
        ManaCost cost = new ManaCost(manaCost);
        int modifier = 0;
        if (morphPayment && permanent.getCard().getMorphCostReduction() != null && amountEvaluationService != null) {
            modifier -= amountEvaluationService.evaluate(gameData, permanent.getCard().getMorphCostReduction(),
                    AmountContext.forCasting(playerId));
        }
        if (castingCostService != null) {
            modifier += castingCostService.getTurnFaceUpCostModifier(
                    gameData, playerId, permanent.getCard(), permanent.getId());
            if (morphPayment) {
                modifier += castingCostService.getMorphCostModifier(gameData, playerId, permanent.getCard());
            }
        }
        if (modifier > 0) cost = cost.increasedBy(new ManaCost("{" + modifier + "}"));
        if (modifier < 0) cost = cost.reducedBy(new ManaCost("{" + -modifier + "}"));
        if (castingCostService != null) {
            cost = castingCostService.applyManaCostPaymentAlternatives(gameData, playerId, cost);
        }
        ManaPool actualPool = gameData.playerManaPools.get(playerId);
        ManaPool pool = actualPool == null ? new ManaPool() : new ManaPool(actualPool);
        pool.promoteSpecialActionMana();
        if (restrictedManaPaysTurnFaceUp(permanent, morphPayment)) {
            pool.promoteFaceDownSpellsOrTurnFaceUpMana();
        } else {
            pool.promoteTurnCreaturesFaceUpMana();
        }
        pool.promoteEnchantmentOrRoomUnlockOrTurnFaceUpMana();
        pool.promoteTurnPermanentsFaceUpMana();
        return cost.canPay(pool, 0);
    }

    /**
     * Whether mana restricted to face-down spells or turning face up by a morph cost (the narrow variant,
     * not mana that may turn any creature face up) may pay the cost being used to turn
     * this permanent face up. A morph cost qualifies unless the permanent is disguised; a manifested creature's
     * mana cost qualifies; a cloaked creature's mana cost does not.
     */
    private boolean restrictedManaPaysTurnFaceUp(Permanent permanent, boolean payingMorphCost) {
        if (permanent.isFaceDownWard()) {
            return false;
        }
        return payingMorphCost || permanent.isManifested();
    }

    /** Turns a targeted face-down creature face up without using its morph or disguise action. */
    public void turnPermanentFaceUpWithoutPayingManaCost(GameData gameData, Permanent permanent) {
        if (runAsActionIfNeeded(gameData,
                () -> turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent, FaceUpTypeCheck.EFFECTIVE_CREATURE))) return;
        turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent, FaceUpTypeCheck.EFFECTIVE_CREATURE);
    }

    /** Turns a face-down permanent face up without paying its mana cost, whatever its printed card type. */
    public void turnPermanentFaceUpWithoutPayingManaCostOfAnyType(GameData gameData, Permanent permanent) {
        if (runAsActionIfNeeded(gameData,
                () -> turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent, FaceUpTypeCheck.NONE))) return;
        turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent, FaceUpTypeCheck.NONE);
    }

    /** Turns a face-down permanent face up when its printed card is a creature. */
    public void turnPermanentFaceUpIfCreatureCardWithoutPayingManaCost(GameData gameData, Permanent permanent) {
        if (runAsActionIfNeeded(gameData,
                () -> turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent,
                        FaceUpTypeCheck.ORIGINAL_CREATURE))) return;
        turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent, FaceUpTypeCheck.ORIGINAL_CREATURE);
    }

    private enum FaceUpTypeCheck { EFFECTIVE_CREATURE, ORIGINAL_CREATURE, NONE }

    private void turnPermanentFaceUpWithoutPayingManaCost(GameData gameData, Permanent permanent,
                                                           FaceUpTypeCheck typeCheck) {
        synchronized (gameData) {
            if (permanent == null || !permanent.isFaceDown()
                    || !faceUpTypeCheckPasses(gameData, permanent, typeCheck)
                    || gameQueryService.isTurnFaceUpPrevented(gameData, permanent)) {
                return;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId == null) {
                return;
            }
            finishTurningFaceUp(gameData, permanent, controllerId, null, false, false);
        }
    }

    private boolean faceUpTypeCheckPasses(GameData gameData, Permanent permanent, FaceUpTypeCheck typeCheck) {
        return switch (typeCheck) {
            case EFFECTIVE_CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ORIGINAL_CREATURE -> permanent.getOriginalCard().hasType(CardType.CREATURE);
            case NONE -> true;
        };
    }

    private void finishTurningFaceUp(GameData gameData, Permanent permanent, UUID controllerId,
                                     Integer xValue, boolean autoPass, boolean paidTurnFaceUpCost) {
        permanent.turnFaceUp();
        gameData.playersWhoTurnedPermanentsFaceUpThisTurn.add(controllerId);
        gameData.permanentsTurnedFaceUpThisTurn.add(permanent.getId());
        List<TurnFaceUpReplacementEffect> replacements = permanent.getCard()
                .getEffects(EffectSlot.ON_TURNED_FACE_UP).stream()
                .filter(TurnFaceUpReplacementEffect.class::isInstance)
                .map(TurnFaceUpReplacementEffect.class::cast)
                .toList();
        for (TurnFaceUpReplacementEffect replacement : replacements) {
            if (!paidTurnFaceUpCost && !replacement.appliesWithoutPayingCost()) {
                continue;
            }
            int counterCount = amountEvaluationService.evaluate(gameData, replacement.counterAmount(),
                    AmountContext.forEnteringPermanent(controllerId, permanent, xValue != null ? xValue : 0));
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, null, permanent, replacement.counterType(), counterCount);
        }
        if (turnFaceUpCopyService != null
                && turnFaceUpCopyService.prepareChoice(gameData, permanent, controllerId)) {
            return;
        }
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is turned face up."));

        if (triggerCollectionService != null) {
            triggerCollectionService.checkSelfOrAnyPermanentTurnsFaceUpTriggers(
                    gameData, controllerId, permanent);
            triggerCollectionService.checkSelfOrAllyPermanentTurnsFaceUpTriggers(
                    gameData, controllerId, permanent);
            triggerCollectionService.checkSelfOrAllyCreatureTurnsFaceUpTriggers(
                    gameData, controllerId, permanent);
            triggerCollectionService.checkGraveyardAllyPermanentTurnsFaceUpTriggers(
                    gameData, controllerId, permanent);
        }

        List<CardEffect> effects = new java.util.ArrayList<>(
                permanent.getCard().getEffects(EffectSlot.ON_TURNED_FACE_UP));
        effects.addAll(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_TURNED_FACE_UP));
        List<CardEffect> grantedEffects = new java.util.ArrayList<>(
                permanent.getPersistentTriggeredEffects(EffectSlot.ON_TURNED_FACE_UP));
        effects.addAll(grantedEffects);
        if (gameQueryService.hasLostAllAbilities(gameData, permanent)) {
            effects.clear();
            grantedEffects.clear();
        }
        effects = effects.stream()
                .filter(effect -> !(effect instanceof com.github.laxika.magicalvibes.model.effect.ReplacementEffect))
                .filter(effect -> turnedFaceUpTriggerConditionIsMet(gameData, permanent, controllerId, effect))
                .toList();
        List<CardEffect> triggeringEffects = effects;
        grantedEffects.removeIf(effect -> !triggeringEffects.contains(effect));
        if (!effects.isEmpty()) {
            int triggerCopies = 1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                    gameData, controllerId, permanent, false,
                    new TriggerContext.PermanentTurnsFaceUp(permanent, controllerId));
            if (effects.size() == 1 && effects.getFirst() instanceof ChooseOneEffect modal) {
                for (int copy = 0; copy < triggerCopies; copy++) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            permanent.getCard(), controllerId, modal, permanent.getId()));
                }
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
            boolean targetsGraveyard = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
            boolean targetsPlayer = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean targetsPermanent = effects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (targetsGraveyard) {
                int minimumGraveyardTargets = effects.stream()
                        .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                        .anyMatch(effect -> !effect.hasOptionalTarget()) ? 1 : 0;
                for (int copy = 0; copy < triggerCopies; copy++) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            permanent.getCard(), controllerId, effects, null,
                            minimumGraveyardTargets, xValue != null ? xValue : 0));
                }
                triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
                if (autoPass) {
                    turnProgressionService.resolveAutoPass(gameData);
                }
                return;
            } else if (targetsSpell) {
                StackEntryPredicate spellFilter = null;
                boolean includeAbilities = false;
                if (permanent.getCard().getTargetFilter() instanceof StackEntryPredicateTargetFilter filter) {
                    spellFilter = filter.predicate();
                    includeAbilities = TriggerCollectionService.predicateContainsHasTarget(filter.predicate());
                }
                for (int copy = 0; copy < triggerCopies; copy++) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                            permanent.getCard(), controllerId, effects, spellFilter, includeAbilities,
                            permanent.getId()));
                }
            } else if (targetsPlayer || targetsPermanent
                    || effects.stream().anyMatch(permanent.getCard()::hasEffectTargetIndex)) {
                boolean multiTarget = permanent.getCard().getTargetGroups().size() > 1
                        || permanent.getCard().getTargetGroups().stream()
                        .anyMatch(group -> group.getMaxTargets() > 1 || group.getMinTargets() == 0
                                || group.getDynamicMinTargets() != null);
                if (multiTarget) {
                    for (int copy = 0; copy < triggerCopies; copy++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                permanent.getCard(), controllerId, effects, permanent.getId(), List.of(), 0, 0));
                    }
                } else {
                    for (int copy = 0; copy < triggerCopies; copy++) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                permanent.getCard(), controllerId, effects,
                                !targetsPermanent, permanent.getCard().getTargetFilter(), 0, permanent.getId()));
                    }
                }
            } else {
                List<List<CardEffect>> abilities = new java.util.ArrayList<>();
                List<CardEffect> ownEffects = new java.util.ArrayList<>(effects);
                grantedEffects.forEach(ownEffects::remove);
                if (!ownEffects.isEmpty()) {
                    abilities.add(ownEffects);
                }
                grantedEffects.forEach(granted -> abilities.add(List.of(granted)));
                for (List<CardEffect> ability : abilities) {
                    for (int copy = 0; copy < triggerCopies; copy++) {
                        gameData.stack.add(new com.github.laxika.magicalvibes.model.StackEntry(
                                com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                                permanent.getCard(), controllerId, permanent.getCard().getName() + "'s ability",
                                ability, xValue != null ? xValue : 0, permanent.getId()));
                    }
                }
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
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
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardWithSplice(gameData, player, cardIndex, xValue, targetId, damageAssignments,
                    targetIds != null ? targetIds : List.of(),
                    spliceHandCardIndices != null ? spliceHandCardIndices : List.of(),
                    splicePermanentIds != null ? splicePermanentIds : List.of());
        }
    }

    public void castCommander(GameData gameData, Player player, UUID cardId, Runnable cast) {
        if (runAsActionIfNeeded(gameData, () -> castCommander(gameData, player, cardId, cast))) return;
        synchronized (gameData) {
            Player actor = resolveActingPlayer(gameData, player);
            requirePriority(gameData, actor);
            if (gameData.commandCastCardId != null) throw new IllegalStateException("Already casting a commander");
            List<Card> zone = gameData.playerCommandZones.getOrDefault(actor.getId(), List.of());
            if (zone.size() != 1 || !zone.getFirst().getId().equals(cardId) || !gameData.isCommander(cardId))
                throw new IllegalArgumentException("Commander is not in your command zone");
            gameData.commandCastPlayerId = actor.getId();
            gameData.commandCastCardId = cardId;
            try { cast.run(); }
            finally { gameData.commandCastPlayerId = null; gameData.commandCastCardId = null; }
        }
    }

    /**
     * Runs {@code play} — any hand cast or land-play call — against {@code handOwnerId}'s hand instead of the
     * acting player's own, while Sen Triplets lets that player play lands and cast spells from it this turn.
     */
    public void playFromHandOf(GameData gameData, Player player, UUID handOwnerId, Runnable play) {
        if (runAsActionIfNeeded(gameData, () -> playFromHandOf(gameData, player, handOwnerId, play))) return;
        synchronized (gameData) {
            Player actor = resolveActingPlayer(gameData, player);
            requirePriority(gameData, actor);
            spellCastingService.playFromHandOf(gameData, actor, handOwnerId, play);
        }
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue, UUID targetId) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId, List.of());
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, boolean flashforwardCast) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardFromExile(gameData, actionPlayer, exileCardId, xValue, targetId,
                        flashforwardCast))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                    flashforwardCast);
        }
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                exileCounterCostPermanentIds, List.of());
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds,
                                  List<UUID> convokeCreatureIds) {
        playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                exileCounterCostPermanentIds, convokeCreatureIds, List.of(), false);
    }

    public void playCardFromExile(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                  UUID targetId, List<UUID> exileCounterCostPermanentIds,
                                  List<UUID> convokeCreatureIds, List<UUID> waterbendPermanentIds,
                                  boolean waterbendPaid) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardFromExile(gameData, actionPlayer, exileCardId, xValue, targetId,
                        exileCounterCostPermanentIds, convokeCreatureIds, waterbendPermanentIds,
                        waterbendPaid))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromExile(gameData, player, exileCardId, xValue, targetId,
                    exileCounterCostPermanentIds != null ? exileCounterCostPermanentIds : List.of(),
                    convokeCreatureIds != null ? convokeCreatureIds : List.of(),
                    waterbendPermanentIds != null ? waterbendPermanentIds : List.of(), waterbendPaid);
        }
    }

    public void playCardFromExileWithTargets(GameData gameData, Player player, UUID exileCardId, Integer xValue,
                                             UUID targetId, List<UUID> targetIds,
                                             List<UUID> exileCounterCostPermanentIds,
                                             List<UUID> convokeCreatureIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardFromExileWithTargets(gameData, actionPlayer, exileCardId, xValue, targetId,
                        targetIds, exileCounterCostPermanentIds, convokeCreatureIds))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromExileWithTargets(gameData, player, exileCardId, xValue, targetId,
                    targetIds != null ? targetIds : List.of(),
                    exileCounterCostPermanentIds != null ? exileCounterCostPermanentIds : List.of(),
                    convokeCreatureIds != null ? convokeCreatureIds : List.of());
        }
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId) {
        playCardFromLibraryTop(gameData, player, xValue, targetId, List.of());
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId,
                                       List<UUID> counterCostPermanentIds) {
        playCardFromLibraryTop(gameData, player, xValue, targetId, counterCostPermanentIds, List.of());
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId,
                                       List<UUID> counterCostPermanentIds,
                                       List<UUID> additionalCostSacrificePermanentIds) {
        playCardFromLibraryTop(gameData, player, xValue, targetId, counterCostPermanentIds,
                additionalCostSacrificePermanentIds, false);
    }

    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId,
                                       List<UUID> counterCostPermanentIds,
                                       List<UUID> additionalCostSacrificePermanentIds, boolean castFaceDown) {
        playCardFromLibraryTop(gameData, player, xValue, targetId, counterCostPermanentIds,
                additionalCostSacrificePermanentIds, castFaceDown, false);
    }

    /** Casts the top library card, as its Adventure when {@code castAsAdventure} is set. */
    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId,
                                       List<UUID> counterCostPermanentIds,
                                       List<UUID> additionalCostSacrificePermanentIds, boolean castFaceDown,
                                       boolean castAsAdventure) {
        playCardFromLibraryTop(gameData, player, xValue, targetId, counterCostPermanentIds,
                additionalCostSacrificePermanentIds, castFaceDown, castAsAdventure, null);
    }

    /**
     * Plays the top card of {@code libraryOwnerId}'s library, or of the first library open to the player when it is
     * {@code null}; as its Adventure when {@code castAsAdventure} is set.
     */
    public void playCardFromLibraryTop(GameData gameData, Player player, Integer xValue, UUID targetId,
                                       List<UUID> counterCostPermanentIds,
                                       List<UUID> additionalCostSacrificePermanentIds, boolean castFaceDown,
                                       boolean castAsAdventure, UUID libraryOwnerId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> playCardFromLibraryTop(gameData, actionPlayer, xValue, targetId,
                        counterCostPermanentIds, additionalCostSacrificePermanentIds, castFaceDown,
                        castAsAdventure, libraryOwnerId))) return;
        synchronized (gameData) {
            requireNotControlledByAnotherPlayer(gameData, player);
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            spellCastingService.playCardFromLibraryTop(gameData, player, xValue, targetId,
                    counterCostPermanentIds != null ? counterCostPermanentIds : List.of(),
                    additionalCostSacrificePermanentIds != null ? additionalCostSacrificePermanentIds : List.of(),
                    castFaceDown, castAsAdventure, libraryOwnerId);
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
            boolean manaPayment = isCombatCostManaPayment(gameData, player) || isMayCostManaPayment(gameData, player);
            if (!manaPayment) {
                requirePriority(gameData, player);
            }
            requireWordOfCommandLandManaSource(gameData, player, permanentIndex);
            requireCanActivateAbilities(gameData, player);
            Player tappingPlayer = player;
            Runnable activation = () -> abilityActivationService.tapPermanent(gameData, tappingPlayer, permanentIndex);
            if (manaPayment) {
                activateDuringManaPayment(gameData, activation);
            } else {
                int revertableBefore = gameData.revertableManaActivations.size();
                PendingManaActivation parkedBefore = gameData.pendingRevertableManaActivation;
                activation.run();
                finishPriorityManaActivation(gameData, revertableBefore, parkedBefore, paymentIntent);
            }
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
            if (!isCombatCostManaPayment(gameData, player) && !isMayCostManaPayment(gameData, player)) {
                requirePriority(gameData, player);
            }
            abilityActivationService.payLifeForColorlessMana(gameData, player);
        }
    }

    /**
     * Special action (CR 116.2c): pays an "unless [that player] pays [cost] before that step"
     * obligation of a delayed triggered ability, such as Quenchable Fire's or Nafs Asp's, so it won't
     * trigger.
     */
    public void payDelayedTriggerCost(GameData gameData, Player player, UUID sourceCardId, UUID affectedId) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> payDelayedTriggerCost(gameData, actionPlayer, sourceCardId, affectedId))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.payDelayedTriggerCost(gameData, player, sourceCardId, affectedId);
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

    public void activateEmblemAbility(GameData gameData, Player player, int emblemIndex, Integer abilityIndex,
                                      Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds,
                                      Map<UUID, Integer> damageAssignments) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateEmblemAbility(gameData, actionPlayer, emblemIndex, abilityIndex, xValue,
                        targetId, targetZone, targetIds, damageAssignments))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            abilityActivationService.activateEmblemAbility(gameData, player, emblemIndex, abilityIndex, xValue,
                    targetId, targetZone, targetIds, damageAssignments);
        }
    }

    /**
     * @param paymentIntent what the player is activating this mana ability for, so an "any colour"
     *                      prompt can grey out the colours that would strand it; {@code null} when
     *                      unknown or when the ability is not being activated to pay for something.
     */
    public void activateAbility(GameData gameData, Player player, int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments, ManaPaymentIntent paymentIntent) {
        activateAbility(gameData, player, permanentIndex, null, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments, paymentIntent);
    }

    /**
     * @param sourcePermanentId when non-null, the permanent that owns the ability, looked up by ID instead of
     *                          {@code permanentIndex}; lets a player activate another player's "any player may
     *                          activate" ability unambiguously.
     */
    public void activateAbility(GameData gameData, Player player, int permanentIndex, UUID sourcePermanentId, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments, ManaPaymentIntent paymentIntent) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateAbility(gameData, actionPlayer, permanentIndex, sourcePermanentId, abilityIndex, xValue,
                        targetId, targetZone, targetIds, damageAssignments, paymentIntent))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            Permanent idSource = null;
            if (sourcePermanentId != null) {
                idSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (idSource == null) {
                    throw new IllegalStateException("Invalid permanent");
                }
            }
            boolean normalPriorityActivation = !isAttackTaxManaPayment(gameData, player)
                    && !isBlockCostManaPayment(gameData, player) && !isMayCostManaPayment(gameData, player);
            boolean manaActivation = idSource != null
                    ? abilityActivationService.isManaAbilityOf(gameData, idSource, abilityIndex)
                    : abilityActivationService.isManaAbilityAt(gameData, player.getId(), permanentIndex, abilityIndex);
            if (isAttackTaxManaPayment(gameData, player)) {
                // CR 508.1i: only mana abilities allowed during attacker declaration
                if (!manaActivation) {
                    throw new IllegalStateException("Only mana abilities can be activated during attacker declaration");
                }
            } else if (isBlockCostManaPayment(gameData, player)) {
                // CR 509.1e: only mana abilities allowed during blocker declaration
                if (!manaActivation) {
                    throw new IllegalStateException("Only mana abilities can be activated during blocker declaration");
                }
            } else if (isMayCostManaPayment(gameData, player)) {
                if (!manaActivation) {
                    throw new IllegalStateException("Only mana abilities can be activated while paying a cost");
                }
                if (idSource != null) throw new IllegalStateException("Only mana abilities of lands you control may be activated");
                requireWordOfCommandLandManaSource(gameData, player, permanentIndex);
            } else {
                requirePriority(gameData, player);
            }
            Player activatingPlayer = player;
            Permanent activationSource = idSource;
            Runnable activation = activationSource != null
                    ? () -> abilityActivationService.activateAbility(gameData, activatingPlayer,
                            activationSource, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments)
                    : () -> abilityActivationService.activateAbility(gameData, activatingPlayer,
                            permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments);
            int revertableBefore = gameData.revertableManaActivations.size();
            PendingManaActivation parkedBefore = gameData.pendingRevertableManaActivation;
            if (normalPriorityActivation) {
                activation.run();
            } else {
                activateDuringManaPayment(gameData, activation);
            }
            manaChoiceNarrowingService.narrowActiveManaColorChoice(gameData, player.getId(), paymentIntent);
            if (normalPriorityActivation && manaActivation) {
                finishPriorityManaActivation(gameData, revertableBefore, parkedBefore, paymentIntent);
            }
        }
    }

    /**
     * Runs a mana-ability activation made while another prompt asks this player for mana
     * (CR 605.3a). The payment prompt stays active while the activation is checked, so timing
     * restrictions still see it; a prompt the ability opens for its own choices (the colour of
     * "any color" mana) is answered first, and the payment prompt then returns.
     */
    private void activateDuringManaPayment(GameData gameData, Runnable activation) {
        gameData.interaction.openNestedPromptWindow();
        try {
            activation.run();
        } finally {
            gameData.interaction.closeNestedPromptWindow();
        }
    }

    /**
     * Ends a mana ability activated while its controller holds priority. One activated on its own
     * (no {@code paymentIntent}) puts its non-mana triggers on the stack right away: the player
     * receives priority after activating an ability (CR 117.3c), which is when triggered abilities
     * are put on the stack (CR 603.3). A trigger on the stack can't be taken back, so that
     * activation leaves the cancel-casting undo window. One activated to pay for a cast or
     * activation the client is holding back keeps its triggers waiting, still revertable, until
     * that spell or ability is put on the stack.
     *
     * <p>A standalone activation that stopped for a colour choice is marked standalone, so the
     * answer finishes it the same way once the mana exists (see {@link #handleInteractionAnswer}).
     */
    private void finishPriorityManaActivation(GameData gameData, int revertableBefore,
                                              PendingManaActivation parkedBefore,
                                              ManaPaymentIntent paymentIntent) {
        if (paymentIntent != null) {
            flushManaAbilityTriggersAfterActivation(gameData);
            return;
        }
        PendingManaActivation parked = gameData.pendingRevertableManaActivation;
        if (parked != null && parked != parkedBefore) {
            gameData.pendingRevertableManaActivation = parked.asStandalone();
        }
        flushStandaloneManaActivationTriggers(gameData, revertableBefore);
    }

    /**
     * Puts the triggers of a standalone mana activation on the stack and drops the activations
     * recorded since {@code revertableBefore} that deferred them from the undo window. Triggers
     * still held by an earlier revertable activation (one tapped to pay for a held-back cast) wait.
     */
    private void flushStandaloneManaActivationTriggers(GameData gameData, int revertableBefore) {
        List<ManaActivation> activations = gameData.revertableManaActivations;
        for (int i = activations.size() - 1; i >= Math.min(revertableBefore, activations.size()); i--) {
            if (!activations.get(i).deferredTriggers().isEmpty()) {
                activations.remove(i);
            }
        }
        if (gameData.interaction.isAwaitingInput()
                || gameData.pendingRevertableManaActivation != null
                || gameData.pendingManaAbilityTriggers.isEmpty()) {
            return;
        }
        Set<StackEntry> held = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ManaActivation revertable : activations) {
            held.addAll(revertable.deferredTriggers());
        }
        List<StackEntry> ready = gameData.pendingManaAbilityTriggers.stream()
                .filter(entry -> !held.contains(entry))
                .toList();
        if (ready.isEmpty()) {
            return;
        }
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.stack.addAll(ready);
        gameData.pendingManaAbilityTriggers.removeIf(entry -> !held.contains(entry));
        gameData.priorityPassedBy.clear();
    }

    /**
     * Puts the triggers deferred by a completed mana-ability activation on the stack: the player
     * receives priority after activating an ability (CR 117.3c), which is when triggered abilities
     * are put on the stack (CR 603.3). Waits while a prompt is open or while earlier mana
     * activations can still be undone by the cancel-casting UI.
     */
    private void flushManaAbilityTriggersAfterActivation(GameData gameData) {
        if (gameData.interaction.isAwaitingInput()
                || !gameData.revertableManaActivations.isEmpty()
                || gameData.pendingRevertableManaActivation != null
                || gameData.pendingManaAbilityTriggers.isEmpty()) {
            return;
        }
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.stack.addAll(gameData.pendingManaAbilityTriggers);
        gameData.pendingManaAbilityTriggers.clear();
        gameData.priorityPassedBy.clear();
    }

    public void activateCommandZoneAbility(GameData gameData, Player player, UUID cardId, Integer abilityIndex) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateCommandZoneAbility(gameData, actionPlayer, cardId, abilityIndex))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateCommandZoneAbility(gameData, player, cardId, abilityIndex);
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
        activateHandAbility(gameData, player, handCardIndex, abilityIndex, targetId, xValue, null);
    }

    public void activateHandAbility(GameData gameData, Player player, int handCardIndex, Integer abilityIndex,
                                    UUID targetId, Integer xValue, List<UUID> targetIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateHandAbility(gameData, actionPlayer, handCardIndex, abilityIndex, targetId,
                        xValue, targetIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateHandAbility(gameData, player, handCardIndex, abilityIndex, targetId,
                    xValue, targetIds);
        }
    }

    public void activateHandAbilityWithGraveyardTargets(GameData gameData, Player player, int handCardIndex, Integer abilityIndex, List<UUID> graveyardCardIds) {
        activateHandAbilityWithGraveyardTargets(gameData, player, handCardIndex, abilityIndex, null,
                graveyardCardIds);
    }

    public void activateHandAbilityWithGraveyardTargets(GameData gameData, Player player, int handCardIndex,
                                                        Integer abilityIndex, Integer xValue,
                                                        List<UUID> graveyardCardIds) {
        Player actionPlayer = player;
        if (runAsActionIfNeeded(gameData,
                () -> activateHandAbilityWithGraveyardTargets(gameData, actionPlayer, handCardIndex,
                        abilityIndex, xValue, graveyardCardIds))) return;
        synchronized (gameData) {
            player = resolveActingPlayer(gameData, player);
            requirePriority(gameData, player);
            requireCanActivateAbilities(gameData, player);
            abilityActivationService.activateHandAbilityWithGraveyardTargets(gameData, player, handCardIndex,
                    abilityIndex, xValue, graveyardCardIds);
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
            int deferredManaTriggersBefore = gameData.pendingManaAbilityTriggers.size();
            int revertableBefore = gameData.revertableManaActivations.size();
            PendingManaActivation parkedBefore = gameData.pendingRevertableManaActivation;
            if (!interactionHandlerRegistry.dispatchAnswer(gameData, player, answer)) {
                throw new IllegalStateException(
                        "Not awaiting " + answer.getClass().getSimpleName() + " input");
            }
            // A nested prompt's handler normally returns to its outer prompt through the
            // input-completion epilogue; one that ends without it must not strand the outer one.
            if (gameData.status != GameStatus.FINISHED && !gameData.interaction.isAwaitingInput()) {
                interactionHandlerRegistry.resumeSuspended(gameData);
            }
            // A standalone activation that stopped for its colour has now resolved, and its
            // controller receives priority (CR 117.3c), so its triggers go on the stack (CR 603.3).
            if (parkedBefore != null && parkedBefore.standalone()
                    && gameData.pendingRevertableManaActivation != parkedBefore) {
                flushStandaloneManaActivationTriggers(gameData, revertableBefore);
            } else if (gameData.pendingManaAbilityTriggers.size() > deferredManaTriggersBefore) {
                // A cost choice (sacrifice, graveyard exile, ...) can complete a mana ability.
                flushManaAbilityTriggersAfterActivation(gameData);
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



