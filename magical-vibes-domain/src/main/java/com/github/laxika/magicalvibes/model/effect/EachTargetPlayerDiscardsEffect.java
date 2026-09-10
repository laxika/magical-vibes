package com.github.laxika.magicalvibes.model.effect;

/** Makes each selected target player discard a fixed number of cards. */
public record EachTargetPlayerDiscardsEffect(int amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
