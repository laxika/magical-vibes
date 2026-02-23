package com.github.laxika.magicalvibes.model.effect;

public record BoostTargetCreatureEffect(int powerBoost, int toughnessBoost) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
