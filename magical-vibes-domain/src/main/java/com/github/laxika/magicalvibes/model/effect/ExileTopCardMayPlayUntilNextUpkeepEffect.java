package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile the top card of the controller's library. The controller may play that card (any type,
 * lands included) until the beginning of their next upkeep, at which point it simply loses its play
 * permission and stays in exile if unplayed.
 * <p>
 * Used by Elkin Bottle. Unlike {@code ExileTopCardsMayPlayUntilNextTurnEffect} the window closes at
 * the next upkeep (a {@code RevokeExilePlayPermissionAtNextUpkeep} delayed action) rather than the
 * end of the next turn, and unlike Grinning Totem the card is never moved to a graveyard.
 */
public record ExileTopCardMayPlayUntilNextUpkeepEffect() implements CardEffect {
}
