package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent(s) a {@link TapPermanentsEffect} / {@link UntapPermanentsEffect} affects,
 * relative to the resolving effect. The effect's optional {@code PermanentPredicate} narrows the
 * scanned scopes further (creature, attacking, not-the-source, …).
 */
public enum TapUntapScope {
    /**
     * The effect's chosen target permanent. For untap the effect's predicate acts as a targeting
     * restriction (exposed via {@code targetPredicate()}); tap chooses its target freely.
     */
    TARGET,
    /** Every permanent in {@code entry.getTargetIds()} (multi-target). */
    ALL_TARGETS,
    /** The source permanent itself. */
    SELF,
    /** The permanent the source aura is attached to. */
    ENCHANTED,
    /** Every permanent the controller controls that matches the predicate. */
    CONTROLLED,
    /** Every creature the controller controls except the source, matching the predicate. */
    OTHER_CONTROLLED_CREATURES,
    /** Every permanent the target player controls that matches the predicate. */
    TARGET_PLAYERS_PERMANENTS,
    /** Every creature on every battlefield that matches the predicate. */
    ALL_CREATURES,
    /** Every creature on every battlefield that attacked this turn. */
    ATTACKED_CREATURES
}
