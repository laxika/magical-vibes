package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Objects;

/**
 * "Exile cards from the top of your library until you exile a nonland card", "target opponent exiles cards from the top
 * of their library until they exile a nonland card": each player {@link #library()} names (in APNAP order) exiles the
 * top card of their library one at a time until they exile a card matching {@link #until()}, or the library runs out.
 * Every card stays in exile. The step records every exiled card; "that card" in the steps that follow is the one that
 * matched, {@code Subjects.cardsThisWay(StepKind.EXILE, until)} (no card exiled before it matched): "you may cast that
 * card without paying its mana cost" is {@link MayCastDiscardedCardWithoutPayingManaCostEffect}, "until end of turn,
 * you may cast that card" is {@link AllowPlayCardsFromExileEffect}, "put two time counters on that card" is
 * {@link PutTimeCountersAndGainSuspendEffect}, "damage equal to that card's mana value" is
 * {@code AmountThisWay.exiledManaValue()} when {@code until} is nonland (the lands before it have mana value 0).
 *
 * <p>{@link #untilTotalManaValue} is the other stop: "until that player has exiled cards with total mana value 20 or
 * greater" (Tasha's Hideous Laughter, Dream Harvest), each player's own total.
 *
 * <p>{@link #withSource()} is "exiled with [this]" (CR 607.2a): every exiled card is exiled with the source
 * permanent, so a linked ability reads them as {@code Subjects.exiledWithSource()} (Krang &amp; Shredder's Disappear
 * cast).
 *
 * @param library        whose library: {@code you()}, {@code eachOpponent()}, or {@code targetPlayer()}, which
 *                       declares the (harmful) player target
 * @param until          the card that stops the exiling; {@code null} for a total mana value
 * @param totalManaValue the total mana value that stops it, or 0 for a card
 * @param linkedToSource whether the cards are exiled with the source permanent
 */
public record ExileCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until, int totalManaValue,
                                           boolean linkedToSource)
        implements CardEffect {

    /** A nonland card. */
    public static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    public ExileCardsFromTopUntilEffect {
        Objects.requireNonNull(library, "library");
        if ((until == null) == (totalManaValue <= 0)) {
            throw new IllegalArgumentException("Either a card or a positive total mana value stops the exiling");
        }
    }

    public ExileCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until) {
        this(library, Objects.requireNonNull(until, "until"), 0, false);
    }

    /** This exile, every card exiled with the source permanent (CR 607.2a). */
    public ExileCardsFromTopUntilEffect withSource() {
        return new ExileCardsFromTopUntilEffect(library, until, totalManaValue, true);
    }

    /** "[Player] exiles cards from the top of their library until they have exiled cards with total mana value N or greater." */
    public static ExileCardsFromTopUntilEffect untilTotalManaValue(PlayerSubject library, int total) {
        return new ExileCardsFromTopUntilEffect(library, null, total, false);
    }

    /** "[Player] exiles cards from the top of their library until they exile a nonland card." */
    public static ExileCardsFromTopUntilEffect untilNonland(PlayerSubject library) {
        return new ExileCardsFromTopUntilEffect(library, NONLAND);
    }

    @Override
    public TargetSpec targetSpec() {
        return library.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
