package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Adds a non-mana cost to activated abilities of matching permanents for all players. */
public record AddActivatedAbilityCostEffect(PermanentPredicate predicate, CostEffect additionalCost)
        implements ActivatedAbilityAdditionalCostEffect {

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }
}
