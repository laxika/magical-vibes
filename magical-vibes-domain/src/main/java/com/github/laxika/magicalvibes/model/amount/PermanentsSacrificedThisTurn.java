package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of matching permanents sacrificed by players in the given scope this turn. */
public record PermanentsSacrificedThisTurn(CardPredicate filter, CountScope scope,
                                           boolean excludeCurrentCastSacrifices) implements DynamicAmount {

    public PermanentsSacrificedThisTurn() {
        this(null, CountScope.CONTROLLER, false);
    }

    public PermanentsSacrificedThisTurn(CountScope scope) {
        this(null, scope, false);
    }

    public PermanentsSacrificedThisTurn(CardPredicate filter, CountScope scope) {
        this(filter, scope, false);
    }
}
