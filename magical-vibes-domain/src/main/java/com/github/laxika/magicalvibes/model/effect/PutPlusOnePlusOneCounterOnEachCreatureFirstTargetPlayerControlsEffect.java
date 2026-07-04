package com.github.laxika.magicalvibes.model.effect;

/**
 * Put a +1/+1 counter on each creature the first target player controls.
 * Used by multi-target spells where targetIds[0] is a player (e.g. Practiced Offense).
 */
public record PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
