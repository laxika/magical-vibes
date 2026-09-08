package com.github.laxika.magicalvibes.model.effect;

/** Winding Constrictor-style replacement for counters a player would get. */
public record AddOnePlayerCounterEffect() implements PlayerCounterReplacementEffect {

    @Override
    public int replace(int count) {
        return count > 0 ? count + 1 : count;
    }
}
