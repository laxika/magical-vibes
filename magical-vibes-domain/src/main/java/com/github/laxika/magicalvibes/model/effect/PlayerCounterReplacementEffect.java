package com.github.laxika.magicalvibes.model.effect;

/** Replacement behavior for counters a player would get. */
public interface PlayerCounterReplacementEffect extends CardEffect {

    int replace(int count);

    /**
     * Whether this replacement applies to counters put on any player, rather than only the
     * controller of the permanent carrying the effect.
     */
    default boolean appliesToAllPlayers() {
        return false;
    }
}
