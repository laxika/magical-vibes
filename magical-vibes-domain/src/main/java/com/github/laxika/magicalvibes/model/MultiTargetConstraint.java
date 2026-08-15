package com.github.laxika.magicalvibes.model;

/**
 * A cross-target restriction that a multi-target spell/ability imposes on the whole set of
 * chosen targets at announcement time (CR 601.2c), in addition to the per-position target
 * filters. Enforced by the targeting services, not by an individual {@code TargetFilter}
 * (which only sees one target at a time).
 */
public enum MultiTargetConstraint {
    /** The chosen creatures must share no creature types (e.g. Rivals' Duel). */
    SHARE_NO_CREATURE_TYPES,
    /**
     * The chosen permanents must share at least one of the card types artifact, creature, or land
     * (e.g. Gauntlets of Chaos: the opponent's permanent must share one of those types with your
     * artifact/creature/land).
     */
    SHARE_ARTIFACT_CREATURE_OR_LAND_TYPE,
    /**
     * The chosen permanents must share at least one of the card types artifact or creature — land
     * alone does not satisfy it (e.g. Legerdemain: "target artifact or creature and another target
     * permanent that shares one of those types with it").
     */
    SHARE_ARTIFACT_OR_CREATURE_TYPE,
    /** The chosen permanents must share at least one card type (e.g. Daring Thief). */
    SHARE_CARD_TYPE,
    /**
     * Every permanent chosen after the first target must be controlled by the first target — the
     * first target itself when it is a player, otherwise the controller of the first target
     * permanent (e.g. Chandra, Pyromaster's "up to one target creature that player or that
     * planeswalker's controller controls").
     */
    CONTROLLED_BY_FIRST_TARGET,
    /** Every permanent chosen after the first must be attached to the first target. */
    ATTACHED_TO_FIRST_TARGET,
    /**
     * At most two of the chosen permanents may be creatures and at most two may be lands
     * ("Untap up to two target creatures and up to two target lands" — Nissa, Genesis Mage +2).
     * Dual-typed permanents (creature lands) may be assigned to either quota.
     */
    AT_MOST_TWO_CREATURES_AND_TWO_LANDS,
    /** At most one chosen target may belong to each player. */
    AT_MOST_ONE_PER_CONTROLLER,
    /** One target must be chosen for each player who controls at least one legal target. */
    ONE_PER_CONTROLLER_IF_ABLE,
    /** At most one chosen graveyard card may be an instant and at most one may be a sorcery. */
    AT_MOST_ONE_INSTANT_AND_ONE_SORCERY,
    /** At most one chosen graveyard card may be a creature and at most one may be a land. */
    AT_MOST_ONE_CREATURE_AND_ONE_LAND,
    /** The second target must be another creature or land of the Aura's current host type. */
    SAME_CREATURE_OR_LAND_TYPE_AS_FIRST_AURA_HOST,
    /** At most one selected card may be assigned to each color. */
    AT_MOST_ONE_PER_COLOR
}
