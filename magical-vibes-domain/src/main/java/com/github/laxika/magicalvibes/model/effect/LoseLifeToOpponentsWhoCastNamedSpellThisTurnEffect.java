package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes each opponent who cast a spell with the given name this turn lose the specified amount of
 * life. Each qualifying opponent loses life only once, regardless of how many matching spells they
 * cast.
 */
public record LoseLifeToOpponentsWhoCastNamedSpellThisTurnEffect(String cardName, int amount)
        implements CardEffect {
}
