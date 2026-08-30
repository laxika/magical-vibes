package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of matching spells cast this turn by the players in scope. */
public record SpellsCastThisTurn(CardPredicate filter, CountScope scope) implements DynamicAmount {

    /** Counts all spells, preserving the original unfiltered form. */
    public SpellsCastThisTurn(CountScope scope) {
        this(null, scope);
    }
}
