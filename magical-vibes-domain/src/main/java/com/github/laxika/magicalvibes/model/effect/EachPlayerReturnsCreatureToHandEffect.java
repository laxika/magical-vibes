package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses one creature they control in active-player order, then all chosen creatures
 * return to their owners' hands.
 */
public record EachPlayerReturnsCreatureToHandEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
