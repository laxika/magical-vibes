package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Creates a token copy of each permanent controlled by the effect's controller that matches the
 * supplied filter, excluding the source permanent. Matching permanents are captured before any
 * tokens are created, so the new tokens are not copied by the same effect.
 */
public record CreateTokenCopyOfEachOtherControlledPermanentEffect(PermanentPredicate filter)
        implements CardEffect {
}
