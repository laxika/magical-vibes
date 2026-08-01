package com.github.laxika.magicalvibes.model.effect;

/**
 * Each creature on the battlefield deals {@code damage} damage to its controller.
 * Each creature is its own damage source (Rakdos Charm mode 3).
 */
public record EachCreatureDealsDamageToItsControllerEffect(int damage) implements CardEffect {
}
