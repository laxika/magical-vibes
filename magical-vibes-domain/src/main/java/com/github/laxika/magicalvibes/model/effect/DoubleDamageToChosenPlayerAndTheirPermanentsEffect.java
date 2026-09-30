package com.github.laxika.magicalvibes.model.effect;

/** Static replacement effect that doubles damage dealt to the player chosen by its source. */
public record DoubleDamageToChosenPlayerAndTheirPermanentsEffect()
        implements ChosenPlayerRecipientDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }
}
