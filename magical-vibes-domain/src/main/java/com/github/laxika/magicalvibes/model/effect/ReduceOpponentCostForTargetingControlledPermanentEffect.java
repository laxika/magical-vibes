package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the cost of spells that target a permanent matching the predicate and controlled by
 * this effect's controller. By default, only opponent spells are affected; the three-argument
 * constructor can also include spells cast by this effect's controller.
 */
public record ReduceOpponentCostForTargetingControlledPermanentEffect(
        PermanentPredicate predicate, int amount, boolean affectsController) implements CardEffect {

    public ReduceOpponentCostForTargetingControlledPermanentEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, false);
    }
}
