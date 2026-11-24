package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that multiplies damage dealt by sources controlled by this permanent's
 * controller to an opponent or a permanent an opponent controls (CR 614.1a). Floated on a player by
 * {@link GrantStaticEffectToPlayerEffect} it is "if a source you control would deal damage this turn to an opponent
 * ..." (Goblin Goliath, Isengard Unleashed): a rules change, so it applies to sources that weren't there when it began
 * (CR 611.2c).
 *
 * @param multiplier         2 for "double", 3 for "triple"
 * @param noncombatOnly      whether this effect applies only to noncombat damage
 * @param toTheirPermanents  whether damage to permanents an opponent controls is multiplied too, not only damage to
 *                           the opponent
 */
public record DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(int multiplier, boolean noncombatOnly,
                                                                        boolean toTheirPermanents)
        implements ControllerRecipientDamageMultiplyingEffect {

    public DoubleControllerDamageToOpponentsAndTheirPermanentsEffect {
        if (multiplier < 2) {
            throw new IllegalArgumentException("Damage multiplier must be at least 2");
        }
    }

    public DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(boolean noncombatOnly) {
        this(2, noncombatOnly, true);
    }

    public DoubleControllerDamageToOpponentsAndTheirPermanentsEffect() {
        this(false);
    }

    @Override
    public int damageMultiplier() {
        return multiplier;
    }

    @Override
    public boolean appliesToOpponentPermanents() {
        return toTheirPermanents;
    }

    @Override
    public boolean isDoublingEffect() {
        return multiplier == 2;
    }
}
