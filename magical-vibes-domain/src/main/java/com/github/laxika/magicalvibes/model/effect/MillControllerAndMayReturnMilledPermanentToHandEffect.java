package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then offers each matching card milled by this
 * resolution for return to its owner's hand. The offers are represented by
 * {@link ReturnMilledPermanentToHandEffect} marker effects. An optional bonus filter and life
 * amount apply only when a matching offered card is actually returned.
 */
public record MillControllerAndMayReturnMilledPermanentToHandEffect(
        int count,
        CardPredicate filter,
        CardPredicate bonusFilter,
        int bonusLife
)
        implements CardEffect {

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count) {
        this(count, new CardIsPermanentPredicate(), null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count, CardPredicate filter) {
        this(count, filter, null, 0);
    }
}
