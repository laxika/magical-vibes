package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/**
 * An emblem in the command zone. {@code timestamp} is its CR 613.7 timestamp, taken when the
 * emblem was created; hand-built emblems without one use 0 (before every other effect).
 * {@code triggeredAbilities} are the emblem's triggered abilities as abilities ("whenever you cast a
 * creature or planeswalker spell, …"), which the trigger collectors read from the command zone;
 * {@code staticEffects} holds its static abilities.
 */
public record Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                     List<ActivatedAbility> activatedAbilities, long timestamp,
                     List<TriggeredAbility> triggeredAbilities) {
    public Emblem {
        staticEffects = List.copyOf(staticEffects);
        activatedAbilities = List.copyOf(activatedAbilities);
        triggeredAbilities = List.copyOf(triggeredAbilities);
    }

    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                  List<ActivatedAbility> activatedAbilities, long timestamp) {
        this(controllerId, staticEffects, sourceCard, activatedAbilities, timestamp, List.of());
    }

    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard,
                  List<ActivatedAbility> activatedAbilities) {
        this(controllerId, staticEffects, sourceCard, activatedAbilities, 0L);
    }

    public Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard) {
        this(controllerId, staticEffects, sourceCard, List.of(), 0L);
    }
}
