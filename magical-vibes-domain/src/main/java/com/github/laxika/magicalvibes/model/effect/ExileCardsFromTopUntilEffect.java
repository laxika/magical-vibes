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
 * @param library whose library: {@code you()}, or {@code targetPlayer()}, which declares the (harmful) player target
 * @param until   the card that stops the exiling
 */
public record ExileCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until) implements CardEffect {

    /** A nonland card. */
    public static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    public ExileCardsFromTopUntilEffect {
        Objects.requireNonNull(library, "library");
        Objects.requireNonNull(until, "until");
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
