package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns {@code count} matching permanents controlled by the resolving player to their owners'
 * hands. If fewer than {@code count} matching permanents are available, all of them are returned.
 */
public record ReturnNControlledPermanentsToHandEffect(int count, PermanentPredicate filter, String noun)
        implements CardEffect {
}
