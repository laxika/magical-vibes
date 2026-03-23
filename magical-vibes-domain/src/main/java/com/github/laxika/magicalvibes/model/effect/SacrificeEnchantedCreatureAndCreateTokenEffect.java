package com.github.laxika.magicalvibes.model.effect;

/**
 * Compound upkeep trigger for auras: sacrifice the enchanted creature, then create a creature token
 * for the aura's controller. Used by Parasitic Implant and similar cards.
 *
 * <p>The enchanted creature's controller sacrifices it (not the aura's controller, unless they are the same).
 * The token is always created for the aura's controller regardless of who controlled the enchanted creature.</p>
 */
public record SacrificeEnchantedCreatureAndCreateTokenEffect(
        CreateTokenEffect tokenEffect
) implements CardEffect {
}
