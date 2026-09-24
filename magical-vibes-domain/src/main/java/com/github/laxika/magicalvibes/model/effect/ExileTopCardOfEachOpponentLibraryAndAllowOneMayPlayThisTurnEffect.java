package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each opponent's library and gives the controller one shared, free play
 * permission for those cards until end of turn.
 */
public record ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect()
        implements CardEffect {
}
