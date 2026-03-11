package com.github.laxika.magicalvibes.model.effect;

public record DealDamageToTargetCreatureEffect(int damage, boolean unpreventable) implements CardEffect {

    public DealDamageToTargetCreatureEffect(int damage) {
        this(damage, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
