package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Marker effect for one permanent card offered by
 * {@link MillControllerAndMayReturnMilledPermanentToHandEffect}. The group ID keeps separate
 * resolutions from clearing one another's pending choices. The optional bonus applies only after
 * the offered card has successfully moved to its owner's hand.
 */
public record ReturnMilledPermanentToHandEffect(
        UUID groupId,
        CardPredicate filter,
        CardPredicate bonusFilter,
        int bonusLife
) implements CardEffect {

    public ReturnMilledPermanentToHandEffect(UUID groupId) {
        this(groupId, new CardIsPermanentPredicate(), null, 0);
    }

    public ReturnMilledPermanentToHandEffect(UUID groupId, CardPredicate filter) {
        this(groupId, filter, null, 0);
    }
}
