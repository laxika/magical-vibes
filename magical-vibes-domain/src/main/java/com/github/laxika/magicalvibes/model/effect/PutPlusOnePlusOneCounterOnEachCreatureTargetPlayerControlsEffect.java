package com.github.laxika.magicalvibes.model.effect;

/**
 * Put a +1/+1 counter on each creature the target player controls. Bind it to the spell's
 * player target group via {@code target(...).addEffect(...)} (e.g. Practiced Offense).
 */
public record PutPlusOnePlusOneCounterOnEachCreatureTargetPlayerControlsEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
