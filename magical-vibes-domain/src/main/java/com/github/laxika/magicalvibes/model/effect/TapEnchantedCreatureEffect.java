package com.github.laxika.magicalvibes.model.effect;

/**
 * Taps the creature that the source aura is attached to.
 * Used by auras with activated abilities like "{1}: Tap enchanted creature."
 * Resolution finds the aura via the stack entry's {@code sourcePermanentId},
 * then taps the creature the aura is attached to.
 */
public record TapEnchantedCreatureEffect() implements CardEffect {
}
