package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that doubles damage dealt by sources controlled by this permanent's
 * controller to an opponent, but not to permanents controlled by an opponent.
 */
public record DoubleControllerDamageToOpponentsEffect() implements ControllerRecipientDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public boolean appliesToOpponentPermanents() {
        return false;
    }
}
