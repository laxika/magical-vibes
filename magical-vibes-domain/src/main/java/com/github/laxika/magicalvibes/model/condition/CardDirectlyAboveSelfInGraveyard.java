package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The source card is in its controller's graveyard and the card immediately above it (the next
 * one put into that graveyard) matches {@code filter}. The graveyard is an ordered pile, so
 * "directly above" means exactly the next higher index than the source card. Contrast
 * {@link CardsAboveSelfInGraveyard}, which counts all cards anywhere above it.
 * Used by Krovikan Horror ("with a creature card directly above it").
 */
public record CardDirectlyAboveSelfInGraveyard(CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "matching card directly above self in graveyard";
    }

    @Override
    public String conditionNotMetReason() {
        return "no matching card directly above it in graveyard";
    }
}
