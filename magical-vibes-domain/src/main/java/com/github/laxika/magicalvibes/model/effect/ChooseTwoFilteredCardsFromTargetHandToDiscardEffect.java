package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Target opponent reveals their hand. The controller chooses one card matching {@code firstFilter}
 * (if any) and one card matching {@code secondFilter} (if any); that player discards those cards.
 * Each band is independent — if a band has no match it is skipped; if both match, both must be
 * chosen. Distended Mindbender (nonland MV ≤ 3, then MV ≥ 4).
 */
public record ChooseTwoFilteredCardsFromTargetHandToDiscardEffect(
        CardPredicate firstFilter, CardPredicate secondFilter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
