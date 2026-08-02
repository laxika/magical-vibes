package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time cipher choice for an instant or sorcery. The chosen creature is not a spell
 * target; the effect opens a separate creature choice and leaves the spell in exile when the
 * choice is completed.
 */
public record CipherEncodeEffect() implements CardEffect {
}
