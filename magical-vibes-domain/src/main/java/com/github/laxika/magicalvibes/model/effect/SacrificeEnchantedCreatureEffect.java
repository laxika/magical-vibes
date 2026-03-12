package com.github.laxika.magicalvibes.model.effect;

/**
 * Upkeep trigger for auras that sacrifice the enchanted creature without any additional effect.
 * Used by cards like Necrotic Plague where the enchanted creature is sacrificed at the beginning
 * of its controller's upkeep.
 *
 * <p>The enchanted creature's controller sacrifices it (not the aura's controller, unless they
 * are the same). Resolution finds the aura via the stack entry's {@code sourcePermanentId},
 * then sacrifices the creature the aura is attached to.</p>
 */
public record SacrificeEnchantedCreatureEffect() implements CardEffect {
}
