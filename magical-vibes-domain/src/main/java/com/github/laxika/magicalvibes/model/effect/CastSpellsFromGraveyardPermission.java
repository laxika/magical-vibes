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

    /** True if this permission applies only during its controller's turn. */
    default boolean onlyDuringControllerTurn() {
        return false;
    }

    /** Number of additional cards the player must exile from their graveyard to cast the spell. */
    default int additionalGraveyardExileCount() {
        return 0;
    }

    /** Whether this permission casts the spell using escape. */
    default boolean escape() {
        return false;
    }

    /** Label for the additional graveyard exile choice shown to the player, if any. */
    default String additionalGraveyardExileLabel() {
        return null;
    }

    /** Additional costs required when using this permission. */
    default List<CastingCost> additionalCosts() {
        return List.of();
    }

    /** True if a spell cast through this permission is exiled instead of returned to its graveyard. */
    default boolean exileAfterResolution() {
        return false;
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
