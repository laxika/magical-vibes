package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Exile each other nonland permanent with mana value less than or equal to {@code maximumManaValue}", the amount
 * evaluated as it resolves: Sarulf, Realm Eater's "the number of counters removed this way" is
 * {@code AmountThisWay(REMOVE_COUNTERS)} after its removal step. Nothing is exiled while the source is off the
 * battlefield (Sarulf's ruling: with Sarulf gone, no other permanents are exiled).
 */
public record ExileOtherNonlandPermanentsWithManaValueAtMostEffect(DynamicAmount maximumManaValue)
        implements CardEffect {
}
