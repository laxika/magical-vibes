package com.github.laxika.magicalvibes.model.effect;

/** Shuffles a targeted face-up exiled card into its owner's library. */
public record ShuffleTargetCardFromExileIntoOwnersLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.exileCard());
    }
}
