package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Controller mills one card. If the milled card matches any of the specified types,
 * transforms the source permanent to its back face.
 *
 * <p>Used by Aberrant Researcher ("At the beginning of your upkeep, mill a card.
 * If an instant or sorcery card was milled this way, transform this creature.").
 * Per ruling, the type check uses the milled card even if a replacement effect sends
 * it somewhere other than the graveyard.
 */
public record MillAndTransformIfTypesEffect(Set<CardType> cardTypes) implements CardEffect {
}
