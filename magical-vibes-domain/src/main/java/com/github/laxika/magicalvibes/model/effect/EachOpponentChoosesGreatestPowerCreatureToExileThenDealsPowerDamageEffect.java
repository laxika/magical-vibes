package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/**
 * Each opponent chooses one of their creatures tied for greatest power and exiles it. If the
 * supplied condition is met, the spell then deals each opponent damage equal to that creature's
 * power.
 */
public record EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect(
        Condition damageCondition) implements CardEffect {

    public EachOpponentChoosesGreatestPowerCreatureToExileThenDealsPowerDamageEffect {
        Objects.requireNonNull(damageCondition, "damageCondition");
    }
}
