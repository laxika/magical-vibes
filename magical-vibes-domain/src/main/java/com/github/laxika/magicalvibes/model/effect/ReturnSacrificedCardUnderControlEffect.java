package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered ability effect that returns the sacrificed card carried by the trigger from its
 * owner's graveyard to the battlefield under the ability controller's control.
 */
public record ReturnSacrificedCardUnderControlEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
