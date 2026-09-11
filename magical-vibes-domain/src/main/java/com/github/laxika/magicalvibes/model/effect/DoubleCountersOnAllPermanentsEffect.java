package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Global replacement effect that doubles counters put on any permanent. */
public record DoubleCountersOnAllPermanentsEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count * 2 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean sourceControlsAffectedPermanent,
                             boolean sourceControllerIsPlacingPlayer,
                             boolean affectedObjectIsPlayer) {
        return !affectedObjectIsPlayer;
    }
}
