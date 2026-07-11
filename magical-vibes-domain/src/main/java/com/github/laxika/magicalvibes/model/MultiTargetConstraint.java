package com.github.laxika.magicalvibes.model;

/**
 * A cross-target restriction that a multi-target spell/ability imposes on the whole set of
 * chosen targets at announcement time (CR 601.2c), in addition to the per-position target
 * filters. Enforced by the targeting services, not by an individual {@code TargetFilter}
 * (which only sees one target at a time).
 */
public enum MultiTargetConstraint {
    /** The chosen creatures must share no creature types (e.g. Rivals' Duel). */
    SHARE_NO_CREATURE_TYPES
}
