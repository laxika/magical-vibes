package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "Discard a [card matching filter]. If you do, [thenEffect]."
 *
 * <p>At resolution the controller discards a card from their hand that matches {@code filter}.
 * If (and only if) a card is actually discarded, {@code thenEffect} is pushed onto the stack as a
 * reflexive triggered ability. If the hand has no matching card, nothing happens. Wrap in
 * {@link MayEffect} for the optional "you may discard" case (Pack Guardian).
 *
 * @param filter           predicate to match discardable hand cards
 * @param thenEffect       effect to execute after a successful discard
 * @param cardDescription  human-readable description of what is discarded (e.g. "a land card")
 * @param condition        optional predicate tested against the discarded card before executing
 *                         {@code thenEffect}
 */
public record DiscardCardThenEffect(
        CardPredicate filter,
        CardEffect thenEffect,
        String cardDescription,
        CardPredicate condition
) implements CardEffect {

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription) {
        this(filter, thenEffect, cardDescription, null);
    }
}
