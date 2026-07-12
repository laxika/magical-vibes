package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Static global restriction: creatures whose power is greater than {@code amount} can't attack.
 * The amount is evaluated from the source permanent's controller (a
 * {@link com.github.laxika.magicalvibes.service.effect.AmountContext#forStaticEffect} context),
 * so scope-dependent counts resolve relative to the source's controller.
 * Example: Ensnaring Bridge — "Creatures with power greater than the number of cards in your hand
 * can't attack" ({@code amount = new CardsInHand(CountScope.CONTROLLER)}).
 *
 * @param amount the power threshold; creatures with strictly greater power can't attack
 */
public record CreaturesWithPowerGreaterThanAmountCantAttackEffect(DynamicAmount amount) implements CardEffect {
}
