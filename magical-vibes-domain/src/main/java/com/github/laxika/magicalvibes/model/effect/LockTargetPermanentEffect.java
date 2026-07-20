package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolved effect that locks a single target creature for a duration, forbidding some combination
 * of attacking, blocking, and activating its abilities. On resolution the handler stamps a
 * {@code FloatingContinuousEffect} carrying this record onto the target with the given
 * {@link EffectDuration}; the combat and ability-activation services then read the lock facts via
 * {@link PermanentLockEffect} and the duration machinery expires it.
 *
 * <p>Covers both Edifice of Authority abilities:
 * <ul>
 *   <li>"Target creature can't attack this turn." — {@code (true, false, false, UNTIL_END_OF_TURN)}</li>
 *   <li>"Until your next turn, target creature can't attack or block and its activated abilities
 *       can't be activated." (Detain) — {@code (true, true, true, UNTIL_YOUR_NEXT_TURN)}</li>
 * </ul>
 *
 * @param locksAttacking          the target can't attack
 * @param locksBlocking           the target can't block
 * @param locksActivatedAbilities the target's activated abilities can't be activated
 * @param duration                how long the lock lasts
 */
public record LockTargetPermanentEffect(
        boolean locksAttacking,
        boolean locksBlocking,
        boolean locksActivatedAbilities,
        EffectDuration duration) implements CardEffect, PermanentLockEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
