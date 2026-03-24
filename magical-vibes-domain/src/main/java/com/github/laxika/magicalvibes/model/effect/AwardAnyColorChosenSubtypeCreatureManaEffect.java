package com.github.laxika.magicalvibes.model.effect;

/**
 * Produces one mana of any color (player chooses) that can only be spent to cast
 * a creature spell of the source permanent's chosen creature type.
 * Used by Pillar of Origins, Unclaimed Territory, etc.
 */
public record AwardAnyColorChosenSubtypeCreatureManaEffect() implements ManaProducingEffect {
}
