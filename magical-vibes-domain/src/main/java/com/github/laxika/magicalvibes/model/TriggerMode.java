package com.github.laxika.magicalvibes.model;

/**
 * Controls how often an effect fires when registered on a card slot.
 * <ul>
 *   <li>{@link #NORMAL} — fires once per event (default)</li>
 *   <li>{@link #PER_BLOCKER} — fires once per blocking creature (e.g. "becomes blocked by a creature")</li>
 *   <li>{@link #PER_COUNTER} — fires once for each counter put on the source in a placement event</li>
 *   <li>{@link #ONCE_PER_BLOCK} — fires once for a block declaration, even when the source blocks multiple creatures</li>
 *   <li>{@link #ONCE_PER_BATCH} — fires once when several matching objects are processed as one event</li>
 *   <li>{@link #PER_TOKEN} — fires separately for each token entering in a token-creation event</li>
 *   <li>{@link #TOKEN_CREATION} — a token-creation event ("whenever you create one or more tokens"), not an enter-the-battlefield trigger</li>
 * </ul>
 */
public enum TriggerMode {
    NORMAL,
    PER_BLOCKER,
    ONCE_PER_BLOCK,
    ONCE_PER_BATCH,
    /** This registration is its own triggered ability instead of another instruction in a shared ability. */
    INDEPENDENT,
    /** Fires separately for each individual counter placed in a counter-placement event. */
    PER_COUNTER,
    /** Fires separately for each token entering in a token-creation event (e.g. "whenever a token enters"). */
    PER_TOKEN,
    /**
     * Fires once per event in which its controller creates tokens. The ability is caused by the
     * creation, not by a permanent entering, so effects that double or suppress enter triggers
     * (Elesh Norn, Mother of Machines) do not apply.
     */
    TOKEN_CREATION
}
