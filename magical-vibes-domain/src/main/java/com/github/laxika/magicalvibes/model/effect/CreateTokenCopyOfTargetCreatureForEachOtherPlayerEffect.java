package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the targeted creature for each player other than its controller. */
public record CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
