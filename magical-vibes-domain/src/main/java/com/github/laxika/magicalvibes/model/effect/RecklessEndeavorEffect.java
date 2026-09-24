package com.github.laxika.magicalvibes.model.effect;

/** Rolls two d12s, then uses one result for creature damage and the other for Treasures. */
public record RecklessEndeavorEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
