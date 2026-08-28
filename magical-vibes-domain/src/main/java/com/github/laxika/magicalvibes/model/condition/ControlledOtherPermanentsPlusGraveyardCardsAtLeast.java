package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller's matching permanents other than the source, plus matching non-token cards in
 * that controller's graveyard, total at least {@code threshold}.
 */
public record ControlledOtherPermanentsPlusGraveyardCardsAtLeast(
        int threshold, PermanentPredicate permanentFilter, CardPredicate graveyardFilter) implements Condition {

    @Override
    public String conditionName() {
        return "controlled other permanents plus matching graveyard cards threshold (" + threshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + threshold + " matching other permanents plus graveyard cards";
    }
}
