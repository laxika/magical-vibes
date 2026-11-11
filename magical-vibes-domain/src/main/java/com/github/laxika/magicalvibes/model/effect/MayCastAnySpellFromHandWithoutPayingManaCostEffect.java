package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast a [filter] spell from your hand without paying its mana cost."
 * (Maelstrom Archangel — any spell; Wildfire Eternal — instant or sorcery)
 *
 * <p>On resolution the controller is offered each matching nonland hand card as a
 * may-cast-from-hand-without-paying choice via {@link MayCastFromHandWithoutPayingManaCostEffect};
 * casting one clears the rest, so only a single spell is cast. A {@code null}
 * {@code spellFilter} matches every nonland hand card.
 *
 * @param spellFilter which hand cards are eligible ({@code null} = any nonland)
 * @param maxManaValue maximum eligible mana value ({@code null} = no maximum)
 * @param afterSuccessfulCastEffect optional "if you do" follow-up, resolved as part of this ability once the spell is cast
 */
public record MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                                  DynamicAmount maxManaValue,
                                                                  CardEffect afterSuccessfulCastEffect,
                                                                  boolean drawnCardsOnly, boolean revealDrawnCards)
        implements CardEffect {

    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                               DynamicAmount maxManaValue) {
        this(spellFilter, maxManaValue, null);
    }

    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                               DynamicAmount maxManaValue,
                                                               CardEffect afterSuccessfulCastEffect) {
        this(spellFilter, maxManaValue, afterSuccessfulCastEffect, false, false);
    }

    /** Any nonland spell (Maelstrom Archangel). */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect() {
        this(null, null, null);
    }

    /** A matching nonland spell, capped by a dynamic mana value. */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(DynamicAmount maxManaValue) {
        this(null, maxManaValue, null);
    }

    /** A matching nonland spell with no mana-value cap. */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter) {
        this(spellFilter, null, null);
    }
}
