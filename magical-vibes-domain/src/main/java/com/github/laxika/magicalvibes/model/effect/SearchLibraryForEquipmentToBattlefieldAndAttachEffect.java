package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for an Equipment card, puts it onto the battlefield,
 * then lets the controller attach it to a creature they control, then shuffles.
 * Used by Stonehewer Giant.
 */
public record SearchLibraryForEquipmentToBattlefieldAndAttachEffect() implements CardEffect {
}
