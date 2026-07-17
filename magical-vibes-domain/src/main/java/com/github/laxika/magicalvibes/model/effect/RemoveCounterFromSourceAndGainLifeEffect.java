package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove a {counter} counter from this permanent. If you do, you gain {lifeGain} life."
 * Removes a single counter of the given type from the source permanent; the controller only gains
 * life if a counter was actually removed. Source analogue of
 * {@link RemoveCounterFromTargetAndGainLifeEffect}. Used by Living Artifact (VITALITY, 1) on an
 * upkeep "you may" ability (wrapped in {@link MayEffect}).
 */
public record RemoveCounterFromSourceAndGainLifeEffect(CounterType counterType, int lifeGain) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
