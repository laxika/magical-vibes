package com.github.laxika.magicalvibes.model.effect;

public record ExileTargetPermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
