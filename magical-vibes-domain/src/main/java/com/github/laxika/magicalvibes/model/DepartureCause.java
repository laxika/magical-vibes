package com.github.laxika.magicalvibes.model;

/**
 * Why a permanent is leaving the battlefield as part of a {@link DepartureBatch}. The cause decides
 * whether destruction replacements are gathered before the CR 616.1 death replacements are chosen.
 */
public enum DepartureCause {
    /** Destroyed by a spell or ability (CR 701.8a); regeneration and other destruction replacements apply. */
    DESTROY,
    /** Sacrificed by its controller (CR 701.21a); destruction replacements can't apply. */
    SACRIFICE,
    /** Destroyed by a state-based action, such as lethal damage (CR 704.5g); regeneration can replace it. */
    STATE_BASED_DESTROY,
    /** Put into the graveyard by a state-based action that isn't destruction, such as 0 toughness (CR 704.5f). */
    STATE_BASED,
    /** Put into the graveyard some other way, neither destroyed nor sacrificed. */
    OTHER;

    /** Whether destruction replacements (indestructible, regeneration, shield counters) apply. */
    public boolean isDestruction() {
        return this == DESTROY || this == STATE_BASED_DESTROY;
    }

    /** Whether the departure is performed by a state-based action check (CR 704.3). */
    public boolean isStateBased() {
        return this == STATE_BASED_DESTROY || this == STATE_BASED;
    }
}
