package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Benevolent Hydra-style replacement: adds one +1/+1 counter to each positive +1/+1 counter
 * placement on another creature controlled by the source's controller.
 */
public record AddOnePlusOneCountersToOtherCreaturesEffect()
        implements PlusOnePlusOneCountersReplacementEffect {

    @Override
    public int replace(int count) {
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
