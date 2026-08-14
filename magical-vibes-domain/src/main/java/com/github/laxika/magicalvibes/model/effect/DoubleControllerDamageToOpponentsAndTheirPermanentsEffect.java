package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that doubles damage dealt by sources controlled by this permanent's
 * controller to an opponent or a permanent an opponent controls.
 */
public record DoubleControllerDamageToOpponentsAndTheirPermanentsEffect()
        implements ControllerRecipientDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }
}
