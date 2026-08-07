package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveal the top {@code count} cards of the controller's library; put every revealed card that
 * matches into the controller's hand, and the rest on the bottom of the library in any order.
 * <p>
 * When {@code matcher} is {@code null} the match is "creature card of the source permanent's chosen
 * creature type" (see {@code Permanent.getChosenSubtype()}, Changeling-aware), which pairs with a
 * {@link ChooseSubtypeOnEnterEffect} in the {@code ON_ENTER_BATTLEFIELD} slot — Brass Herald
 * ({@code count}=4). Otherwise the predicate decides — Sylvan Messenger
 * ({@code count}=4, {@code CardSubtypePredicate(ELF)}).
 */
public record RevealTopCardsMatchingToHandRestToBottomEffect(int count, CardPredicate matcher) implements CardEffect {

    public RevealTopCardsMatchingToHandRestToBottomEffect(int count) {
        this(count, null);
    }
}
