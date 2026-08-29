package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Marker effect for one permanent card offered by
 * {@link MillControllerAndMayReturnMilledPermanentToHandEffect}. The group ID keeps separate
 * resolutions from clearing one another's pending choices, and {@code maxCount} limits how many
 * offers in the group may be accepted.
 */
public record ReturnMilledPermanentToHandEffect(
        UUID groupId,
        CardPredicate filter,
        int maxCount,
        CardPredicate bonusFilter,
        int bonusLife
) implements CardEffect {

    public ReturnMilledPermanentToHandEffect(UUID groupId, CardPredicate filter) {
        this(groupId, filter, 1, null, 0);
    }

    public ReturnMilledPermanentToHandEffect(UUID groupId, CardPredicate filter, int maxCount) {
        this(groupId, filter, maxCount, null, 0);
    }

    public ReturnMilledPermanentToHandEffect(UUID groupId, CardPredicate filter,
                                              CardPredicate bonusFilter, int bonusLife) {
        this(groupId, filter, 1, bonusFilter, bonusLife);
    }

    public ReturnMilledPermanentToHandEffect(UUID groupId) {
        this(groupId, new CardIsPermanentPredicate(), 1, null, 0);
    }

    public ReturnMilledPermanentToHandEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
