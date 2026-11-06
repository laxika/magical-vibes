package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered punisher effect (Prince of Thralls, Nazgûl Battle-Mace): a permanent an opponent controlled was put into
 * a graveyard. That opponent may pay {@code lifeCost} life; if they don't (or can't), the ability's controller puts
 * that card onto the battlefield under their control, tracked as a stolen permanent so it returns to its owner on
 * death. "That card" is the stack entry's triggering card and "that opponent" the controller of its triggering
 * permanent as it last existed on the battlefield.
 *
 * @param lifeCost how much life "that opponent" may pay to keep the permanent from being stolen
 */
public record StealDyingOpponentPermanentUnlessPaysLifeEffect(int lifeCost) implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
