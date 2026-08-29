package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Static replacement effect for controller-owned sources dealing damage to opponents or their
 * permanents.
 */
public record AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(DynamicAmount amount)
        implements ControllerOpponentDamageBonusEffect {

    public AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(int amount) {
        this(new Fixed(amount));
    }
}
