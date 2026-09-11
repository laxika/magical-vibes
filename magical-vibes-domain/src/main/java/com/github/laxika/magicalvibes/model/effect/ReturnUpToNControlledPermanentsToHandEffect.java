package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns up to {@code maxCount} matching permanents controlled by the resolving player to their
 * owners' hands. The controller may choose zero permanents.
 */
public record ReturnUpToNControlledPermanentsToHandEffect(
        int maxCount, PermanentPredicate filter, String noun) implements CardEffect {
}
