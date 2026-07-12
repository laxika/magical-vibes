package com.github.laxika.magicalvibes.model.effect;

/**
 * Target opponent exiles the top card of their library; until end of turn, the source's controller
 * may play that card (Knacksaw Clique). In a two-player game the single opponent is the only legal
 * target. The exiled card is owned by the opponent but the controller receives normal play
 * permission (lands and spells at normal costs and timing) that expires at end of turn.
 */
public record ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect() implements CardEffect {
}
