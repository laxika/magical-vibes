package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Sets the effect's controller's life total to a computed amount.
 * E.g. Form of the Dragon: "your life total becomes 5" ({@code new Fixed(5)}), or
 * Invincible Hymn: "your life total becomes [the number of cards in your library]".
 *
 * @param amount the amount to set the controller's life total to
 */
public record SetControllerLifeToAmountEffect(DynamicAmount amount) implements CardEffect {
}
