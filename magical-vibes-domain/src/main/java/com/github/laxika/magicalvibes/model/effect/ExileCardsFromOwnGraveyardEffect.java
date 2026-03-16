package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Forces a player to exile a number of cards from their own graveyard.
 * If the graveyard has more cards than {@code count}, the player chooses which to exile.
 *
 * @param count            number of cards to exile
 * @param affectedPlayerId the player who must exile cards (baked in at trigger/resolution time; null in card definition)
 */
public record ExileCardsFromOwnGraveyardEffect(int count, UUID affectedPlayerId) implements CardEffect {

    public ExileCardsFromOwnGraveyardEffect(int count) {
        this(count, null);
    }
}
