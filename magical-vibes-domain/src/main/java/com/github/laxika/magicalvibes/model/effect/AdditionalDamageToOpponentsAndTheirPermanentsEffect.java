package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that adds damage dealt to an opponent of this permanent's controller
 * or to a permanent that opponent controls.
 */
public record AdditionalDamageToOpponentsAndTheirPermanentsEffect(int amount)
        implements OpponentDamageBonusEffect {
}
