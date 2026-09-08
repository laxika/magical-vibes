package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Makes the source permanent a copy of the specified card in a graveyard until end of turn.
 * The card ID is captured after a library choice so replacement effects and later graveyard
 * changes cannot make the copy use a different card.
 */
public record BecomeCopyOfCardInGraveyardUntilEndOfTurnEffect(UUID cardId) implements CardEffect {
}
