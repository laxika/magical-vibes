package com.github.laxika.magicalvibes.model.effect;

/**
 * Increases the controller's devotion to every color and color combination by the given amount.
 */
public record IncreaseDevotionEffect(int amount) implements CardEffect {
}
