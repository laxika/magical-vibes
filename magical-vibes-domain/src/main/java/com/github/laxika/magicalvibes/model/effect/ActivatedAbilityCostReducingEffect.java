package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability for static effects that reduce the generic activation cost of abilities on matching
 * permanents controlled by the effect's controller.
 */
public interface ActivatedAbilityCostReducingEffect extends CardEffect {

    /** The permanents whose activated abilities can be reduced. */
    PermanentPredicate affectedPermanents();

    /** Generic mana removed from a matching activated ability's cost. */
    int genericCostReduction();

    /** Whether this reduction applies to the particular ability being activated. */
    default boolean appliesTo(ActivatedAbility ability) {
        return true;
    }
}
