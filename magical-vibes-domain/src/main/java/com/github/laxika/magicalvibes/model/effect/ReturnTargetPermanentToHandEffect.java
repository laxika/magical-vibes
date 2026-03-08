package com.github.laxika.magicalvibes.model.effect;

public record ReturnTargetPermanentToHandEffect(int lifeLoss) implements CardEffect {

    public ReturnTargetPermanentToHandEffect() {
        this(0);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
