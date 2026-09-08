package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants additional loyalty counters to a planeswalker spell as it enters the battlefield.
 * The grant is recorded on the spell's stack entry while the spell is still resolving.
 */
public record GrantAdditionalLoyaltyCountersToCastSpellEffect(int amount) implements CardEffect {
}
