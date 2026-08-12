package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals a target player's library until a card matches {@code predicate} or the required count
 * of cards has been revealed. A matching card is put onto the controller's battlefield and the
 * remaining revealed cards are put into the target player's graveyard.
 */
public record RevealTargetPlayerLibraryUntilPredicateOrCountToBattlefieldRestToGraveyardEffect(
        DynamicAmount requiredCount,
        CardPredicate predicate
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
