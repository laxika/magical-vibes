package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that doubles damage dealt by sources controlled by this permanent's
 * controller to an opponent or a permanent an opponent controls.
 *
 * @param noncombatOnly whether this effect applies only to noncombat damage
 */
public record DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(boolean noncombatOnly)
        implements ControllerRecipientDamageMultiplyingEffect {

    public DoubleControllerDamageToOpponentsAndTheirPermanentsEffect() {
        this(false);
    }

    @Override
    public int damageMultiplier() {
        return 2;
    }
}
