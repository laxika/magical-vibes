package com.github.laxika.magicalvibes.model.effect;

/** Resolves the digital heist mechanic against the targeted player's library. */
public record HeistTargetLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
