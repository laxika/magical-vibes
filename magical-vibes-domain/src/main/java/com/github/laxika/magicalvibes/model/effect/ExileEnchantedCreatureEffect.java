package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the creature the source Aura is attached to, without any additional effect.
 * The exile variant of {@link SacrificeEnchantedCreatureEffect} — resolution finds the Aura via
 * the stack entry's {@code sourcePermanentId}, then exiles the creature it is attached to. If the
 * Aura is sacrificed as an activation cost, the attached creature is bound before that cost is
 * paid.
 *
 * <p>Used by Weight of Conscience ("Tap two untapped creatures you control that share a creature
 * type: Exile enchanted creature.").</p>
 */
public record ExileEnchantedCreatureEffect(UUID enchantedPermanentId) implements CardEffect {

    public ExileEnchantedCreatureEffect() {
        this(null);
    }

    /**
     * Binds the enchanted permanent when an Aura sacrifices itself as the activation cost.
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return true;
    }
}
