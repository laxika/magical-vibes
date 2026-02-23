package com.github.laxika.magicalvibes.model.effect;

public record MakeTargetUnblockableEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
