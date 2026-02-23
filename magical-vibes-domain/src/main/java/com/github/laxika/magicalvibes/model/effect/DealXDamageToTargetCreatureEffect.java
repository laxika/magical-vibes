package com.github.laxika.magicalvibes.model.effect;

public record DealXDamageToTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
