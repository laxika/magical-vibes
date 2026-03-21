package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static effect stored in an emblem that permanently grants flashback to cards of the
 * specified types in the controller's graveyard. The flashback cost equals the card's
 * mana cost, and the card is exiled after resolution (standard flashback disposition).
 * <p>
 * Used by Jaya Ballard's emblem: "You may cast instant and sorcery spells from your graveyard.
 * If a spell cast this way would be put into your graveyard, exile it instead."
 *
 * @param cardTypes the card types that gain flashback (e.g. INSTANT, SORCERY)
 */
public record EmblemGrantsFlashbackEffect(Set<CardType> cardTypes) implements CardEffect {
}
