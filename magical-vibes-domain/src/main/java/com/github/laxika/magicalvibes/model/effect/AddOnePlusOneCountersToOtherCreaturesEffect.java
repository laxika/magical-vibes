package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Benevolent Hydra: adds one +1/+1 counter when counters would be put on another creature
 * controlled by the effect's controller.
 */
public record AddOnePlusOneCountersToOtherCreaturesEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature) {
        return false;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean affectedPermanentIsArtifact, Permanent sourcePermanent,
                             Permanent affectedPermanent) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE
                && affectedPermanentIsCreature
                && sourcePermanent != null
                && affectedPermanent != null
                && !sourcePermanent.getId().equals(affectedPermanent.getId());
    }
}
