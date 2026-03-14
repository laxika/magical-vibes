package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Holds the state between Sphinx Ambassador's library search and the opponent's card name choice.
 * The selected card is "set aside" from the library while the opponent names a card.
 *
 * @param selectedCard    the card selected by the Sphinx Ambassador's controller from the opponent's library
 * @param controllerId    the controller of Sphinx Ambassador
 * @param targetPlayerId  the damaged player whose library was searched
 * @param sourceCard      the Sphinx Ambassador card (for may ability description)
 */
public record PendingSphinxAmbassadorChoice(Card selectedCard, UUID controllerId, UUID targetPlayerId, Card sourceCard) {
}
