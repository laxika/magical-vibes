package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches a library for one card, then applies an effect only if the chosen card matches. */
public record SearchLibraryAndConditionalEffect(
        CardPredicate searchFilter,
        LibrarySearchDestination destination,
        CardPredicate selectedCardFilter,
        CardEffect conditionalEffect,
        ManaValueBound manaValueBound,
        boolean shuffleAfterSelection
) implements CardEffect {
    public SearchLibraryAndConditionalEffect(CardPredicate searchFilter, LibrarySearchDestination destination, CardPredicate selectedCardFilter, CardEffect conditionalEffect, boolean shuffleAfterSelection) {
        this(searchFilter, destination, selectedCardFilter, conditionalEffect, null, shuffleAfterSelection);
    }

    public SearchLibraryAndConditionalEffect(CardPredicate searchFilter, LibrarySearchDestination destination,
                                             CardPredicate selectedCardFilter, CardEffect conditionalEffect) {
        this(searchFilter, destination, selectedCardFilter, conditionalEffect, null, true);
    }

    public SearchLibraryAndConditionalEffect(CardPredicate searchFilter, LibrarySearchDestination destination,
                                             CardPredicate selectedCardFilter, CardEffect conditionalEffect,
                                             ManaValueBound manaValueBound, boolean shuffleAfterSelection) {
        this.searchFilter = searchFilter;
        this.destination = destination;
        this.selectedCardFilter = selectedCardFilter;
        this.conditionalEffect = conditionalEffect;
        this.manaValueBound = manaValueBound;
        this.shuffleAfterSelection = shuffleAfterSelection;
    }
}
