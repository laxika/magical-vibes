package com.github.laxika.magicalvibes.model.effect;

public record DealDamageToTargetCreatureEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
