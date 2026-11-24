package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may cast a [filter] spell from your hand without paying its mana cost."
 * (Maelstrom Archangel — any spell; Wildfire Eternal — instant or sorcery)
 *
 * <p>On resolution the controller is offered each matching nonland hand card as a
 * may-cast-from-hand-without-paying choice via {@link MayCastFromHandWithoutPayingManaCostEffect};
 * casting one clears the rest, so only a single spell is cast unless {@link #maxCount} allows more. A {@code null}
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
 * @param maxCount how many spells may be cast: 1 ("a spell"), N ("up to three spells", Power Without Equal) or
 *                 {@link Integer#MAX_VALUE} ("any number of spells", Aetherflux Conduit). With more than one, each spell
 *                 cast is followed by a new round of offers for the rest of the hand, so a card declined before is
 *                 offered again and the caster chooses the order of the casts (CR 608.2g); the filter and the
 *                 mana-value limit of the first round are kept (CR 608.2h). Declining every offer of a round ends it.
 */
public record MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                                  DynamicAmount maxManaValue,
                                                                  CardEffect afterSuccessfulCastEffect,
                                                                  boolean drawnCardsOnly, boolean revealDrawnCards,
                                                                  CardEffect declineEffect,
                                                                  int maxCount)
        implements CardEffect {

    public MayCastAnySpellFromHandWithoutPayingManaCostEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        if (maxCount > 1 && (afterSuccessfulCastEffect != null || declineEffect != null)) {
            throw new IllegalArgumentException("An \"if you do\" or \"if you don't\" rider needs a single spell");
        }
    }

    public MayCastAnySpellFromHandWithoutPayingManaCostEffect(CardPredicate spellFilter,
                                                               DynamicAmount maxManaValue,
                                                               CardEffect afterSuccessfulCastEffect,
                                                               boolean drawnCardsOnly, boolean revealDrawnCards,
                                                               CardEffect declineEffect) {
        this(spellFilter, maxManaValue, afterSuccessfulCastEffect, drawnCardsOnly, revealDrawnCards, declineEffect, 1);
    }

    /** "You may cast any number of spells from your hand without paying their mana costs." */
    public static MayCastAnySpellFromHandWithoutPayingManaCostEffect anyNumber() {
        return upTo(Integer.MAX_VALUE);
    }

    /** "You may cast up to {@code count} spells from your hand without paying their mana costs." */
    public static MayCastAnySpellFromHandWithoutPayingManaCostEffect upTo(int count) {
        return new MayCastAnySpellFromHandWithoutPayingManaCostEffect(null, null, null, false, false, null, count);
    }

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
                afterSuccessfulCastEffect, drawnCardsOnly, revealDrawnCards, effect, maxCount);
    }
}
