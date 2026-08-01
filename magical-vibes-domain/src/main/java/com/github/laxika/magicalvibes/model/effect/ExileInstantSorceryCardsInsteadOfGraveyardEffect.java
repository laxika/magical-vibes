package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that exiles instant and sorcery cards instead of putting them into a
 * graveyard, regardless of which player owns the graveyard.
 */
public record ExileInstantSorceryCardsInsteadOfGraveyardEffect() implements CardEffect {
}
