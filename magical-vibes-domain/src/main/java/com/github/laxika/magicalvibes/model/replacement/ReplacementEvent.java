package com.github.laxika.magicalvibes.model.replacement;

import java.util.Set;
import java.util.UUID;

/**
 * An impending event that replacement and/or prevention effects may modify. When two or more of
 * them apply, {@link #chooserId()} picks the one to apply next (CR 616.1); every effect already
 * applied is remembered in {@link #appliedKeys()} so it never affects the event again (CR 614.5).
 */
public sealed interface ReplacementEvent permits DamageEvent, LandManaEvent {

    /** The affected player, or the affected object's controller, who orders the effects. */
    UUID chooserId();

    /** How much the event does: the damage still to be dealt, or the mana to be produced. */
    int amount();

    /** Keys of the {@link ReplacementOption}s already applied to this event. */
    Set<String> appliedKeys();

    /** This event after the effect identified by {@code key} has been applied to it. */
    ReplacementEvent withApplied(String key);

    /** This event with no applied effects recorded, for comparing final outcomes. */
    ReplacementEvent withoutAppliedKeys();

    /** An independent copy for a simulated game, sharing nothing mutable with this event. */
    default ReplacementEvent copyForSimulation() {
        return this;
    }
}
