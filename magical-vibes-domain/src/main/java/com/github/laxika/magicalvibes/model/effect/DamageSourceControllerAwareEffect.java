package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability for {@code ON_DEALT_DAMAGE} effects phrased as "whenever a source deals damage to this
 * creature, that source's controller ...". Such effects are registered on the card as markers (no
 * player id, no amount) because neither is known until the damage event happens.
 *
 * <p>Both damage paths (combat via {@code CombatDamageService}, non-combat via
 * {@code DamageTriggerCollectorService}) call {@link #bindDamageSourceController(UUID, int)} to
 * exchange the marker for a bound instance before it goes on the stack. An implementation returns
 * {@code this} when the event does not qualify (e.g. no damage dealt, unknown source controller),
 * leaving the inert marker to resolve as a no-op.
 */
public interface DamageSourceControllerAwareEffect extends CardEffect {

    /**
     * @param controllerId controller of the damage source, or {@code null} when unknown
     * @param damageDealt  damage dealt by that source to this creature
     * @return a bound copy carrying the event data, or {@code this} when it does not qualify
     */
    CardEffect bindDamageSourceController(UUID controllerId, int damageDealt);
}
