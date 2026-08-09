package com.github.laxika.magicalvibes.model.effect;

/** Exile all but the bottom card of the target player's library. */
public record ExileAllButBottomCardOfTargetLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
