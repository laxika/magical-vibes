package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** The controller taps any number of untapped creatures they control, then queues a reflexive ability. */
public record TapAnyNumberOfCreaturesThenReflexiveAbilityEffect(CardEffect reflexiveEffect)
        implements CardEffect {

    public TapAnyNumberOfCreaturesThenReflexiveAbilityEffect {
        Objects.requireNonNull(reflexiveEffect);
    }
}
