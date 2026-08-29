package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Lets the controller tap any number of their untapped permanents matching the filter.
 */
public record TapAnyNumberPermanentsEffect(PermanentPredicate permanentFilter) implements CardEffect {
}
