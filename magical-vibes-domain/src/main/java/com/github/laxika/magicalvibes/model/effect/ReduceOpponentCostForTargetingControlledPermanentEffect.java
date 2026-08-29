package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the cost of spells an opponent casts that target a permanent matching the predicate
 * and controlled by this effect's controller.
 */
public record ReduceOpponentCostForTargetingControlledPermanentEffect(
        PermanentPredicate predicate, int amount) implements CardEffect {
}
