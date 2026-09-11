package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.effect.*;
import com.github.laxika.magicalvibes.model.planar.*;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
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
    private final CardCatalog catalog;
    private final PlanarDieRoller die;
    private final GameQueryService query;
    private final GameLogService logs;
    private final TriggerCollectionService triggers;
    private final ConditionEvaluationService conditionEvaluationService;

    public PlanechaseService(PlanarDieRoller die, @Lazy GameQueryService query,
                             @Lazy GameLogService logs, @Lazy TriggerCollectionService triggers,
                             @Lazy ConditionEvaluationService conditionEvaluationService, CardCatalog catalog) {
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
        state.rollTurn = -1;
        state.lastRoll = null;
        state.lastRollPlayerId = null;
        Collections.shuffle(state.deck);
    }

    public boolean canRoll(GameData game, UUID playerId, ManaPool pool) {
        return canRollAtThisTime(game, playerId) && rollManaCost(game, playerId).canPay(pool, 0, false, false, false, false, false,
                null, null, false, false, false, false, null, true);
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
        rollManaCost(game, playerId).pay(pool, 0, false, false, false, false, false,
                null, null, false, false, false, false, null, true);
        game.planechase.recordSpecialAction(playerId, game.turnNumber);
        game.revertableManaActivations.clear();
        game.priorityPassedBy.clear();
        roll(game, playerId);
    }

    public void roll(GameData game, UUID playerId) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        state.controllerId = game.activePlayerId;
        state.lastRoll = die.roll();
        state.lastRollPlayerId = playerId;
        state.rollSequence++;
        logs.append(game, GameLogEntry.text(game.playerIdToName.get(playerId)
                + " rolls the planar die: " + state.lastRoll.name().toLowerCase(Locale.ROOT) + "."));
        triggers.checkControllerRollsPlanarDieTriggers(game, playerId);
        switch (state.lastRoll) {
            case BLANK -> {
                if (state.faceUp.stream().anyMatch(object -> state.blankRollChaosSources.contains(object.getId()))) {
                    chaos(game);
                }
            }
            case CHAOS -> chaos(game);
            case PLANESWALKER -> game.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                    null, playerId, "Planeswalk", List.of(new PlaneswalkEffect())));
        }
    }

    public void chaos(GameData game) {
        if (game.planechase == null) return;
        for (PlanarObject object : List.copyOf(game.planechase.faceUp)) {
            trigger(game, object, EffectSlot.CHAOS_TRIGGERED, game.planechase.controllerId);
        }
    }

    public void planeswalk(GameData game) {
        if (game.planechase == null) return;
        PlanechaseState state = game.planechase;
        state.controllerId = game.activePlayerId;
        if (state.faceUp.size() > 1) {
            interactions.begin(game, new PendingInteraction.LibraryReorder(state.controllerId,
                    state.faceUp.stream().map(PlanarObject::getCard).toList(), true, null,
                    "Order the departing cards on the bottom of the planar deck", 0, true));
            return;
        }
        finishPlaneswalk(game, List.copyOf(state.faceUp));
    }

    public void finishPlaneswalk(GameData game, List<PlanarObject> departing) {
        PlanechaseState state = game.planechase;
        state.faceUp.clear();
        state.blankRollChaosSources.clear();
        for (PlanarObject object : departing) {
            state.deck.add(object.getCard());
            trigger(game, object, EffectSlot.PLANESWALK_FROM_TRIGGERED, state.controllerId);
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
        List<PlanarObject> departing = List.copyOf(state.faceUp);
        state.faceUp.clear();
        state.blankRollChaosSources.clear();

        for (PlanarObject object : departing) {
            state.deck.add(object.getCard());
            trigger(game, object, EffectSlot.PLANESWALK_FROM_TRIGGERED, controllerId);
        }
        state.deck.addAll(cardsToBottom);
        game.floatingEffects.removeIf(effect -> effect.duration() == EffectDuration.UNTIL_PLANESWALK);

        for (Card card : arrivingPlanes) {
            state.faceUp.add(new PlanarObject(card, game.nextTimestamp()));
        }
        for (PlanarObject object : List.copyOf(state.faceUp)) {
            logs.append(game, GameLogEntry.text("The planar card is " + object.getCard().getName() + "."));
            trigger(game, object, EffectSlot.PLANESWALK_TO_TRIGGERED, controllerId);
        }
    }

    public void reveal(GameData game, boolean triggerAbilities) {
        PlanechaseState state = game.planechase;
        if (state.deck.isEmpty()) return;
        PlanarObject object = new PlanarObject(state.deck.removeFirst(), game.nextTimestamp());
        state.faceUp.add(object);
        logs.append(game, GameLogEntry.text("The planar card is " + object.getCard().getName() + "."));
        if (triggerAbilities) {
            trigger(game, object, object.getCard().hasType(CardType.PHENOMENON)
                    ? EffectSlot.ENCOUNTER_TRIGGERED : EffectSlot.PLANESWALK_TO_TRIGGERED, state.controllerId);
        }
    }

    public void trigger(GameData game, PlanarObject object, EffectSlot slot, UUID controller) {
        Card card = object.getCard();
        List<CardEffect> effects = card.getEffects(slot).stream()
                .filter(effect -> !(effect instanceof ConditionalEffect conditional)
                        || !conditional.interveningIf()
                        || conditionEvaluationService.isMet(game, conditional.condition(),
                        ConditionContext.forCard(card, controller)))
                .toList();
        boolean needsSlotBySlotTargetSelection = card.getSpellTargets().size() > 1
                || card.getSpellTargets().stream().anyMatch(target -> target.getMaxTargets() > 1
                || target.getMinTargets() == 0 || target.getDynamicMinTargets() != null);
        List<CardEffect> multiTargetEffects = needsSlotBySlotTargetSelection
                ? effects.stream().filter(effect -> card.getEffectTargetIndex(effect) >= 0).toList()
                : List.of();
        boolean multiTargetQueued = false;

        for (CardEffect effect : effects) {
            if (needsSlotBySlotTargetSelection && card.getEffectTargetIndex(effect) >= 0) {
                if (isStandaloneSingleTargetEffect(card, effect, effects)) {
                    int targetGroupIndex = card.getEffectTargetIndex(effect);
                    TargetFilter targetFilter = card.getSpellTargets().get(targetGroupIndex).getFilter();
                    boolean playerTargetOnly = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                    game.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            card, controller, List.of(effect), playerTargetOnly, targetFilter,
                            0, null, null, false, null, null, controller, object.copy()));
                    continue;
                }
                if (multiTargetQueued) {
                    continue;
                }
                game.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, controller, multiTargetEffects, null, List.of(), 0, 0,
                        List.of(), 0, List.of(), false, null, null, 0, object.copy()));
                multiTargetQueued = true;
                continue;
            }
            boolean targetsGraveyard = effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD);
            boolean targetsOtherZone = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD)
                    || effect.targetSpec().admits(TargetPredicate.Kind.SPELL);
            if (targetsGraveyard && !targetsOtherZone) {
                game.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controller, List.of(effect), null, 1));
                continue;
            }
            if (effect.targetSpec().targetPredicate() != null) {
                boolean playerTargetOnly = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                TargetFilter targetFilter = playerTargetOnly ? null : card.getTargetFilter();
                game.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        card, controller, List.of(effect), playerTargetOnly, targetFilter,
                        0, null, null, false, null, null, controller, object.copy()));
                continue;
            }
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controller,
                    card.getName() + "'s ability", List.of(effect));
            entry.setSourcePlanarObject(object.copy());
            game.enqueueTrigger(entry);
        }
    }

    private boolean isStandaloneSingleTargetEffect(Card card, CardEffect effect, List<CardEffect> effects) {
        if (effects.size() != 1 || effect.targetSpec() == TargetSpec.NONE) {
            return false;
        }
        int targetGroupIndex = card.getEffectTargetIndex(effect);
        if (targetGroupIndex < 0 || targetGroupIndex >= card.getSpellTargets().size()) {
            return false;
        }
        SpellTarget targetGroup = card.getSpellTargets().get(targetGroupIndex);
        return targetGroup.getMinTargets() == 1 && targetGroup.getMaxTargets() == 1;
    }

    public void step(GameData game, EffectSlot... slots) {
        if (game.planechase == null) return;
        game.planechase.controllerId = game.activePlayerId;
        for (PlanarObject object : List.copyOf(game.planechase.faceUp)) {
            for (EffectSlot slot : slots) trigger(game, object, slot, game.activePlayerId);
        }
    }

    public void drawStep(GameData game) {
        if (game.planechase == null) return;
        game.planechase.controllerId = game.activePlayerId;
        for (PlanarObject object : List.copyOf(game.planechase.faceUp)) {
            trigger(game, object, EffectSlot.DRAW_TRIGGERED, game.activePlayerId);
            trigger(game, object, EffectSlot.EACH_DRAW_TRIGGERED, game.activePlayerId);
        }
    }

    public boolean checkPhenomena(GameData game) {
        if (game.planechase == null || game.effectResolutionDepth > 0 || game.interaction.isAwaitingInput()
                || game.pendingEffectResolutionEntry != null || !game.pendingMayAbilities.isEmpty()
                || game.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)) return false;
        for (PlanarObject object : List.copyOf(game.planechase.faceUp)) {
            if (!object.getCard().hasType(CardType.PHENOMENON)) continue;
            boolean pending = game.stack.stream().anyMatch(entry -> isSource(entry, object))
                    || game.pendingManaAbilityTriggers.stream().anyMatch(entry -> isSource(entry, object));
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
        return entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getSourcePlanarObject() != null
                && entry.getSourcePlanarObject().getId().equals(object.getId());
    }
}
