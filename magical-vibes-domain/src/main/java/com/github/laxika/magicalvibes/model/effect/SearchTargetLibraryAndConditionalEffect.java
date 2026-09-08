package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches a target player's library, then applies an effect based on the selected card. */
public record SearchTargetLibraryAndConditionalEffect(
        CardPredicate searchFilter,
        LibrarySearchDestination destination,
        CardPredicate selectedCardFilter,
        CardEffect conditionalEffect
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
