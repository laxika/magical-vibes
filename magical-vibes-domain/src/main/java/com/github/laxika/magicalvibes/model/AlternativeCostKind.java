package com.github.laxika.magicalvibes.model;

/**
 * Which alternative cost (CR 118.9) a spell was cast for, as far as a rule reads it later. Only one alternative cost
 * can be applied to a spell (CR 118.9a); {@code null} on a {@link StackEntry} or {@link Permanent} means none was.
 */
public enum AlternativeCostKind {
    /** The card's dash cost (CR 702.109a): its permanent has haste and returns at the next end step. */
    DASH,
    /**
     * A blitz cost (CR 702.152a), printed or granted: its permanent has haste and "When this permanent is put into a
     * graveyard from the battlefield, draw a card" and is sacrificed at the next end step.
     */
    BLITZ,
    /** Any other alternative cost. */
    OTHER
}
