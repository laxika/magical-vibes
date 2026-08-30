package com.github.laxika.magicalvibes.model.effect;

/** Replacement behavior for counters a player would get. */
public interface PlayerCounterReplacementEffect extends CardEffect {

    int replace(int count);
}
