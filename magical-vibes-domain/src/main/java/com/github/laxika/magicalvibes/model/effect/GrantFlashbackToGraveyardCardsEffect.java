package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Grants flashback to cards of the specified types in the controller's graveyard.
 * In a spell slot the grant lasts until end of turn; in a static slot it lasts while
 * the source permanent remains on the battlefield. The flashback cost equals the card's
 * mana cost.
 */
public record GrantFlashbackToGraveyardCardsEffect(Set<CardType> cardTypes) implements CardEffect {
}
