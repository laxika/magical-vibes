package com.github.laxika.magicalvibes.model.effect;

/**
 * Upkeep trigger for auras that sacrifice the enchanted creature without any additional effect.
 * Used by cards like Curse Artifact where the Aura itself has the triggered ability.
 *
 * <p>The enchanted creature's controller sacrifices it (not the aura's controller, unless they
 * are the same). Resolution finds the aura via the stack entry's {@code sourcePermanentId},
 * then sacrifices the creature the aura is attached to. An ability the Aura grants the creature
 * ("enchanted creature has 'At the beginning of your upkeep, sacrifice this creature'", Necrotic
 * Plague) is the creature's own, written with {@link SacrificeSelfEffect}.</p>
 */
public record SacrificeEnchantedCreatureEffect() implements CardEffect {
}
