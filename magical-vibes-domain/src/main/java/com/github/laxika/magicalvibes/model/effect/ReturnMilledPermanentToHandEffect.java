package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Marker effect for one permanent card offered by
 * {@link MillControllerAndMayReturnMilledPermanentToHandEffect}. The group ID keeps separate
 * resolutions from clearing one another's pending choices.
 */
public record ReturnMilledPermanentToHandEffect(UUID groupId, CardPredicate filter) implements CardEffect {

    public ReturnMilledPermanentToHandEffect(UUID groupId) {
        this(groupId, new CardIsPermanentPredicate());
    }
}
