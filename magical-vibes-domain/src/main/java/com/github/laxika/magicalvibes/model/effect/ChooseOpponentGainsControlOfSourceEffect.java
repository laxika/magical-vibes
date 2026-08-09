package com.github.laxika.magicalvibes.model.effect;

/**
 * The ability's controller chooses an opponent, then that player gains permanent control of the
 * source permanent.
 */
public record ChooseOpponentGainsControlOfSourceEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
