package com.github.laxika.magicalvibes.model.effect;

/** Makes the targeted creature a permanent copy of a creature card exiled with the source. */
public record TargetCreatureBecomesCopyOfExiledCreatureWithSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
