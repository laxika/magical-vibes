package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Capability interface for static effects that reduce the generic mana portion of activated
 * abilities of matching permanents. The effect is symmetric unless its predicate narrows the
 * affected permanents or its owning card's wording provides another scope.
 */
public interface ActivatedAbilityCostReducingEffect extends CardEffect {

    /** The permanents whose activated abilities are cheaper. */
    PermanentPredicate affectedPermanents();

    /** Generic mana removed from a matching activated ability's cost. */
    int genericCostReduction();

    /** Whether this reduction applies to the particular ability being activated. */
    default boolean appliesTo(ActivatedAbility ability) {
        return true;
    }

    /** Whether this reduction applies symmetrically to abilities on every battlefield. */
    default boolean appliesSymmetrically() {
        return true;
    }
}
