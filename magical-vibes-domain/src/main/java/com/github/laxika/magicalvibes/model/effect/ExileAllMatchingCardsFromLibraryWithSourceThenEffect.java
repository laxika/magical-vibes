package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles every matching card from the controller's library and, if at least one card was exiled,
 * puts the supplied effect on the stack as a reflexive ability.
 */
public record ExileAllMatchingCardsFromLibraryWithSourceThenEffect(CardPredicate exileFilter,
                                                                    CardEffect thenEffect)
        implements CardEffect {
}
