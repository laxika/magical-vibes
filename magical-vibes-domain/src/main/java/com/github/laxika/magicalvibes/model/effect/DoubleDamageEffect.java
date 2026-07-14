package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that doubles all damage dealt to permanents and players.
 * Used by Furnace of Rath and similar cards.
 */
public record DoubleDamageEffect() implements GlobalDamageMultiplyingEffect {

    @Override
    public int damageMultiplierFactor() {
        return 2;
    }
}
