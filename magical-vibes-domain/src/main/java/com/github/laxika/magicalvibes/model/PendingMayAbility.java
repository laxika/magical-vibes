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
        Permanent sourcePermanentSnapshot
) {

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
