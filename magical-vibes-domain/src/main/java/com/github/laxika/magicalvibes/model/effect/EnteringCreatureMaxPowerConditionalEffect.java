package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper for ally-creature-enters triggers: the wrapped effect only fires
 * if the entering creature has power <= {@code maxPower}.
 * <p>
 * Used by cards like Mentor of the Meek ("whenever another creature you control with
 * power 2 or less enters, you may pay {1}. If you do, draw a card") and similar
 * "small creature" triggers.
 */
public record EnteringCreatureMaxPowerConditionalEffect(
        int maxPower,
        CardEffect wrapped
) implements CardEffect {
}
