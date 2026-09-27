package com.github.laxika.magicalvibes.model.effect;

/** Gives the player in the chosen seating direction permanent control of the source permanent. */
public record NextPlayerGainsControlOfSourceEffect(PlayerDirection direction)
        implements CardEffect, ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
