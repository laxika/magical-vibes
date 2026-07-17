package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The number of cards matching {@code predicate} in the hand(s) of the players in scope. The
 * filtered sibling of {@link CardsInHand} — used for "for each [matching] card in your hand"
 * amounts such as Sacellum Godspeaker's "Add {G} for each creature card with power 5 or greater
 * revealed" (modelled as the whole set of qualifying hand cards, since revealing more only adds mana).
 */
public record MatchingCardsInHand(CountScope scope, CardPredicate predicate) implements DynamicAmount {
}
