package com.github.laxika.magicalvibes.model.effect;

public record UntapTargetPermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
