package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper for ally-creature-enters triggers: the wrapped effect only fires
 * if the entering creature has power >= {@code minPower}.
 * <p>
 * Used by cards like Garruk's Packleader ("whenever another creature you control with
 * power 3 or greater enters, you may draw a card") and similar "big creature" triggers.
 */
public record EnteringCreatureMinPowerConditionalEffect(
        int minPower,
        CardEffect wrapped
) implements CardEffect {
}
