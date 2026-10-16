package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The number of matching spells cast this turn by the players in scope. When
 * {@code excludeResolvingSpell} is true the spell currently resolving is not counted ("each other
 * spell you've cast this turn"); a copy of that spell was never cast, so it excludes nothing.
 */
public record SpellsCastThisTurn(CardPredicate filter, CountScope scope, boolean excludeResolvingSpell)
        implements DynamicAmount {

    /** Counts all spells, preserving the original unfiltered form. */
    public SpellsCastThisTurn(CountScope scope) {
        this(null, scope, false);
    }

    public SpellsCastThisTurn(CardPredicate filter, CountScope scope) {
        this(filter, scope, false);
    }
}
