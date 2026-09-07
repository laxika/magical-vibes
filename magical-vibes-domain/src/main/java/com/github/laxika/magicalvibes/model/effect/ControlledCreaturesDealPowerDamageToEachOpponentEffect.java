package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each creature controlled by the ability's controller that matches {@code filter} deals damage
 * equal to its power to each opponent.
 */
public record ControlledCreaturesDealPowerDamageToEachOpponentEffect(
        PermanentPredicate filter
) implements CardEffect {
}
