package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each creature controlled by the ability's controller that matches {@code filter} deals damage
 * equal to its power to the target player or planeswalker.
 */
public record ControlledCreaturesDealPowerDamageToTargetPlayerOrPlaneswalkerEffect(
        PermanentPredicate filter
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.playerOrPlaneswalker());
    }
}
