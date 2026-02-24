package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

public record PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId, String manaCost) {

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description) {
        this(sourceCard, controllerId, effects, description, null, null);
    }

    public PendingMayAbility(Card sourceCard, UUID controllerId, List<CardEffect> effects, String description, UUID targetCardId) {
        this(sourceCard, controllerId, effects, description, targetCardId, null);
    }
}
