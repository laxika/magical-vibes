package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Ozolith, the Shattered Spire-style replacement for +1/+1 counters put on artifacts or creatures. */
public record AddOnePlusOneCountersToArtifactsOrCreaturesEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE && count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean affectedPermanentIsArtifact) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE
                && (affectedPermanentIsCreature || affectedPermanentIsArtifact);
    }
}
