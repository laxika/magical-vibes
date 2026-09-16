package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that multiplies damage dealt to an opponent of the effect's controller.
 * Implementations can limit the multiplier to player recipients or include permanents controlled
 * by those opponents.
 */
public interface OpponentRecipientDamageMultiplyingEffect extends CardEffect {

    int damageMultiplier();

    /** Whether this multiplier also applies to permanents controlled by the opponent. */
    default boolean appliesToPermanents() {
        return true;
    }
}
