package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * The controller chooses any number of their untapped permanents matching {@code filter}, taps
 * those permanents, then queues the reflexive ability even if none were tapped, in which case the
 * reflexive ability's event value is zero.
 */
public record TapAnyNumberOfPermanentsThenReflexiveAbilityEffect(
        PermanentPredicate filter, CardEffect reflexiveEffect) implements CardEffect {

    public TapAnyNumberOfPermanentsThenReflexiveAbilityEffect {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(reflexiveEffect);
    }
}
