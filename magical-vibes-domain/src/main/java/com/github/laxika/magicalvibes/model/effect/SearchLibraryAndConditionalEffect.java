package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches a library for one card, then applies an effect only if the chosen card matches. */
public record SearchLibraryAndConditionalEffect(
        CardPredicate searchFilter,
        LibrarySearchDestination destination,
        CardPredicate selectedCardFilter,
        CardEffect conditionalEffect
) implements CardEffect {
}
