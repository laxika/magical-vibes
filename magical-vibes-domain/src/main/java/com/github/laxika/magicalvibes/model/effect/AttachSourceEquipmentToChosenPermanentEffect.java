package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the source Equipment to the permanent remembered by the resolving stack entry.
 * Used by effects such as amass that choose a permanent before attaching the Equipment.
 */
public record AttachSourceEquipmentToChosenPermanentEffect() implements CardEffect {
}
