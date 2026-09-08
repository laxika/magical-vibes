package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Resolves to a global attack tax lasting until end of turn.
 */
public record RequirePaymentToAttackThisTurnEffect(DynamicAmount amountPerAttacker) implements CardEffect {
}
