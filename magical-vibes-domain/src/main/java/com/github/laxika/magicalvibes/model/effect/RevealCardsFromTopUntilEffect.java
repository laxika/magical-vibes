package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "Reveal cards from the top of your library until you reveal a land card", "target opponent reveals cards from the
 * top of their library until they reveal three creature cards", "until an artifact card or X cards are revealed,
 * whichever comes first": each player {@link #library()} names reveals cards from the top of their library until
 * enough of them match {@link #until()} or the library runs out. The cards stay where they are (CR 701.20b) and the
 * step records them ({@code StepKind.REVEAL}): "that card" is {@code Subjects.revealedThisWay(until)}, "the rest"
 * {@code Subjects.restRevealedThisWay(until)}, "all cards revealed this way" and "the cards not put onto the
 * battlefield" {@code Subjects.revealedThisWay()}, which names those still in the library.
 *
 * @param library whose library; only {@code targetPlayer()} declares a (harmful) player target
 * @param until   the cards that end the revealing
 * @param matches how many of them it takes; none revealed for 0
 * @param atMost  "or X cards are revealed": the most cards revealed; {@code null} for no limit
 */
public record RevealCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until, DynamicAmount matches,
                                            DynamicAmount atMost) implements CardEffect {

    public RevealCardsFromTopUntilEffect {
        Objects.requireNonNull(library, "library");
        Objects.requireNonNull(until, "until");
        Objects.requireNonNull(matches, "matches");
    }

    /** "[Player] reveals cards from the top of their library until they reveal a [card]." */
    public RevealCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until) {
        this(library, until, new Fixed(1), null);
    }

    /** "Until you reveal a [card]": the controller's library. */
    public RevealCardsFromTopUntilEffect(CardPredicate until) {
        this(Subjects.you(), until);
    }

    /** "Until you reveal three nonland cards", "until you reveal X creature cards". */
    public RevealCardsFromTopUntilEffect(PlayerSubject library, CardPredicate until, DynamicAmount matches) {
        this(library, until, matches, null);
    }

    /** "Until [a card] or [max] cards are revealed, whichever comes first." */
    public RevealCardsFromTopUntilEffect orUntilRevealed(DynamicAmount max) {
        return new RevealCardsFromTopUntilEffect(library, until, matches, Objects.requireNonNull(max, "max"));
    }

    @Override
    public TargetSpec targetSpec() {
        return library.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
