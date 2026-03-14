package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards X cards at random, where X is the X value paid when casting the spell.
 */
public record TargetPlayerRandomDiscardXEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
}
