package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller has at least {@code threshold} matching cards among their hand, graveyard, and
 * library.
 */
public record CardsInHandGraveyardAndLibraryMatchingAtLeast(int threshold, CardPredicate filter)
        implements Condition {

    @Override
    public String conditionName() {
        return "matching cards in hand, graveyard, and library (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching cards in hand, graveyard, and library";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
