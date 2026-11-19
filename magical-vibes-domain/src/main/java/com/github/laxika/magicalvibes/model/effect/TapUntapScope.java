package com.github.laxika.magicalvibes.model.effect;

/**
 * Which permanent(s) an untap restriction or a "this turn" combat restriction affects, relative to the resolving
 * effect: {@link SkipNextUntapEffect}, {@link DoesntUntapEffect}, {@link DoesntUntapWithCounterEffect},
 * {@link UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect}, {@link CantAttackThisTurnEffect} and
 * {@link CantBlockThisTurnEffect}. The tap and untap verbs ({@link TapPermanentsEffect},
 * {@link UntapPermanentsEffect}) take a {@link PermanentSubject} instead. The effect's optional
 * {@code PermanentPredicate} narrows the scanned scopes further (creature, attacking, not-the-source, …).
 */
public enum TapUntapScope {
    /** The effect's chosen target permanent. */
    TARGET,
    /** The source permanent itself. */
    SELF,
    /** The permanent whose event caused the triggered ability. */
    TRIGGERING,
    /** The permanent the source aura is attached to. */
    ENCHANTED,
    /** Every permanent the controller controls that matches the predicate. */
    CONTROLLED,
    /** Every permanent the target player controls that matches the predicate. */
    TARGET_PLAYERS_PERMANENTS,
    /**
     * Every creature controlled by the target permanent's controller <em>except</em> the target
     * itself, matching the predicate ("Other creatures that player controls …" — Mark for Death).
     */
    TARGET_CONTROLLERS_OTHER_CREATURES,
    /** Every creature on every battlefield that matches the predicate. */
    ALL_CREATURES,
    /** Every permanent controlled by an opponent of the controller that matches the predicate. */
    OPPONENTS_PERMANENTS
}
