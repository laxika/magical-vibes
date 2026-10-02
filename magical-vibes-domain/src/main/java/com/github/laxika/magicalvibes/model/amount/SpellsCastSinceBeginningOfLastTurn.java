package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The number of matching spells cast this turn and during the immediately preceding turn. */
public record SpellsCastSinceBeginningOfLastTurn(CardPredicate filter, CountScope scope)
        implements DynamicAmount {
}
