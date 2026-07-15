package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove a {counter} counter from target creature. If you do, you gain {lifeGain} life."
 * Removes a single counter of the given type from the target; the controller only gains life
 * if a counter was actually removed. Used by Woeleecher (MINUS_ONE_MINUS_ONE, 2).
 */
public record RemoveCounterFromTargetAndGainLifeEffect(CounterType counterType, int lifeGain) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
