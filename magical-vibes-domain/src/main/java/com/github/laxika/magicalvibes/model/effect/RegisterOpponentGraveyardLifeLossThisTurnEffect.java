package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect creating a delayed triggered ability: for the rest of the turn, whenever a card is
 * put into an opponent's graveyard from anywhere, that player loses 1 life. Adds an
 * {@code OpponentGraveyardLifeLossWatcher} to {@code GameData.opponentGraveyardLifeLossWatchers}
 * (cleared at turn cleanup); {@code GraveyardService.addCardToGraveyard} pushes one life-loss
 * trigger per watcher whose controller is an opponent of the graveyard's owner. Multiple activations
 * stack. Used by Duskmantle Guildmage.
 */
public record RegisterOpponentGraveyardLifeLossThisTurnEffect() implements CardEffect {
}
