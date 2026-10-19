package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * An emblem in the command zone. {@code timestamp} is its CR 613.7 timestamp, taken when the
 * emblem was created; hand-built emblems without one use 0 (before every other effect).
 */
public record Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                     List<ActivatedAbility> activatedAbilities, long timestamp) {
    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                  List<ActivatedAbility> activatedAbilities, long timestamp) {
        this.controllerId = controllerId;
        this.staticEffects = List.copyOf(staticEffects);
        this.sourceCard = sourceCard;
        this.activatedAbilities = List.copyOf(activatedAbilities);
        this.timestamp = timestamp;
    }

    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                  List<ActivatedAbility> activatedAbilities) {
        this(controllerId, staticEffects, sourceCard, activatedAbilities, 0L);
    }

    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard) {
        this(controllerId, staticEffects, sourceCard, List.of(), 0L);
    }
}
