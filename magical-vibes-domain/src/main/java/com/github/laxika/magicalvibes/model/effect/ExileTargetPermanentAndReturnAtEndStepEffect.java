package com.github.laxika.magicalvibes.model.effect;

public record ExileTargetPermanentAndReturnAtEndStepEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
