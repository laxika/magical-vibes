package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses one permanent they control in active-player order, then all chosen
 * permanents return to their owners' hands.
 */
public record EachPlayerReturnsPermanentToHandEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
