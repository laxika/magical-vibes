package com.github.laxika.magicalvibes.model.effect;

/**
 * Put a -1/-1 counter on each creature target player controls.
 */
public record PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
