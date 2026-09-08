package com.github.laxika.magicalvibes.model.effect;

/**
 * A mana ability that asks its controller to choose a color, then adds that many mana of the
 * chosen color as the number of distinct powers among creatures they control.
 */
public record AwardManaOfChosenColorEqualToDistinctCreaturePowersEffect() implements ManaProducingEffect {
}
