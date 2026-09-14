package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a source would deal damage to an opponent, it deals double that
 * damage to that player instead."
 */
public record DoubleDamageToOpponentsEffect() implements OpponentRecipientDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }

    @Override
    public boolean appliesToPermanents() {
        return false;
    }
}
