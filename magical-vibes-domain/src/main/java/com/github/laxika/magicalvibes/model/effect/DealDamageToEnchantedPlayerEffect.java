package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Deals damage to the enchanted player (the player a curse is attached to).
 * Used by curses with {@code ENCHANTED_PLAYER_UPKEEP_TRIGGERED} slot.
 *
 * <p>If {@code damageEqualsAttachedCount} is non-null, the fixed {@code damage} value is ignored and
 * the amount dealt is computed at resolution time as the number of permanents attached to the
 * enchanted player that match the predicate (e.g. Curse of Thirst counts Curses attached to the player).
 *
 * @param damage                     fixed amount of damage to deal (ignored when computed dynamically)
 * @param damageEqualsAttachedCount  when non-null, deal damage equal to the number of permanents attached
 *                                   to the player matching this predicate
 * @param affectedPlayerId           the enchanted player (baked in at trigger time; null in card definition)
 */
public record DealDamageToEnchantedPlayerEffect(int damage, PermanentPredicate damageEqualsAttachedCount,
                                                UUID affectedPlayerId) implements CardEffect {

    public DealDamageToEnchantedPlayerEffect(int damage) {
        this(damage, null, null);
    }

    /**
     * Deals damage equal to the number of permanents attached to the enchanted player that match the
     * given predicate (e.g. {@code PermanentHasSubtypePredicate(CardSubtype.CURSE)} for Curse of Thirst).
     */
    public static DealDamageToEnchantedPlayerEffect attachedCount(PermanentPredicate predicate) {
        return new DealDamageToEnchantedPlayerEffect(0, predicate, null);
    }
}
