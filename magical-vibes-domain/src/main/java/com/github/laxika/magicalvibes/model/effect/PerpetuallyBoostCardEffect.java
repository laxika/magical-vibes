package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "[It] perpetually gets +P/+T" (digital only). With {@code cards}, the cards that subject names ("the duplicate", "that
 * card", "that creature": {@code Subjects.cardsOf(...)}); without, the ability's own card ({@code card} is this card,
 * "this creature perpetually gets +1/+1"), while it is still the object the ability refers to: the source permanent,
 * the card in the graveyard or exile stay its ability works from, the card in the hand its ability was activated from,
 * or the permanent this resolution returned it as (CR 400.7, 400.7e, 400.7j). The change is made to the physical card,
 * so a copying permanent's own card is changed.
 */
public record PerpetuallyBoostCardEffect(Card card, DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                         CardSubject cards)
        implements CardEffect {

    public PerpetuallyBoostCardEffect {
        if (card == null && cards == null) {
            throw new IllegalArgumentException("Either this card or a card subject is boosted");
        }
    }

    public PerpetuallyBoostCardEffect(Card card, int powerBoost, int toughnessBoost) {
        this(card, new Fixed(powerBoost), new Fixed(toughnessBoost), null);
    }

    public PerpetuallyBoostCardEffect(Card card, DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(card, powerBoost, toughnessBoost, null);
    }

    /** "It perpetually gets +N/+M" of the cards {@code cards} names. */
    public static PerpetuallyBoostCardEffect toCards(CardSubject cards, int powerBoost, int toughnessBoost) {
        return new PerpetuallyBoostCardEffect(null, new Fixed(powerBoost), new Fixed(toughnessBoost), cards);
    }

    /** "It perpetually gets +X/+Y" of the cards {@code cards} names, the amounts read as the ability resolves. */
    public static PerpetuallyBoostCardEffect toCards(CardSubject cards, DynamicAmount powerBoost,
                                                     DynamicAmount toughnessBoost) {
        return new PerpetuallyBoostCardEffect(null, powerBoost, toughnessBoost, cards);
    }
}
