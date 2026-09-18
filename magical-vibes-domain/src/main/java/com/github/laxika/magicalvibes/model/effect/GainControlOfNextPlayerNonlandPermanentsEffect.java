package com.github.laxika.magicalvibes.model.effect;

/**
 * Rotates control of all nonland permanents around the table in the chosen direction.
 * The source permanent is excluded by the resolution handler.
 */
public record GainControlOfNextPlayerNonlandPermanentsEffect(Direction direction)
        implements CardEffect, ControlStealingEffect {

    public enum Direction {
        LEFT,
        RIGHT
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
