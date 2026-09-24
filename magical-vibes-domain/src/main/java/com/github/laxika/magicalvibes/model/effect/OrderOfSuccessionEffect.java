package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the controller, each player chooses a creature controlled by the next player in
 * the chosen direction, then each player gains control of the creature they chose.
 */
public record OrderOfSuccessionEffect(
        GainControlOfNextPlayerNonlandPermanentsEffect.Direction direction)
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
