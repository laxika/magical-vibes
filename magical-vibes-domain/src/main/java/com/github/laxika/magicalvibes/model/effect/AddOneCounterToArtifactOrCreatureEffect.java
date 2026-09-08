package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Winding Constrictor-style replacement for counters put on artifacts or creatures. */
public record AddOneCounterToArtifactOrCreatureEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count + 1 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean affectedPermanentIsArtifact) {
        return affectedPermanentIsCreature || affectedPermanentIsArtifact;
    }
}
