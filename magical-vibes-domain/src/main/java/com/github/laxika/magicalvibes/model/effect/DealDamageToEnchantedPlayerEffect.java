package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Deals a fixed amount of damage to the enchanted player (the player a curse is attached to).
 * Used by curses with {@code ENCHANTED_PLAYER_UPKEEP_TRIGGERED} slot.
 *
 * @param damage            amount of damage to deal
 * @param affectedPlayerId  the enchanted player (baked in at trigger time; null in card definition)
 */
public record DealDamageToEnchantedPlayerEffect(int damage, UUID affectedPlayerId) implements CardEffect {

    public DealDamageToEnchantedPlayerEffect(int damage) {
        this(damage, null);
    }
}
