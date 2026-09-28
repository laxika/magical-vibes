package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that makes matching permanents enter untapped, regardless of
 * controller.
 */
public record AllPermanentsEnterUntappedEffect(PermanentPredicate filter)
        implements PermanentsEnterUntappedEffect {
}
