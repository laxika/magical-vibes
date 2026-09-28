package com.github.laxika.magicalvibes.model.effect;

/** Rolls two d6s, then uses one result for a power-based creature wipe and the other for Knights. */
public record ValiantEndeavorEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
