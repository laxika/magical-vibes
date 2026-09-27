package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static replacement effect for controller-owned sources dealing damage to opponents or their
 * permanents.
 *
 * @param excludesSource whether the permanent carrying this effect is excluded as a damage source
 */
public record AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(
        DynamicAmount amount, boolean noncombatOnly, boolean excludesSource)
        implements ControllerOpponentDamageBonusEffect {

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(int amount) {
        this(new Fixed(amount), false, false);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(int amount, boolean noncombatOnly) {
        this(new Fixed(amount), noncombatOnly, false);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(
            int amount, boolean noncombatOnly, boolean excludesSource) {
        this(new Fixed(amount), noncombatOnly, excludesSource);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(DynamicAmount amount) {
        this(amount, false, false);
    }

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(
            DynamicAmount amount, boolean noncombatOnly) {
        this(amount, noncombatOnly, false);
    }

    @Override
    public boolean appliesToCombatDamage() {
        return !noncombatOnly;
    }
}
