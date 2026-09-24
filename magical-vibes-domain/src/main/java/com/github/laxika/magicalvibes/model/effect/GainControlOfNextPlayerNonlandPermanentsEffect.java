package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player gains permanent control of the nonland permanents controlled by the next player in
 * the chosen seating direction.
 */
public record GainControlOfNextPlayerNonlandPermanentsEffect(PlayerDirection direction)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
