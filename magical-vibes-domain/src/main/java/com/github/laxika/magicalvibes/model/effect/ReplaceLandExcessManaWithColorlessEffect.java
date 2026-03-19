package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: if a land is tapped for two or more mana, it produces {C} instead
 * of any other type and amount. Used by Damping Sphere.
 */
public record ReplaceLandExcessManaWithColorlessEffect() implements CardEffect {
}
