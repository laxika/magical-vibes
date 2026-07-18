package com.github.laxika.magicalvibes.model.amount;

/**
 * The {@code whileAttacking} amount when the source permanent is attacking; the {@code otherwise}
 * amount when it is not. Models characteristic-defining wordings that switch on the source's combat
 * state (Gaea's Liege: "As long as it isn't attacking, its power and toughness are each equal to the
 * number of Forests you control. As long as it is attacking, they are each equal to the number of
 * Forests defending player controls."). Evaluates {@code otherwise} when there is no source.
 */
public record IfSourceAttacking(DynamicAmount whileAttacking, DynamicAmount otherwise) implements DynamicAmount {
}
