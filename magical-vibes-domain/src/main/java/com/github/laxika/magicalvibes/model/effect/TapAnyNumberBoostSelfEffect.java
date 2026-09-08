package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * An effect that lets its controller tap any number of untapped matching permanents, then gives
 * the source +X/+Y until end of turn for each permanent tapped this way.
 */
public record TapAnyNumberBoostSelfEffect(
        PermanentPredicate permanentFilter,
        int powerPerPermanent,
        int toughnessPerPermanent
) implements CardEffect {
}
