package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for a continuous effect that "locks" a single permanent for a duration:
 * it may forbid that permanent from attacking, from blocking, and/or from having its activated
 * abilities activated (the Detain lockdown from Return to Ravnica / Edifice of Authority).
 *
 * <p>Exposed as an interface so the combat services ({@code CombatAttackService},
 * {@code GameQueryService}) and {@code AbilityActivationService} can ask "does this floating
 * effect forbid attacking/blocking/activating this permanent" without branching on the concrete
 * record, mirroring how {@link AttackOrBlockRestrictionEffect} abstracts board-wide restrictions.
 * Descriptive only: every method states a fact drawn from the record's components, never behaviour.
 * The engine reads these facts from a {@code FloatingContinuousEffect} whose
 * {@code affectedPermanentId} is the locked permanent, and lets the floating-effect duration
 * machinery expire it (end of turn / the controller's next turn).
 */
public interface PermanentLockEffect extends CardEffect {

    /** When {@code true}, the affected permanent can't be declared as an attacker. */
    default boolean locksAttacking() {
        return false;
    }

    /** When {@code true}, the affected permanent can't be declared as a blocker. */
    default boolean locksBlocking() {
        return false;
    }

    /** When {@code true}, the affected permanent's activated abilities can't be activated. */
    default boolean locksActivatedAbilities() {
        return false;
    }
}
