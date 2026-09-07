package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses any number of cards from their hand, puts them on the bottom of their
 * library, then draws one more card than the number chosen.
 */
public record PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect()
        implements CardEffect {
}
