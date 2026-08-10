package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: players can't play lands while at least {@code minimumCount} matching
 * permanents are on the battlefield.
 */
public record PlayersCantPlayLandsIfPermanentCountEffect(int minimumCount, PermanentPredicate filter)
        implements GlobalLandPlayRestrictionEffect {
}
