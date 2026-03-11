package com.github.laxika.magicalvibes.model.effect;

public record ExileTargetPermanentAndReturnAtEndStepEffect(boolean returnTapped) implements CardEffect {
    public ExileTargetPermanentAndReturnAtEndStepEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
