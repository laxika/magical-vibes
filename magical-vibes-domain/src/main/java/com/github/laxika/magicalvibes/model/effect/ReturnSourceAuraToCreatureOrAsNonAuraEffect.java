package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for an Aura: return it under its existing controller's control attached to a
 * creature it can legally enchant, or return it as a non-Aura enchantment with a remembered-player
 * upkeep damage ability when no such creature exists.
 */
public record ReturnSourceAuraToCreatureOrAsNonAuraEffect(UUID enchantedCreatureControllerId)
        implements CardEffect {

    /**
     * Card-definition constructor; the enchanted creature's controller is baked in when the
     * enchanted creature dies.
     */
    public ReturnSourceAuraToCreatureOrAsNonAuraEffect() {
        this(null);
    }
}
