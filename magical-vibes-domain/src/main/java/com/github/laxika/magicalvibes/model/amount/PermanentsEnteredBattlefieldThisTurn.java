package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The number of matching permanents that entered under players in the given scope this turn. When
 * {@code stillOnBattlefield} is set, only those that are still on the battlefield under such a player
 * are counted ("creatures you control that entered this turn", Tromell, Seymour's Butler).
 */
public record PermanentsEnteredBattlefieldThisTurn(CardPredicate filter, CountScope scope,
                                                   boolean stillOnBattlefield)
        implements DynamicAmount {

    public PermanentsEnteredBattlefieldThisTurn(CardPredicate filter, CountScope scope) {
        this(filter, scope, false);
    }
}
