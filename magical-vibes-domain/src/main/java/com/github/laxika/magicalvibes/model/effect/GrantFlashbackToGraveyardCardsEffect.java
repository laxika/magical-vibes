package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Grants flashback to cards of the specified types in the controller's graveyard
 * until end of turn. The flashback cost equals the card's mana cost.
 * (e.g. Past in Flames — CR 702.33)
 */
public record GrantFlashbackToGraveyardCardsEffect(Set<CardType> cardTypes) implements CardEffect {
}
