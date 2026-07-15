package com.github.laxika.magicalvibes.model.effect;

public record TapOrUntapTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
