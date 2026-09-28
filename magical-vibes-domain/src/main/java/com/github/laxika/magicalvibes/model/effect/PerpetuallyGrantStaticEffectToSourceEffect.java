package com.github.laxika.magicalvibes.model.effect;

/** Perpetually adds a static effect to the source permanent's runtime card. */
public record PerpetuallyGrantStaticEffectToSourceEffect(CardEffect staticEffect) implements CardEffect {
}
