package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The source card is in its controller's graveyard with at least {@code threshold} cards
 * matching {@code filter} positioned above it (put into the graveyard more recently). The
 * graveyard is an ordered pile; "above" means a higher index than the source card.
 * Used by Nether Shadow ("with three or more creature cards above it").
 */
public record CardsAboveSelfInGraveyard(int threshold, CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "cards above self in graveyard (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards above it in graveyard";
    }
}
