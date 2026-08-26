package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CastingCost;

import java.util.List;

/**
 * Capability for a static effect that lets its controller cast spells matching {@link #filter()}
 * from their graveyard for their normal mana costs (Abandoned Sarcophagus). Read by
 * {@code CastingPermissionService} / graveyard cast paths without branching on the concrete effect.
 */
public interface CastSpellsFromGraveyardPermission extends CardEffect {

    /** Spells matching this predicate may be cast from the controller's graveyard. */
    CardPredicate filter();

    /**
     * True if the permission is limited to one spell during each of the controller's own turns
     * (Gisa and Geralf). False grants an unlimited, any-turn permission (Abandoned Sarcophagus).
     */
    default boolean oncePerControllerTurn() {
        return false;
    }

    /** Additional costs required when using this permission. */
    default List<CastingCost> additionalCosts() {
        return List.of();
    }

    /** Counter applied to a permanent cast with this permission as it enters. */
    default CounterType enterWithCounter() {
        return null;
    }

    /** Number of counters applied by {@link #enterWithCounter()}. */
    default int enterWithCounterCount() {
        return enterWithCounter() == null ? 0 : 1;
    }
}
