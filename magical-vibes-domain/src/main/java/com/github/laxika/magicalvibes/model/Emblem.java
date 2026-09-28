package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

public record Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                     List<ActivatedAbility> activatedAbilities) {
    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                  List<ActivatedAbility> activatedAbilities) {
        this.controllerId = controllerId;
        this.staticEffects = List.copyOf(staticEffects);
        this.sourceCard = sourceCard;
        this.activatedAbilities = List.copyOf(activatedAbilities);
    }

    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard) {
        this(controllerId, staticEffects, sourceCard, List.of());
    }
}
