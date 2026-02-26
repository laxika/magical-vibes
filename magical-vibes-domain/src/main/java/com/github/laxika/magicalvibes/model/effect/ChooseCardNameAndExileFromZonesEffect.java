package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Choose a card name (excluding cards of the given types), then search target player's
 * graveyard, hand, and library for any number of cards with that name and exile them.
 * Then that player shuffles their library.
 * <p>
 * Used by: Memoricide, Cranial Extraction, etc.
 */
public record ChooseCardNameAndExileFromZonesEffect(List<CardType> excludedTypes) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
