package com.github.laxika.magicalvibes.model.effect;

/**
 * A land-tap trigger that adds one mana of each type produced by the tapped land for every other
 * land with the same name controlled by the tapped land's controller.
 */
public record AddManaForEachOtherLandWithSameNameEffect() implements CardEffect {
}
