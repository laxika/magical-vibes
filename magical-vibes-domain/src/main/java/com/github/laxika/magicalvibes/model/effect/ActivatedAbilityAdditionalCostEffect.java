package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for static effects that add a non-mana cost to activated abilities of matching
 * permanents. The cost is symmetric and applies to abilities of matching permanents on any
 * battlefield.
 */
public interface ActivatedAbilityAdditionalCostEffect extends CardEffect {

    /** The permanents whose activated abilities receive the additional cost. */
    PermanentPredicate affectedPermanents();

    /** The cost added to each matching activated ability. */
    CostEffect additionalCost();
}
