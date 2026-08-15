package com.github.laxika.magicalvibes.model.effect;

/** Exile every card from the target player's library. */
public record ExileTargetPlayerLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
