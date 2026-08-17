package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses any number of their untapped permanents matching {@code filter}, taps
 * those permanents, and draws a card for each permanent tapped this way.
 */
public record TapAnyNumberOfPermanentsThenDrawPerTappedEffect(PermanentPredicate filter)
        implements CardEffect {
}
