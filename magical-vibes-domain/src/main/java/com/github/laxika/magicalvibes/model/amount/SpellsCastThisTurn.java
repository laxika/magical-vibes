package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of spells cast this turn by the players in scope, optionally matching a filter. */
public record SpellsCastThisTurn(CardPredicate filter, CountScope scope) implements DynamicAmount {

    public SpellsCastThisTurn(CountScope scope) {
        this(null, scope);
    }
}
