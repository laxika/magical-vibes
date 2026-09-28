package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static replacement effect for noncombat or all damage dealt by this permanent's controller to
 * opposing players. Damage to permanents controlled by opponents is not affected.
 */
public record AdditionalControllerDamageToOpponentsEffect(
        DynamicAmount amount, boolean noncombatOnly) implements ControllerOpponentDamageBonusEffect {

    public AdditionalControllerDamageToOpponentsEffect(int amount) {
        this(new Fixed(amount), false);
    }

    public AdditionalControllerDamageToOpponentsEffect(int amount, boolean noncombatOnly) {
        this(new Fixed(amount), noncombatOnly);
    }

    public AdditionalControllerDamageToOpponentsEffect(DynamicAmount amount) {
        this(amount, false);
    }

    @Override
    public boolean appliesToCombatDamage() {
        return !noncombatOnly;
    }

    @Override
    public boolean appliesToOpponentPermanents() {
        return false;
    }
}
