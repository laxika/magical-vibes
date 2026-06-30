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
        TapMultiplePermanentsCost tapPermanentsCost
) {

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description) {
        this(sourceCard, controllerId, effects, description, null, null, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId) {
        this(sourceCard, controllerId, effects, description, targetCardId, null, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost, UUID sourcePermanentId) {
        this(sourceCard, controllerId, effects, description, targetCardId, manaCost, sourcePermanentId, null);
    }
}
