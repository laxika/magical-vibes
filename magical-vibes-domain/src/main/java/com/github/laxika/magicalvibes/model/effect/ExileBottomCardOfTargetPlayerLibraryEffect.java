package com.github.laxika.magicalvibes.model.effect;

/** Exiles the bottom card of the targeted player's library. */
public record ExileBottomCardOfTargetPlayerLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
