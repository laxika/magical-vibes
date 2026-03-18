package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy target creature. Its controller loses life equal to the number of creatures
 * that died this turn (across all players).
 * Used by Flesh Allergy (SOM #62).
 */
public record DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
