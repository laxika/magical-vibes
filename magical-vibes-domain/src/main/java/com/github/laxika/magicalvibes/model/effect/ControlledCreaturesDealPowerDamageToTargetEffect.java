package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each creature the controller controls that matches {@code filter} deals damage equal to its
 * power to target creature. Each matching creature is its own damage source (CR 608.2h).
 * No tapping, no damage back — contrast {@link PackHuntEffect}.
 *
 * <p>Used by Moonlight Hunt ({@code PermanentHasAnySubtypePredicate(WOLF, WEREWOLF)}).
 */
public record ControlledCreaturesDealPowerDamageToTargetEffect(PermanentPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
