package com.github.laxika.magicalvibes.model.effect;

/**
 * A mana ability that asks its controller to choose a color, then adds mana of that color equal
 * to that color's devotion.
 */
public record AwardManaOfChosenColorEqualToDevotionEffect() implements ManaProducingEffect {
}
