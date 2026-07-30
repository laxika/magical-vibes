package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Each player's life total becomes N." The all-players counterpart of
 * {@link SetControllerLifeToAmountEffect}; the amount is evaluated once, from the effect
 * controller's point of view, and applied to every player. Used by Worldfire
 * ({@code new Fixed(1)}).
 *
 * @param amount the amount every player's life total is set to
 */
public record SetEachPlayerLifeToAmountEffect(DynamicAmount amount) implements CardEffect {
}
