package com.github.laxika.magicalvibes.model;

/**
 * Controls how often an effect fires when registered on a card slot.
 * <ul>
 *   <li>{@link #NORMAL} — fires once per event (default)</li>
 *   <li>{@link #PER_BLOCKER} — fires once per blocking creature (e.g. "becomes blocked by a creature")</li>
 *   <li>{@link #ONCE_PER_BLOCK} — fires once for a block declaration, even when the source blocks multiple creatures</li>
 * </ul>
 */
public enum TriggerMode {
    NORMAL,
    PER_BLOCKER,
    ONCE_PER_BLOCK
}
