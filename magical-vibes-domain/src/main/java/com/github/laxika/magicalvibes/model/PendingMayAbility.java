package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;

import java.util.List;
import java.util.UUID;

public record PendingMayAbility(
        Card sourceCard,
        UUID controllerId,
        List<CardEffect> effects,
        String description,
        UUID targetCardId,
        String manaCost,
        UUID sourcePermanentId,
        TapMultiplePermanentsCost tapPermanentsCost,
        int lifeCost,
        int additionalLifeCost,
        UUID attackedTargetId,
        UUID activePlayerId,
        UUID choicePlayerId,
        Permanent sourcePermanentSnapshot,
        UUID sourceControllerId,
        UUID triggeringCardId,
        int eventValue,
        UUID triggeringPermanentId,
        Integer sourcePowerAtTrigger,
        Integer xValue,
        Integer triggeringPermanentToughnessAtTrigger,
        long triggeringCardGraveyardEntryVersion,
        Card heldCard,
        UUID grantingPermanentId,
        StackEntry triggeringSpellSnapshot
) {

    /**
     * Constructor for a question asked by an ability whose trigger event was about no spell. {@code
     * triggeringSpellSnapshot} is the triggering spell of the stack entry that asked it ("copy that spell"), which the
     * entry the accepted ability resolves as carries ({@code StackEntry.getTriggeringSpellSnapshot()}).
     */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue, UUID triggeringPermanentId, Integer sourcePowerAtTrigger,
                             Integer xValue, Integer triggeringPermanentToughnessAtTrigger,
                             long triggeringCardGraveyardEntryVersion, Card heldCard, UUID grantingPermanentId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,
                triggeringPermanentId, sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger,
                triggeringCardGraveyardEntryVersion, heldCard, grantingPermanentId, null);
    }

    /**
     * Constructor for a question no granted triggered ability asked. {@code grantingPermanentId} is the permanent that
     * granted the triggered ability that asked it (the Equipment of "you may sacrifice this Equipment"), which the
     * stack entry the accepted ability resolves as carries ({@code StackEntry.getGrantingPermanentId()}).
     */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue, UUID triggeringPermanentId, Integer sourcePowerAtTrigger,
                             Integer xValue, Integer triggeringPermanentToughnessAtTrigger,
                             long triggeringCardGraveyardEntryVersion, Card heldCard) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,
                triggeringPermanentId, sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger,
                triggeringCardGraveyardEntryVersion, heldCard, null);
    }

    /**
     * Constructor without a held card: the card, in no zone, that a resolving ability held out of its destination
     * until this question is answered ("you may put it onto the battlefield; otherwise put it into your hand").
     */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue, UUID triggeringPermanentId, Integer sourcePowerAtTrigger,
                             Integer xValue, Integer triggeringPermanentToughnessAtTrigger,
                             long triggeringCardGraveyardEntryVersion) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,
                triggeringPermanentId, sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger,
                triggeringCardGraveyardEntryVersion, null);
    }

    /** Backward-compatible constructor without a triggering card graveyard entry version ({@code -1}: none). */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue, UUID triggeringPermanentId, Integer sourcePowerAtTrigger,
                             Integer xValue, Integer triggeringPermanentToughnessAtTrigger) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,
                triggeringPermanentId, sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger, -1L);
    }

    /** Backward-compatible constructor without a triggering permanent toughness snapshot. */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue, UUID triggeringPermanentId, Integer sourcePowerAtTrigger,
                             Integer xValue) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,
                triggeringPermanentId, sourcePowerAtTrigger, xValue, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue,
                null, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, 0);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, int eventValue) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, null, eventValue, null, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, null, 0);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, null, null, 0);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, UUID sourceControllerId) {
        this(sourceCard, controllerId, effects, description, targetCardId, null, null, null,
                0, 0, null, null, null, null, sourceControllerId, null, 0);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description) {
        this(sourceCard, controllerId, effects, description, null, null, null, null, 0, 0, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId) {
        this(sourceCard, controllerId, effects, description, targetCardId, null, null, null, 0, 0, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, null, null, 0, 0, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost, UUID sourcePermanentId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId, null, 0, 0, null, null);
    }

    /** Creates a may ability that retains a source power captured when its trigger was created. */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID sourcePermanentId, Integer sourcePowerAtTrigger) {
        this(sourceCard, controllerId, effects, description, null, null, sourcePermanentId,
                null, 0, 0, null, null, null, null, null, null, 0, null, sourcePowerAtTrigger, null);
    }

    /** Creates a may ability that retains the X announced by its source spell. */
    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID sourcePermanentId, Integer sourcePowerAtTrigger, Integer xValue) {
        this(sourceCard, controllerId, effects, description, null, null, sourcePermanentId,
                null, 0, 0, null, null, null, null, null, null, 0, null, sourcePowerAtTrigger, xValue);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost, UUID sourcePermanentId, TapMultiplePermanentsCost tapPermanentsCost) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, 0, 0, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost, UUID sourcePermanentId, int additionalLifeCost) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                null, 0, additionalLifeCost, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, null, null);
    }

    /** Retains the triggering context when a cast choice delegates to another existing effect. */
    public PendingMayAbility withEffects(List<CardEffect> updatedEffects) {
        return new PendingMayAbility(sourceCard, controllerId, List.copyOf(updatedEffects), description,
                targetCardId, manaCost, sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost,
                attackedTargetId, activePlayerId, choicePlayerId, sourcePermanentSnapshot,
                sourceControllerId, triggeringCardId, eventValue, triggeringPermanentId,
                sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion,
                heldCard, grantingPermanentId, triggeringSpellSnapshot);
    }

    public PendingMayAbility withSourceControllerId(UUID updatedSourceControllerId) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, updatedSourceControllerId, triggeringCardId,
                eventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,
                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion,
                heldCard, grantingPermanentId, triggeringSpellSnapshot);
    }

    /**
     * This ability with {@code cardId} as the card the trigger event was about ("that card") and the graveyard
     * incarnation it had ({@code -1}: none), carried from the stack entry that asked the question.
     */
    public PendingMayAbility withTriggeringCard(UUID cardId, long graveyardEntryVersion) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, cardId,
                eventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,
                triggeringPermanentToughnessAtTrigger, graveyardEntryVersion,
                heldCard, grantingPermanentId, triggeringSpellSnapshot);
    }

    /** This ability holding {@code card}, which the resolving ability held out of its destination. */
    public PendingMayAbility withHeldCard(Card card) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId,
                eventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,
                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion, card,
                grantingPermanentId, triggeringSpellSnapshot);
    }

    /** This ability as asked by an ability whose trigger event was about {@code spell}, which it keeps a copy of. */
    public PendingMayAbility withTriggeringSpell(StackEntry spell) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId,
                eventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,
                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion, heldCard,
                grantingPermanentId, spell == null ? null : new StackEntry(spell));
    }

    /** This ability as asked by a triggered ability that {@code permanentId} granted. */
    public PendingMayAbility withGrantingPermanentId(UUID permanentId) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId,
                eventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,
                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion, heldCard, permanentId,
                triggeringSpellSnapshot);
    }

    public PendingMayAbility withEventValue(int updatedEventValue) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId,
                updatedEventValue, triggeringPermanentId, sourcePowerAtTrigger, xValue,
                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion,
                heldCard, grantingPermanentId, triggeringSpellSnapshot);
    }

    public PendingMayAbility withXValue(Integer updatedXValue) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, targetCardId, manaCost,
                sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId,
                activePlayerId, choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId,
                eventValue, triggeringPermanentId, sourcePowerAtTrigger, updatedXValue,
                triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion,
                heldCard, grantingPermanentId, triggeringSpellSnapshot);
    }

    /** Copies pending entry requests whose permanent has not entered the battlefield yet. */
    public PendingMayAbility deepCopyEntryRequests() {
        List<CardEffect> copiedEffects = effects.stream().map(effect ->
                effect instanceof com.github.laxika.magicalvibes.model.effect.UnleashEffect unleash
                        && unleash.entryRequest() != null
                        ? (CardEffect) new com.github.laxika.magicalvibes.model.effect.UnleashEffect(
                                unleash.entryRequest().deepCopy())
                        : effect instanceof com.github.laxika.magicalvibes.model.effect.RiotEffect riot
                        && riot.entryRequest() != null
                        ? new com.github.laxika.magicalvibes.model.effect.RiotEffect(riot.entryRequest().deepCopy(),
                                riot.remainingChoices(), riot.counterChoices(), riot.hasteChosen()) : effect).toList();
        return new PendingMayAbility(sourceCard, controllerId, copiedEffects, description, targetCardId,
                manaCost, sourcePermanentId, tapPermanentsCost, lifeCost, additionalLifeCost,
                attackedTargetId, activePlayerId, choicePlayerId, sourcePermanentSnapshot,
                sourceControllerId, triggeringCardId, eventValue, triggeringPermanentId,
                sourcePowerAtTrigger, xValue, triggeringPermanentToughnessAtTrigger, triggeringCardGraveyardEntryVersion,
                heldCard, grantingPermanentId,
                triggeringSpellSnapshot == null ? null : new StackEntry(triggeringSpellSnapshot));
    }
}
