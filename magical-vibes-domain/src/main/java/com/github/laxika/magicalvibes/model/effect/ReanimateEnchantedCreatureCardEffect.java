package com.github.laxika.magicalvibes.model.effect;

/**
 * Reanimates the creature card currently enchanted by the source Aura, then attaches the Aura to
 * the resulting creature. The source must still be on the battlefield when this ability resolves.
 */
public record ReanimateEnchantedCreatureCardEffect(boolean enterTapped) implements CardEffect {
}
