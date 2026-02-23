package com.github.laxika.magicalvibes.model.effect;

public record ReturnTargetPermanentToHandEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
