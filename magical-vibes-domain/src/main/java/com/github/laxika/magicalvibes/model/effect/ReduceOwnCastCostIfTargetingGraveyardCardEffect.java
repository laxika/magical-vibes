package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reduces this spell's casting cost by the given amount if its first target is a graveyard card
 * matching the predicate.
 */
public record ReduceOwnCastCostIfTargetingGraveyardCardEffect(CardPredicate predicate, int amount)
        implements GraveyardCardTargetCostReductionEffect {
}
