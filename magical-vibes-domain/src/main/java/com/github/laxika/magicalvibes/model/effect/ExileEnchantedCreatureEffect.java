package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the creature the source Aura is attached to, without any additional effect.
 * The exile variant of {@link SacrificeEnchantedCreatureEffect} — resolution finds the Aura via
 * the stack entry's {@code sourcePermanentId}, then exiles the creature it is attached to.
 *
 * <p>Used by Weight of Conscience ("Tap two untapped creatures you control that share a creature
 * type: Exile enchanted creature.").</p>
 */
public record ExileEnchantedCreatureEffect() implements CardEffect {
}
