package com.github.laxika.magicalvibes.model.effect;

/** Each player taps white creatures, chooses Plains, then the remaining Plains are destroyed. */
public record RaidingPartyEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
