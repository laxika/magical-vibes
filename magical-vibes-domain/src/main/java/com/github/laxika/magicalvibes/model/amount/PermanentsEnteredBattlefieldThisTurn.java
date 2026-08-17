package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of matching permanents that entered under players in the given scope this turn. */
public record PermanentsEnteredBattlefieldThisTurn(CardPredicate filter, CountScope scope)
        implements DynamicAmount {
}
