package com.github.laxika.magicalvibes.model.effect;

/**
 * Which battlefield(s) a {@link PutCounterOnEachMatchingPermanentEffect},
 * {@link DealDamageToEachMatchingPermanentEffect} or {@link DestroyAllPermanentsEffect} scans,
 * relative to the resolving effect. The effect's {@code PermanentPredicate} narrows the set further
 * (creature, attacking, not-the-source, …).
 */
public enum EachPermanentScope {
    /** Every player's battlefield (e.g. "each attacking creature", "each creature"). */
    ALL_PLAYERS,
    /** The target player's battlefield (e.g. "each creature target player controls"). */
    TARGET_PLAYER,
    /**
     * The permanents the trigger event was about, the stack entry's triggering permanents wherever they are now: "put a
     * +1/+1 counter on each of them" for "whenever you attack with one or more Insects" or "whenever one or more
     * creatures you control deal combat damage to a player". Only {@link PutCounterOnEachMatchingPermanentEffect}
     * reads it.
     */
    TRIGGERING_PERMANENTS
}
