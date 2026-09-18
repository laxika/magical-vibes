package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that multiplies damage when its controller's source would deal damage to one of
 * that controller's opponents or to a permanent controlled by one of those opponents.
 */
public interface ControllerRecipientDamageMultiplyingEffect extends DoublingEffect {

    /**
     * The factor applied to each matching damage event.
     */
    int damageMultiplier();

    /**
     * Whether this multiplier applies only to noncombat damage.
     */
    default boolean noncombatOnly() {
        return false;
    }

    /**
     * Whether this multiplier also applies when the recipient is a permanent controlled by an
     * opponent. A false value models effects that mention only damage to an opponent.
     */
    default boolean appliesToOpponentPermanents() {
        return true;
    }
}
