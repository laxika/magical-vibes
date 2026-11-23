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
 * {@code spellFilter} matches every nonland hand card. A filter about the source (Brain in a Jar's
 * "mana value equal to the number of charge counters on this artifact") reads the source as it is, or as
 * it last existed once it has left the battlefield; one about the triggering spell (Baral and Kari Zev's "shares a
 * card type with it") reads that spell.
 *
 * @param spellFilter which hand cards are eligible ({@code null} = any nonland)
 * @param maxManaValue maximum eligible mana value ({@code null} = no maximum)
 * @param afterSuccessfulCastEffect optional "if you do" follow-up, resolved as part of this ability once the spell is cast
 * @param declineEffect optional "if you don't" follow-up, resolved as part of this ability once no spell is cast: every
 *                      offer was declined or couldn't be cast, or nothing was offered (In Search of Greatness's
 *                      scry, Kellan, the Kid's land, Baral and Kari Zev's token)
 */
public record MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                                  DynamicAmount maxManaValue,
                                                                  CardEffect afterSuccessfulCastEffect,
                                                                  boolean drawnCardsOnly, boolean revealDrawnCards,
                                                                  CardEffect declineEffect)
        implements CardEffect {

    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                               DynamicAmount maxManaValue,
                                                               CardEffect afterSuccessfulCastEffect,
                                                               boolean drawnCardsOnly, boolean revealDrawnCards) {
        this(spellFilter, maxManaValue, afterSuccessfulCastEffect, drawnCardsOnly, revealDrawnCards, null);
    }

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

    /** This effect with "if you don't, [effect]" ({@code declineEffect}). */
    public MayCastAnySpellFromHandWithoutPayingManaCostEffect orElse(CardEffect effect) {
        return new MayCastAnySpellFromHandWithoutPayingManaCostEffect(spellFilter, maxManaValue,
                afterSuccessfulCastEffect, drawnCardsOnly, revealDrawnCards, effect);
    }
}
