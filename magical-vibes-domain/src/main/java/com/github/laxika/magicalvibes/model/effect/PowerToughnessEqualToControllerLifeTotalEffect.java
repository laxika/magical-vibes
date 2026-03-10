package com.github.laxika.magicalvibes.model.effect;

/**
 * Characteristic-defining ability: this creature's power and toughness are each
 * equal to its controller's life total (static, e.g. Ajani Goldmane's Avatar token,
 * Serra Avatar).
 */
public record PowerToughnessEqualToControllerLifeTotalEffect() implements CardEffect {
    @Override
    public boolean isPowerToughnessDefining() { return true; }
}
