package com.github.laxika.magicalvibes.model.effect;

/**
 * Which battlefield(s) a {@link PutCounterOnEachMatchingPermanentEffect} scans, relative to the
 * resolving effect. The effect's {@code PermanentPredicate} narrows the set further (creature,
 * attacking, not-the-source, …).
 */
public enum EachPermanentScope {
    /** Every player's battlefield (e.g. "each attacking creature", "each creature"). */
    ALL_PLAYERS,
    /** The target player's battlefield (e.g. "each creature target player controls"). */
    TARGET_PLAYER
}
