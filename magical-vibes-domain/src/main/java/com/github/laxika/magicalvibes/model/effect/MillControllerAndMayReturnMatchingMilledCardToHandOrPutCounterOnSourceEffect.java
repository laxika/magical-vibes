package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then offers matching milled cards one at a time for
 * return to hand. If every offer is declined, a +1/+1 counter is put on the source permanent.
 */
public record MillControllerAndMayReturnMatchingMilledCardToHandOrPutCounterOnSourceEffect(
        int count, CardPredicate filter) implements CardEffect {
}
