package com.github.laxika.magicalvibes.model.effect;

/** Static replacement effect that doubles damage between the two players chosen by its source. */
public record DoubleDamageBetweenChosenPlayersEffect() implements ChosenPlayersDamageMultiplyingEffect {

    @Override
    public int damageMultiplier() {
        return 2;
    }
}
