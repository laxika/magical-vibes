package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Mowu, Loyal Companion: if one or more +1/+1 counters would be put on this permanent, that many
 * plus one +1/+1 counters are put on it instead.
 */
public record AddOnePlusOneCountersToSourceEffect() implements CounterReplacementEffect {

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
                && sourcePermanent != null
                && affectedPermanent != null
                && sourcePermanent.getId().equals(affectedPermanent.getId());
    }

    @Override
    public boolean appliesToWhenEntering(CounterType counterType, boolean enteringPermanentIsCreature,
                                         boolean enteringPermanentIsArtifact, Permanent enteringPermanent) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE && enteringPermanent != null;
    }
}
