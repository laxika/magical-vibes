package com.github.laxika.magicalvibes.model.effect;

/**
 * Takklemaggot's "when enchanted creature dies, that creature's controller chooses a creature that this card could
 * enchant": returns the source Aura attached to a legal creature that player chooses, or as a non-Aura enchantment
 * with an upkeep damage ability aimed at that player when no legal creature exists. "That creature's controller" is
 * the controller of the stack entry's triggering permanent, as it last existed on the battlefield (CR 603.10a), read
 * as the ability resolves.
 */
public record ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect() implements CardEffect {
}
