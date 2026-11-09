package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Ninja-style combat damage bounce: the source's controller may return permanents the damaged
 * player controls to their owners' hands. {@code filter} restricts which permanents can be chosen
 * ({@code null} = any permanent).
 *
 * <p>{@code fixedCount} caps how many may be returned: {@code 0} means "up to the combat damage
 * dealt" (Cephalid Constable, {@link #upToThatManyTargets()}), while a positive value is a flat cap independent of the damage
 * ({@code 1} for "you may return target creature that player controls" — Mistblade Shinobi). The
 * choice always allows returning fewer, which is what makes the "you may" optional. "That player" and "that many"
 * are the player dealt damage and the damage dealt, which the collector records on the stack entry (its
 * non-targeting {@code targetId} and event value).</p>
 */
public record ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter, int fixedCount, boolean targetsChosenAtTriggerTime)
        implements CardEffect {

    public ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter, int fixedCount) {
        this(filter, fixedCount, false);
    }

    /**
     * "Return up to that many target permanents that player controls to their owner's hand" (Cephalid Constable): the
     * targets, up to the damage dealt, are chosen among the permanents of the player dealt damage as the ability goes on
     * the stack (CR 603.3d), the card declaring them, never the collector.
     */
    public static ReturnPermanentsOnCombatDamageToPlayerEffect upToThatManyTargets() {
        return new ReturnPermanentsOnCombatDamageToPlayerEffect(null, 0, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsChosenAtTriggerTime ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }

    public ReturnPermanentsOnCombatDamageToPlayerEffect() {
        this(null, 0);
    }

    public ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter) {
        this(filter, 0);
    }
}
