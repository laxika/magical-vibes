package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.UUID;

/**
 * Capability interface for static effects that reduce the generic mana portion of activated
 * abilities of matching permanents. The effect is symmetric unless its predicate narrows the
 * affected permanents or its owning card's wording provides another scope.
 */
public interface ActivatedAbilityCostReducingEffect extends CardEffect {

    /** The permanents whose activated abilities are cheaper. */
    PermanentPredicate affectedPermanents();

    /** Generic mana removed from a matching activated ability's cost when the reduction is fixed. */
    default int genericCostReduction() {
        return 0;
    }

    /** Dynamic generic mana reduction, evaluated against the permanent carrying this effect. */
    default DynamicAmount genericCostReductionAmount() {
        return null;
    }

    /** Whether this reduction applies to the particular ability being activated. */
    default boolean appliesTo(ActivatedAbility ability) {
        return true;
    }

    /** Whether this reduction applies to the ability and its chosen targets. */
    default boolean appliesTo(ActivatedAbility ability, UUID reducingPermanentId,
                              UUID targetId, List<UUID> targetIds) {
        return appliesTo(ability);
    }

    /** Whether this reduction applies symmetrically to abilities on every battlefield. */
    default boolean appliesSymmetrically() {
        return true;
    }
}
