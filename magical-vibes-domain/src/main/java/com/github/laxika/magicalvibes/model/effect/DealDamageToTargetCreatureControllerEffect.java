package com.github.laxika.magicalvibes.model.effect;

public record DealDamageToTargetCreatureControllerEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
