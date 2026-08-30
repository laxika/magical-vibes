package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.CardType;

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
 * @param useEntryTarget   when true, the entry's pre-bound target is carried through the discard
 *                         choice to the reflexive effect instead of asking for a new target
 * @param alternateCardType if non-null, use {@code alternateThenEffect} when the discarded card
 *                          has this type
 * @param alternateThenEffect alternate reflexive effect selected by {@code alternateCardType}
 */
public record DiscardCardThenEffect(
        CardPredicate filter,
        CardEffect thenEffect,
        String cardDescription,
        CardPredicate condition,
        boolean useEntryTarget,
        CardType alternateCardType,
        CardEffect alternateThenEffect
) implements CardEffect {

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription) {
        this(filter, thenEffect, cardDescription, null, false, null, null);
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardPredicate condition) {
        this(filter, thenEffect, cardDescription, condition, false, null, null);
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 boolean useEntryTarget) {
        this(filter, thenEffect, cardDescription, null, useEntryTarget, null, null);
    }

    public DiscardCardThenEffect(CardPredicate filter, CardEffect thenEffect, String cardDescription,
                                 CardType alternateCardType, CardEffect alternateThenEffect) {
        this(filter, thenEffect, cardDescription, null, false, alternateCardType, alternateThenEffect);
    }
}
