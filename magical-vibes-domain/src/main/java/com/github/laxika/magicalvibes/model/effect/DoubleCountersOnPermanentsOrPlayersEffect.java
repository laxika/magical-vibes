package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Vorinclex's replacement effect that doubles counters put by its controller. */
public record DoubleCountersOnPermanentsOrPlayersEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count * 2 : count;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean sourceControlsAffectedPermanent,
                             boolean sourceControllerIsPlacingPlayer,
                             boolean affectedObjectIsPlayer) {
        return sourceControllerIsPlacingPlayer;
    }
}
