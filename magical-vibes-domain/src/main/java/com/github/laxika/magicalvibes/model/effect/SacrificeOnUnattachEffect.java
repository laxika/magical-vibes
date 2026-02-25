package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for equipment that forces sacrifice of the previously-equipped permanent
 * whenever the equipment becomes unattached (re-equip, spell-based unattach, or equipment
 * leaves the battlefield).
 */
public record SacrificeOnUnattachEffect() implements CardEffect {
}
