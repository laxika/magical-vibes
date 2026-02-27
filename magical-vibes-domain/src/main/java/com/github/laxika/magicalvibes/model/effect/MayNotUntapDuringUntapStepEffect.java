package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "You may choose not to untap this permanent during your untap step."
 * During the untap step, if the permanent is tapped, the controller is prompted
 * whether to untap it or keep it tapped.
 */
public record MayNotUntapDuringUntapStepEffect() implements CardEffect {
}
