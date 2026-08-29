package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Resolves to a global block tax lasting until end of turn.
 */
public record RequirePaymentToBlockThisTurnEffect(DynamicAmount amountPerBlocker) implements CardEffect {
}
