package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may play face-up cards you own from exile": a rule for the player it is floated on
 * ({@link GrantStaticEffectToPlayerEffect#untilEndOfTurn}, Yawgmoth's Testament), read as each card is played, so a card
 * exiled later in the duration counts too. It has no handler of its own.
 */
public record AllowPlayFaceUpCardsFromExileThisTurnEffect() implements CardEffect {
}
