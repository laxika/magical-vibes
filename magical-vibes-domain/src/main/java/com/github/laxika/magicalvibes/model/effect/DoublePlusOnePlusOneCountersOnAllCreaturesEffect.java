package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Primal Vigor's global replacement for +1/+1 counters put on creatures. */
public record DoublePlusOnePlusOneCountersOnAllCreaturesEffect() implements CounterReplacementEffect, DoublingEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count * 2 : count;
    }

    @Override
    public boolean appliesToAllPermanents() {
        return true;
    }

    @Override
    public boolean appliesTo(CounterType counterType, boolean affectedPermanentIsCreature,
                             boolean affectedPermanentIsArtifact) {
        return counterType == CounterType.PLUS_ONE_PLUS_ONE && affectedPermanentIsCreature;
    }
}
