package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Ninja-style combat damage bounce: the source's controller may return permanents the damaged
 * player controls to their owners' hands. {@code filter} restricts which permanents can be chosen
 * ({@code null} = any permanent).
 *
 * <p>{@code fixedCount} caps how many may be returned: {@code 0} means "up to the combat damage
 * dealt" (Cephalid Constable), while a positive value is a flat cap independent of the damage
 * ({@code 1} for "you may return target creature that player controls" — Mistblade Shinobi). The
 * choice always allows returning fewer, which is what makes the "you may" optional.</p>
 */
public record ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter, int fixedCount)
        implements CombatDamageTriggerContextEffect {

    public ReturnPermanentsOnCombatDamageToPlayerEffect() {
        this(null, 0);
    }

    public ReturnPermanentsOnCombatDamageToPlayerEffect(PermanentPredicate filter) {
        this(filter, 0);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT;
    }
}
