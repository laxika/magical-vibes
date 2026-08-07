package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the controller's library. If the revealed card matches the given
 * {@link CardPredicate}, it is put into the controller's hand; otherwise it stays revealed on top
 * of the library.
 *
 * <p>Used by Llanowar Empath ("scry 2, then reveal the top card of your library. If it's a creature
 * card, put it into your hand.") with a {@code CardTypePredicate(CREATURE)}.
 */
public record RevealTopCardMatchingToHandEffect(CardPredicate matchPredicate) implements CardEffect {
}
