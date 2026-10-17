package com.github.laxika.magicalvibes.model.effect;

/**
 * Upkeep trigger for auras that sacrifice the enchanted creature without any additional effect.
 * Used by cards like Curse Artifact where the Aura itself has the triggered ability.
 *
 * <p>The enchanted creature's controller sacrifices it (not the aura's controller, unless they
 * are the same). Resolution finds the aura via the stack entry's {@code sourcePermanentId},
 * then sacrifices the creature the aura is attached to.</p>
 *
 * <p>When {@code grantedToCreature} is set the ability is one the Aura gives the creature
 * ("Enchanted creature has 'At the beginning of your upkeep, sacrifice this creature'", Necrotic
 * Plague): the creature is the ability's source and its controller controls the ability, and
 * resolution sacrifices the source creature itself.</p>
 *
 * @param grantedToCreature whether the triggered ability belongs to the enchanted creature
 */
public record SacrificeEnchantedCreatureEffect(boolean grantedToCreature) implements CardEffect {

    public SacrificeEnchantedCreatureEffect() {
        this(false);
    }
}
