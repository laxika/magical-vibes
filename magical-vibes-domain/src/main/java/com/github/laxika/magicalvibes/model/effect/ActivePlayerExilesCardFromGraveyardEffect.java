package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles one card from the active player's graveyard, or resolves a fallback effect when that
 * graveyard is empty.
 *
 * @param noCardEffect effect resolved when the active player has no card to exile
 */
public record ActivePlayerExilesCardFromGraveyardEffect(CardEffect noCardEffect) implements CardEffect {
}
