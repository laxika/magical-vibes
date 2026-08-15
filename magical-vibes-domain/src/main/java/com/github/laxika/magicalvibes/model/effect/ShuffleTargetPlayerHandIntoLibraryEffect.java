package com.github.laxika.magicalvibes.model.effect;

/** Put the target player's hand into their library, then shuffle that library. */
public record ShuffleTargetPlayerHandIntoLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
