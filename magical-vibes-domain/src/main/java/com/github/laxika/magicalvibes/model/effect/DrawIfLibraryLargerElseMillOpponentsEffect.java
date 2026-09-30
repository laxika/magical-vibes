package com.github.laxika.magicalvibes.model.effect;

/** One targeted end-step ability whose result depends on the two libraries at resolution. */
public record DrawIfLibraryLargerElseMillOpponentsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
