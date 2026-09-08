package com.github.laxika.magicalvibes.model.effect;

public record CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
