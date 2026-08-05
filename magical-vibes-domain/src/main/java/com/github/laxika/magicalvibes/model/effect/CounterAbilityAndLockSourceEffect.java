package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters a target ability on the stack and, in addition, locks the permanent that ability came
 * from so its activated abilities can't be activated for the given duration (Interdict).
 *
 * <p>Both halves live in one effect because the lock needs the countered entry's source permanent,
 * which is only reachable while that entry is still on the stack. The handler stamps a
 * {@link LockTargetPermanentEffect} carrying {@code locksActivatedAbilities} onto the source
 * permanent, so the existing {@link PermanentLockEffect} machinery (read by
 * {@code AbilityActivationService}) and the floating-effect duration expiry apply unchanged.
 *
 * <p>The card supplies the narrowing {@code StackEntryPredicateTargetFilter} (e.g. "activated
 * ability from an artifact, creature, enchantment, or land"); mana abilities never use the stack,
 * so they are excluded automatically.
 *
 * @param lockDuration how long the source permanent's ability lock lasts
 */
public record CounterAbilityAndLockSourceEffect(EffectDuration lockDuration)
        implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
