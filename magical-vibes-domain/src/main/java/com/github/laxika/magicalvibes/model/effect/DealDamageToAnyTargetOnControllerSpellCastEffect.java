package com.github.laxika.magicalvibes.model.effect;

/**
 * Emblem marker for a fixed amount of damage to any target whenever its controller casts a spell.
 */
public record DealDamageToAnyTargetOnControllerSpellCastEffect(int damage) implements CardEffect {
}
