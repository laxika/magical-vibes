package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Capability for static effects that add loyalty counters to the cost of loyalty abilities.
 */
public interface LoyaltyAbilityCostIncreasingEffect extends CardEffect {

    /** The permanents whose loyalty abilities receive the additional counter cost. */
    PermanentPredicate affectedPermanents();

    /** Additional loyalty counters added to the activation cost. */
    int additionalLoyaltyCost();

    /** Whether this effect applies to the activation being evaluated. */
    default boolean appliesTo(ActivatedAbility ability, UUID activatingPlayerId, UUID sourceControllerId) {
        return ability != null && ability.getLoyaltyCost() != null
                && activatingPlayerId != null && activatingPlayerId.equals(sourceControllerId);
    }
}
