package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Reduces this spell's own casting cost by the evaluated {@link DynamicAmount} of generic mana.
 * The single spell-self cost-reduction effect: fixed reductions use {@code new Fixed(n)},
 * "for each …" reductions use a counting amount (e.g. {@code CardsInGraveyard},
 * {@code PermanentCount}). Conditional reductions ("costs {N} less to cast if …") wrap this in a
 * {@link ConditionalEffect}.
 */
public record ReduceOwnCastCostEffect(DynamicAmount amount) implements CardEffect {
}
