package com.github.laxika.magicalvibes.model.effect;

/**
 * A mana ability that asks its controller to choose a color, then adds mana of that color equal
 * to the number of creatures they control carrying the source permanent's chosen subtype.
 */
public record AwardManaOfChosenSubtypeCreatureCountEffect() implements ManaProducingEffect {
}
