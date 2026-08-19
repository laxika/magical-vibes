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
        UUID triggeringPermanentId
) {

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description,
                             UUID targetCardId, String manaCost, UUID sourcePermanentId,
                             TapMultiplePermanentsCost tapPermanentsCost, int lifeCost, int additionalLifeCost,
                             UUID attackedTargetId, UUID activePlayerId, UUID choicePlayerId,
                             Permanent sourcePermanentSnapshot, UUID sourceControllerId, UUID triggeringCardId,
                             int eventValue) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId,
                tapPermanentsCost, lifeCost, additionalLifeCost, attackedTargetId, activePlayerId,
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, triggeringCardId, eventValue, null);
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
                choicePlayerId, sourcePermanentSnapshot, sourceControllerId, null, eventValue);
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

    /** Creates a may ability that acts on the spell that caused its spell-cast trigger. */
    public static PendingMayAbility forSpellCastTrigger(Card sourceCard, UUID controllerId,
                                                        List<CardEffect> effects, String description,
                                                        String manaCost, UUID sourcePermanentId,
                                                        UUID triggeringCardId) {
        return new PendingMayAbility(sourceCard, controllerId, effects, description, null, manaCost,
                sourcePermanentId, null, 0, 0, null, null, null, null, null, triggeringCardId, 0);
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
}
