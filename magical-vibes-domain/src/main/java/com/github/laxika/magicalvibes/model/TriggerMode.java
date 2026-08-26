package com.github.laxika.magicalvibes.model;

/**
 * Controls how often an effect fires when registered on a card slot.
 * <ul>
 *   <li>{@link #NORMAL} — fires once per event (default)</li>
 *   <li>{@link #PER_BLOCKER} — fires once per blocking creature (e.g. "becomes blocked by a creature")</li>
 *   <li>{@link #ONCE_PER_BLOCK} — fires once for a block declaration, even when the source blocks multiple creatures</li>
 *   <li>{@link #ONCE_PER_BATCH} — fires once when several matching permanents become tapped as one event</li>
 * </ul>
 */
public enum TriggerMode {
    NORMAL,
    PER_BLOCKER,
    ONCE_PER_BLOCK,
    ONCE_PER_BATCH
}
