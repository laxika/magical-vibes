package com.github.laxika.magicalvibes.model.effect;

/**
 * One side of an attacker/blocker pair created in the declare-blockers step.
 */
public enum BlockParticipant {
    /** The attacking creature that became blocked. */
    ATTACKER,
    /** The creature that blocked it. */
    BLOCKER
}
