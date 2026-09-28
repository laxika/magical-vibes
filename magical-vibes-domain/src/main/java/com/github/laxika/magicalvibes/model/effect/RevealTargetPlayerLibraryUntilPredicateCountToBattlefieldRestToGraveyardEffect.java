package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals a target player's library until the specified number of cards match the predicate.
 * Matching cards enter the controller's battlefield, and the remaining revealed cards go to the
 * target player's graveyard.
 */
public record RevealTargetPlayerLibraryUntilPredicateCountToBattlefieldRestToGraveyardEffect(
        DynamicAmount requiredCount,
        CardPredicate predicate
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
