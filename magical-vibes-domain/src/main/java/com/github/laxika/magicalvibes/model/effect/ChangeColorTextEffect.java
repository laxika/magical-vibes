package com.github.laxika.magicalvibes.model.effect;

public record ChangeColorTextEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
