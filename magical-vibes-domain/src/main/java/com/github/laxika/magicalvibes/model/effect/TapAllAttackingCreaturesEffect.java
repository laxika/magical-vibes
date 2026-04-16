package com.github.laxika.magicalvibes.model.effect;

/**
 * Effect: "Tap all attacking creatures."
 *
 * <p>Iterates all permanents across all battlefields, taps each creature
 * that is currently attacking. Used by fog variants with fateful hour
 * (e.g. Clinging Mists).</p>
 */
public record TapAllAttackingCreaturesEffect() implements CardEffect {
}
