package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the top card of the controller's graveyard (the card most recently put there) on the bottom
 * of their library. Does nothing if that graveyard is empty.
 * <p>
 * Used by Soldevi Digger.
 */
public record PutTopCardOfGraveyardOnBottomOfLibraryEffect() implements CardEffect {
}
