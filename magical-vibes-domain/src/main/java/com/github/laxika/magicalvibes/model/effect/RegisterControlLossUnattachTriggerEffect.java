package com.github.laxika.magicalvibes.model.effect;

public record RegisterControlLossUnattachTriggerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
