package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for controller-owned sources dealing damage to opponents or their
 * permanents.
 */
public record AdditionalControllerDamageToOpponentsAndTheirPermanentsEffect(int amount)
        implements ControllerOpponentDamageBonusEffect {
}
