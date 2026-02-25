package com.github.laxika.magicalvibes.model;

/**
 * Controls how often an effect fires when registered on a card slot.
 * <ul>
 *   <li>{@link #NORMAL} — fires once per event (default)</li>
 *   <li>{@link #PER_BLOCKER} — fires once per blocking creature (e.g. "becomes blocked by a creature")</li>
 * </ul>
 */
public enum TriggerMode {
    NORMAL,
    PER_BLOCKER
}
