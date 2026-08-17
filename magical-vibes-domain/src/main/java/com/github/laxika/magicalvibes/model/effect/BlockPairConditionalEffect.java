package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Effect in the {@code ON_ANY_CREATURE_BECOMES_BLOCKED} slot that only fires for some attacker/blocker
 * pairs and acts on one named side of the pair. The combat code evaluates the pair condition and
 * optional {@link #attackerPredicate()} at trigger creation time, then bakes the
 * {@link #actsOn()} participant into the stack entry's non-targeting {@code targetId}, so the
 * resolution handler never has to re-derive the pair.
 */
public interface BlockPairConditionalEffect extends CardEffect {

    /** The side of the pair this effect acts on. */
    BlockParticipant actsOn();

    /** Optional predicate evaluated against the attacking creature when the pair is declared. */
    default PermanentPredicate attackerPredicate() {
        return null;
    }

    /** Effect to place on the stack after this trigger marker matches. */
    default CardEffect resolvedEffect() {
        return this;
    }

    /** Whether the trigger condition holds for a pair with the given effective powers. */
    boolean firesForPair(int attackerPower, int blockerPower);
}
