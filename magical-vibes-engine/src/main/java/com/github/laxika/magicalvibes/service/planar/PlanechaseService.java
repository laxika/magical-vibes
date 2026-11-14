package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.effect.*;
import com.github.laxika.magicalvibes.model.planar.*;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AscendEffectHandler;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/** Shared-deck Planechase actions. Callers own the game mutation and priority boundaries. */
@Service
public class PlanechaseService {
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactions;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.cast.PotentialManaService potentialMana;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private AscendEffectHandler ascendEffectHandler;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private PhasingService phasingService;
    private final CardCatalog catalog;
    private final PlanarDieRoller die;
    private final GameQueryService query;
    private final GameLogService logs;
    private final TriggerCollectionService triggers;
    private final ConditionEvaluationService conditionEvaluationService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;

    public PlanechaseService(PlanarDieRoller die, @Lazy GameQueryService query,
                             @Lazy GameLogService logs, @Lazy TriggerCollectionService triggers,
                             @Lazy ConditionEvaluationService conditionEvaluationService, CardCatalog catalog,
                             @Lazy TriggeredAbilityStackService triggeredAbilityStackService) {
        this.triggeredAbilityStackService = triggeredAbilityStackService;
        this.catalog = catalog;
        this.die = die;
        this.query = query;
        this.logs = logs;
        this.triggers = triggers;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    public void initializeDeck(GameData game) {
        game.planechase = new PlanechaseState();
        for (int i = 0; i < 20; i++) {
            Card card = catalog.findByCollectorNumber(CardSet.SET_MOC, "153").createCard();
            card.freeze();
            game.planechase.deck.add(card);
        }
        Collections.shuffle(game.planechase.deck);
    }

    public void start(GameData game) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        state.controllerId = game.startingPlayerId;
        if (!state.faceUp.isEmpty()) return;
        if (state.deck.stream().noneMatch(card -> card.hasType(CardType.PLANE))) {
            if (game.session.depth() > 0) return;
            throw new IllegalStateException("A planar deck must contain a plane");
        }
        while (state.deck.getFirst().hasType(CardType.PHENOMENON)) {
            state.deck.add(state.deck.removeFirst());
        }
        reveal(game, false);
    }

    public void restart(GameData game) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        state.faceUp.forEach(object -> state.deck.add(object.getCard()));
        state.faceUp.clear();
        state.specialActionRolls.clear();
        state.blankRollChaosSources.clear();
        state.planeswalkedToTurn = -1;
        state.planeswalkedToNamesThisTurn.clear();
        state.rollTurn = -1;
        state.lastRoll = null;
        state.lastRollPlayerId = null;
        Collections.shuffle(state.deck);
    }

    public boolean canRoll(GameData game, UUID playerId, ManaPool pool) {
        return canRollAtThisTime(game, playerId) && rollManaCost(game, playerId).canPayForSpecialAction(pool);
    }

    public boolean canOfferRoll(GameData game, UUID playerId) {
        if (!canRollAtThisTime(game, playerId)) return false;
        return canRoll(game, playerId, game.playerManaPools.get(playerId))
                || canRoll(game, playerId, potentialMana.buildVirtualManaPoolForSpecialAction(game, playerId));
    }

    public boolean canRollAtThisTime(GameData game, UUID playerId) {
        return game.planechase != null && game.status == GameStatus.RUNNING
                && Objects.equals(game.activePlayerId, playerId)
                && Objects.equals(query.getPriorityPlayerId(game), playerId)
                && (game.currentStep == TurnStep.PRECOMBAT_MAIN || game.currentStep == TurnStep.POSTCOMBAT_MAIN)
                && game.stack.isEmpty() && !game.interaction.isAwaitingInput()
                && game.pendingMayAbilities.isEmpty() && game.pendingInteractions.isEmpty()
                && game.pendingManaAbilityTriggers.isEmpty() && game.pendingEffectResolutionEntry == null;
    }

    public ManaCost rollManaCost(GameData game, UUID playerId) {
        return new ManaCost("{" + game.planechase.rollCost(playerId, game.turnNumber) + "}");
    }

    public void rollSpecialAction(GameData game, UUID playerId) {
        ManaPool pool = game.playerManaPools.get(playerId);
        if (pool == null || !canRoll(game, playerId, pool)) {
            throw new IllegalStateException("You cannot roll the planar die now");
        }
        rollManaCost(game, playerId).payForSpecialAction(pool);
        game.planechase.recordSpecialAction(playerId, game.turnNumber);
        game.revertableManaActivations.clear();
        game.priorityPassedBy.clear();
        roll(game, playerId);
    }

    /**
     * Returns the number of planar dice to roll for this player after static replacements. A
     * permanent that has lost its abilities adds no extra die.
     */
    private int numberOfRolls(GameData game, UUID rollingPlayerId) {
        int count = 1;
        for (Permanent permanent : game.playerBattlefields.getOrDefault(rollingPlayerId, List.of())) {
            if (query.hasLostPrintedAbilities(game, permanent)) {
                continue;
            }
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                if (effect instanceof RollPlanarDieWithAdvantageEffect) {
                    count++;
                }
            }
        }
        return count;
    }

    public void roll(GameData game, UUID playerId) {
        if (game.planechase == null) return;
        int rollCount = numberOfRolls(game, playerId);
        List<PlanarDieResult> rolls = new ArrayList<>(rollCount);
        for (int i = 0; i < rollCount; i++) {
            rolls.add(die.roll());
        }
        if (rolls.size() > 1) {
            interactions.begin(game, new PendingInteraction.PlanarDieChoice(
                    playerId, rolls, rolls.size() - 1));
            return;
        }
        completeRoll(game, playerId, rolls.getFirst());
    }

    /** Completes a planar roll after any replacement-effect choice has been made. */
    public void completeRoll(GameData game, UUID playerId, PlanarDieResult result) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        state.controllerId = game.activePlayerId;
        state.lastRoll = result == PlanarDieResult.BLANK && !state.blankRollChaosSources.isEmpty()
                ? PlanarDieResult.CHAOS : result;
        state.lastRollPlayerId = playerId;
        state.rollSequence++;
        logs.append(game, GameLogEntry.text(game.playerIdToName.get(playerId)
                + " rolls the planar die: " + state.lastRoll.name().toLowerCase(Locale.ROOT) + "."));
        triggers.checkControllerRollsPlanarDieTriggers(game, playerId,
                state.lastRoll == PlanarDieResult.BLANK ? 0 : 1);
        switch (state.lastRoll) {
            case BLANK -> { }
            case CHAOS -> chaos(game);
            case PLANESWALKER -> {
                if (planarDiePlaneswalkIsReplaced(game)) {
                    chaos(game);
                } else {
                    // The planeswalking ability, controlled by the player whose roll made it trigger (CR 901.8, 901.9c).
                    triggeredAbilityStackService.putInherent(game, playerId, "Planeswalk",
                            List.of(new PlaneswalkEffect()), TriggeredAbilityRequest.TriggerRecord.NONE);
                }
            }
        }
    }

    private boolean planarDiePlaneswalkIsReplaced(GameData game) {
        synchronized (game.floatingEffects) {
            return game.floatingEffects.stream()
                    .filter(effect -> effect.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN)
                    .anyMatch(effect -> effect.effect() instanceof PlanarDiePlaneswalkToChaosReplacementEffect);
        }
    }

    public void chaos(GameData game) {
        if (game.planechase == null) return;
        for (PlanarObject object : List.copyOf(game.planechase.faceUp)) {
            trigger(game, object, Triggers.chaosEnsues(), game.planechase.controllerId);
        }
    }

    public void planeswalk(GameData game) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        state.controllerId = game.activePlayerId;
        if (applyPlaneswalkReplacement(game)) {
            return;
        }
        continuePlaneswalk(game);
    }

    /** Continues a planeswalk after a replacement effect has ordered the top planar cards. */
    public void continuePlaneswalk(GameData game) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        if (state.faceUp.size() > 1) {
            interactions.begin(game, new PendingInteraction.LibraryReorder(state.controllerId,
                    state.faceUp.stream().map(PlanarObject::getCard).toList(), true, null,
                    "Order the departing cards on the bottom of the planar deck", 0, true));
            return;
        }
        finishPlaneswalk(game, List.copyOf(state.faceUp));
    }

    private boolean applyPlaneswalkReplacement(GameData game) {
        PlanechaseState state = game.planechase;
        if (state.deck.size() < 2 || state.controllerId == null) {
            return false;
        }

        boolean replacementActive = false;
        for (Permanent source : game.playerBattlefields.getOrDefault(state.controllerId, List.of())) {
            if (source.isFaceDown() || source.isLosesAllAbilitiesUntilEndOfTurn()
                    || query.computeStaticBonus(game, source).losesAllAbilities()) {
                continue;
            }
            if (source.getCard().getStaticEffects().stream()
                    .anyMatch(PlaneswalkTopTwoReplacementEffect.class::isInstance)) {
                replacementActive = true;
                break;
            }
        }
        if (!replacementActive) {
            return false;
        }

        List<Card> revealed = List.of(state.deck.removeFirst(), state.deck.removeFirst());
        logs.append(game, GameLog.text(game.playerIdToName.get(state.controllerId)
                + " looks at the top two cards of the planar deck."));
        interactions.begin(game, new PendingInteraction.PlanarCardChoice(
                state.controllerId, revealed, revealed.stream().map(Card::getId).toList(),
                "Choose one card to put on the bottom of the planar deck, then planeswalk.", true));
        return true;
    }

    public void finishPlaneswalk(GameData game, List<PlanarObject> departing) {
        PlanechaseState state = game.planechase;
        if (phasingService != null) phasingService.phaseInUntilPlaneswalk(game);
        state.faceUp.clear();
        if (departing.stream().anyMatch(object -> object.getCard().hasType(CardType.PLANE))) {
            state.blankRollChaosSources.clear();
        }
        state.planeswalkedToNamesThisTurn.clear();
        for (PlanarObject object : departing) {
            state.deck.add(object.getCard());
            trigger(game, object, Triggers.planeswalkAwayFromThis(), state.controllerId);
        }
        game.floatingEffects.removeIf(effect -> effect.duration() == EffectDuration.UNTIL_PLANESWALK);
        reveal(game, true);
    }

    /** Replaces the face-up planar cards with the two planes found by Spatial Merging. */
    public void completeSpatialMerging(GameData game, List<Card> arrivingPlanes,
                                       List<Card> cardsToBottom, UUID controllerId) {
        if (game.planechase == null) return;

        PlanechaseState state = game.planechase;
        state.controllerId = controllerId;
        if (phasingService != null) phasingService.phaseInUntilPlaneswalk(game);
        List<PlanarObject> departing = List.copyOf(state.faceUp);
        state.faceUp.clear();
        if (departing.stream().anyMatch(object -> object.getCard().hasType(CardType.PLANE))) {
            state.blankRollChaosSources.clear();
        }
        state.planeswalkedToNamesThisTurn.clear();

        for (PlanarObject object : departing) {
            state.deck.add(object.getCard());
            trigger(game, object, Triggers.planeswalkAwayFromThis(), controllerId);
        }
        state.deck.addAll(cardsToBottom);
        game.floatingEffects.removeIf(effect -> effect.duration() == EffectDuration.UNTIL_PLANESWALK);

        for (Card card : arrivingPlanes) {
            state.faceUp.add(new PlanarObject(card, game.nextTimestamp()));
            if (card.hasType(CardType.PLANE)) state.planeswalkedToNamesThisTurn.add(card.getName());
        }
        state.planeswalkedToTurn = game.turnNumber;
        if (ascendEffectHandler != null) {
            ascendEffectHandler.checkPlanarAscend(game);
        }
        for (PlanarObject object : List.copyOf(state.faceUp)) {
            logs.append(game, GameLogEntry.text("The planar card is " + object.getCard().getName() + "."));
            trigger(game, object, Triggers.planeswalkToThis(), controllerId);
        }
    }

    /** Adds one plane without planeswalking away from any currently face-up planar card. */
    public void completePlaneswalkToPlaneWithoutDeparting(GameData game, Card arrivingPlane,
                                                          List<Card> cardsToBottom, UUID controllerId) {
        if (game.planechase == null) return;

        PlanechaseState state = game.planechase;
        state.controllerId = controllerId;
        state.deck.addAll(cardsToBottom);
        PlanarObject object = new PlanarObject(arrivingPlane, game.nextTimestamp());
        state.faceUp.add(object);
        state.planeswalkedToTurn = game.turnNumber;
        if (arrivingPlane.hasType(CardType.PLANE)) state.planeswalkedToNamesThisTurn.add(arrivingPlane.getName());
        if (phasingService != null) phasingService.phaseInUntilPlaneswalk(game);
        game.floatingEffects.removeIf(effect -> effect.duration() == EffectDuration.UNTIL_PLANESWALK);
        logs.append(game, GameLogEntry.text("The planar card is " + arrivingPlane.getName() + "."));
        if (ascendEffectHandler != null) {
            ascendEffectHandler.checkPlanarAscend(game);
        }
        trigger(game, object, Triggers.planeswalkToThis(), controllerId);
    }

    public void reveal(GameData game, boolean triggerAbilities) {
        PlanechaseState state = game.planechase;
        if (state.deck.isEmpty()) return;
        PlanarObject object = new PlanarObject(state.deck.removeFirst(), game.nextTimestamp());
        state.faceUp.add(object);
        if (triggerAbilities && object.getCard().hasType(CardType.PLANE)) {
            state.planeswalkedToTurn = game.turnNumber;
            state.planeswalkedToNamesThisTurn.add(object.getCard().getName());
        }
        logs.append(game, GameLogEntry.text("The planar card is " + object.getCard().getName() + "."));
        if (ascendEffectHandler != null) {
            ascendEffectHandler.checkPlanarAscend(game);
        }
        if (triggerAbilities) {
            trigger(game, object, object.getCard().hasType(CardType.PHENOMENON)
                    ? Triggers.encounterThis() : Triggers.planeswalkToThis(), state.controllerId);
        }
    }

    /**
     * Hands the planar object's abilities with {@code trigger} to the stack door under {@code controller}, each one whole,
     * in the order they were written: the door judges its intervening "if" (CR 603.4) and puts it on the stack as one
     * object of its effects (CR 603.3), choosing its targets as it goes there (CR 603.3d), with the planar object it is
     * of. A planar ability's event condition is judged as it triggers ({@link MiscAbilityReader}, CR 603.2); the step
     * and phase abilities are the trigger pipeline's.
     */
    public void trigger(GameData game, PlanarObject object, Trigger trigger, UUID controller) {
        Card card = object.getCard();
        ConditionContext context = ConditionContext.forCard(card, controller);
        for (TriggeredAbility ability : triggeredAbilities(game, card, trigger, context)) {
            PlanarObject source = object.copy();
            triggeredAbilityStackService.put(game, new TriggeredAbilityRequest(ability, card, null, controller,
                    card.getName() + "'s ability", context, entry -> entry.setSourcePlanarObject(source)));
        }
    }

    /** The abilities of {@code card} with {@code trigger} whose trigger event this is, in order. */
    private List<TriggeredAbility> triggeredAbilities(GameData game, Card card, Trigger trigger,
                                                      ConditionContext context) {
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : card.getTriggeredAbilities(trigger)) {
            if (!MiscAbilityReader.effects(List.of(ability), trigger).isEmpty()
                    && conditionEvaluationService.eventConditionMet(game, ability.options(), context)) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    public boolean checkPhenomena(GameData game) {
        if (game.planechase == null || game.effectResolutionDepth > 0 || game.interaction.isAwaitingInput()
                || game.pendingEffectResolutionEntry != null || !game.pendingMayAbilities.isEmpty()) return false;
        for (PlanarObject object : List.copyOf(game.planechase.faceUp)) {
            if (!object.getCard().hasType(CardType.PHENOMENON)) continue;
            // Its ability is on the stack, held with mana abilities' triggers, or waiting for its mode or targets with
            // its record as it goes there.
            boolean pending = game.stack.stream().anyMatch(entry -> isSource(entry, object))
                    || game.pendingManaAbilityTriggers.stream().anyMatch(entry -> isSource(entry, object))
                    || game.pendingInteractions.stream()
                    .anyMatch(interaction -> isSource(game.queuedTriggerRecord(interaction), object));
            if (!pending) {
                planeswalk(game);
                return true;
            }
        }
        return false;
    }

    public void enableBlankPlanarDieRollChaos(GameData game, UUID sourceId) {
        if (game.planechase != null && sourceId != null) {
            game.planechase.blankRollChaosSources.add(sourceId);
        }
    }

    private boolean isSource(StackEntry entry, PlanarObject object) {
        return entry != null && entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getSourcePlanarObject() != null
                && entry.getSourcePlanarObject().getId().equals(object.getId());
    }
}
