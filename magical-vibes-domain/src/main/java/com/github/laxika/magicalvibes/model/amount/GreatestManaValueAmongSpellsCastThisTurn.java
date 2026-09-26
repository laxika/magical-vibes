package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The greatest mana value among matching spells cast in the scoped players' current turn history. */
public record GreatestManaValueAmongSpellsCastThisTurn(CardPredicate filter, CountScope scope)
        implements DynamicAmount {
}
