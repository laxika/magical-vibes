package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Innkeeper's Talent's level 3 replacement effect: doubles every kind of counter put on any
 * permanent or player.
 */
public record DoubleCountersOnAllPermanentsAndPlayersEffect()
        implements CounterReplacementEffect, PlayerCounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count * 2 : count;
    }

    @Override
    public int replace(int count) {
        return count > 0 ? count * 2 : count;
    }

    @Override
    public boolean appliesToAllPermanents() {
        return true;
    }

    @Override
    public boolean appliesToAllPlayers() {
        return true;
    }
}
