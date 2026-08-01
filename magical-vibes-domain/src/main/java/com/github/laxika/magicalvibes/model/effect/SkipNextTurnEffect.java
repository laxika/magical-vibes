package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the resolving controller skip their next turn ("you skip your next turn" — Chronatog).
 * Modelled by incrementing a per-player counter ({@code GameData.skipNextTurnCount}) which
 * {@code TurnProgressionService.advanceTurn} consumes when that player's turn would begin,
 * proceeding past the turn as though it did not exist (CR 500.11, 614.10 / 614.10a). Pending
 * player-controlling effects wait until a turn is actually taken (CR 723.1b).
 */
public record SkipNextTurnEffect() implements CardEffect {
}
