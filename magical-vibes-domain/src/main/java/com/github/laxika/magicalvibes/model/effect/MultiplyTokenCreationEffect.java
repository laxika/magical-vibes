package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that multiplies the number of tokens created under the controller's control.
 * Used by Parallel Lives (multiplier=2), Doubling Season (token half, multiplier=2), and similar cards.
 * Unlike {@link DoubleDamageEffect} which is global, this only applies to the controller's
 * own token creation — multiple instances on the battlefield stack multiplicatively.
 *
 * @param multiplier the factor by which token creation is multiplied (e.g. 2 for doubling)
 */
public record MultiplyTokenCreationEffect(int multiplier) implements CardEffect {
}
