package com.github.laxika.magicalvibes.model.effect;

public record TapOrUntapTargetPermanentEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
