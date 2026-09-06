package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Creates a token copy of the permanent created by a preceding targeted graveyard return.
 * The stack entry keeps the returned card's ID as its target while the battlefield permanent
 * receives a new ID.
 */
public record CreateTokenCopyOfReturnedPermanentEffect(
        PermanentPredicate condition,
        Integer powerOverride,
        Integer toughnessOverride
) implements CardEffect {
}
