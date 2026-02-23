package com.github.laxika.magicalvibes.model.effect;

public record DealDamageToTargetPlayerEffect(int damage) implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
