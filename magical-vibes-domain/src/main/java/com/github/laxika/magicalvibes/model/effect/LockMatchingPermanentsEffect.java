package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Non-targeted mass lock: every permanent on the battlefield matching {@code predicate} is locked
 * for {@code duration}, forbidding some combination of attacking, blocking and activating its
 * abilities. The handler stamps one floating {@link LockTargetPermanentEffect} per matching
 * permanent, so the same combat / ability-activation readers and duration expiry apply as for the
 * single-target lock.
 *
 * <p>Covers "detain each nonland permanent your opponents control with mana value 4 or less"
 * (Lavinia of the Tenth) — {@code (predicate, true, true, true, UNTIL_YOUR_NEXT_TURN)}.</p>
 *
 * @param predicate               which permanents get locked
 * @param locksAttacking          matching permanents can't attack
 * @param locksBlocking           matching permanents can't block
 * @param locksActivatedAbilities matching permanents' activated abilities can't be activated
 * @param duration                how long the lock lasts
 */
public record LockMatchingPermanentsEffect(
        PermanentPredicate predicate,
        boolean locksAttacking,
        boolean locksBlocking,
        boolean locksActivatedAbilities,
        EffectDuration duration) implements CardEffect {
}
