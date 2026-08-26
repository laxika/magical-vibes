package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that makes matching permanents controlled by this source's controller
 * enter untapped.
 */
public record ControlledPermanentsEnterUntappedEffect(PermanentPredicate filter)
        implements PermanentsEnterUntappedEffect {
}
