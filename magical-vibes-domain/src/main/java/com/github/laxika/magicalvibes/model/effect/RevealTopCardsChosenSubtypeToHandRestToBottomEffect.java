package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top {@code count} cards of the controller's library; put every creature card of the
 * source permanent's chosen creature type (see {@code Permanent.getChosenSubtype()}, Changeling-aware)
 * into the controller's hand, and the rest on the bottom of the library in any order.
 * <p>
 * The chosen subtype is read from the source permanent at resolution time, so this pairs with a
 * {@link ChooseSubtypeOnEnterEffect} in the {@code ON_ENTER_BATTLEFIELD} slot. Used by Brass Herald
 * ({@code count}=4).
 */
public record RevealTopCardsChosenSubtypeToHandRestToBottomEffect(int count) implements CardEffect {
}
