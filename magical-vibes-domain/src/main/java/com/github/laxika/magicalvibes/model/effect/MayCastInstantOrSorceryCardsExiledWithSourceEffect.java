package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers the controller one instant or sorcery card exiled with the source permanent to cast
 * during the resolving ability. The cast uses the card's normal mana cost, and the card is put on
 * the bottom of its owner's library instead of into a graveyard when the spell leaves the stack.
 */
public record MayCastInstantOrSorceryCardsExiledWithSourceEffect() implements CardEffect {
}
