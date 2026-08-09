package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for an Equipment trigger that taps a creature dealt damage by its equipped creature and
 * keeps that creature from untapping while the Equipment remains on the battlefield.
 *
 * <p>The trigger collector expands this marker into a tap effect and a source-linked untap lock,
 * so the marker itself is never resolved directly.
 */
public record EquipmentTapsAndLocksDamagedCreatureEffect() implements CardEffect {
}
