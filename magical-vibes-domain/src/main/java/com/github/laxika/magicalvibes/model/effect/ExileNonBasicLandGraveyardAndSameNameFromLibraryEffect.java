package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles all cards from target player's graveyard other than basic land cards.
 * For each card exiled this way, searches that player's library for all cards
 * with the same name and exiles them. Then that player shuffles their library.
 * <p>
 * Used by: Haunting Echoes
 */
public record ExileNonBasicLandGraveyardAndSameNameFromLibraryEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
