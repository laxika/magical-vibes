package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * During resolution, the controller chooses zero or more matching permanents to transform.
 */
public record TransformAnyNumberOfPermanentsEffect(PermanentPredicate filter) implements CardEffect {
}
