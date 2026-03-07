package com.github.laxika.magicalvibes.model.effect;

public record BoostSecondTargetCreatureEffect(int powerBoost, int toughnessBoost) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
