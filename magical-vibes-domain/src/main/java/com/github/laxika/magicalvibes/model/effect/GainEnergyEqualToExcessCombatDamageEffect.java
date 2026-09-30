package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat-damage trigger that gives its controller energy equal to the excess combat damage dealt
 * to the creature damaged by the source during the same damage event.
 */
public record GainEnergyEqualToExcessCombatDamageEffect()
        implements CardEffect, CombatExcessDamageAwareEffect {
}
