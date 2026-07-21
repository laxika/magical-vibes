package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller's graveyard contains at least {@code threshold} nontoken cards
 * matching the predicate ({@code null} matches every card).
 */
public record GraveyardCardThreshold(int threshold, CardPredicate filter) implements Condition {

    @Override
    public String conditionName() {
        return "graveyard card threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards in graveyard";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
