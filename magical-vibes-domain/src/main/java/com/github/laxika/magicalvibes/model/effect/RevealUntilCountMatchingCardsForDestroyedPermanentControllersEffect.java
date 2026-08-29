package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * For each player recorded as controlling a permanent destroyed earlier on the same stack entry,
 * reveals that many matching cards from their library, exiles the matches, and then puts those
 * cards onto the battlefield together before shuffling the other revealed cards back.
 */
public record RevealUntilCountMatchingCardsForDestroyedPermanentControllersEffect(
        Set<CardType> cardTypes
) implements CardEffect {
}
