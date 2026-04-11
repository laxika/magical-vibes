package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards a card at random. If they can't (empty hand), the controller draws a card instead.
 */
public record TargetPlayerRandomDiscardOrControllerDrawsEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
