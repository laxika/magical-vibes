package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.cards.CardPrinting;

import java.util.List;

/**
 * The printings whose names a "choose a card name" prompt offers beyond the cards in the game.
 *
 * <p>No bean of this type exists in the application: a prompt then offers every implemented
 * printing of the {@link com.github.laxika.magicalvibes.cards.CardCatalog}. A card test's context
 * supplies one that offers only the cards the test declares, so that a name prompt does not load
 * every card class there is.
 */
public interface CardNameCatalog {

    /**
     * The printings to offer. The same list instance is returned for as long as the offer stands:
     * {@link PlayerInputService} keeps the name lists it builds from it until a different instance
     * comes back.
     */
    List<CardPrinting> printings();
}
