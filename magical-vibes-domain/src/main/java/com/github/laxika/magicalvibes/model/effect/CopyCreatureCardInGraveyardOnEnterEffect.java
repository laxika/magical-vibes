package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "You may have this creature enter as a copy of [a creature card the candidates name] [, except ...]": a replacement
 * effect (CR 614.1c) on a card entering the battlefield. As it would enter, its controller may choose one of the cards
 * {@code candidates} names, found then, and it enters as a copy of that card, with the {@code exceptions} as part of
 * its copiable values (CR 707.9b).
 *
 * <p>The candidates are a static card subject: {@code Subjects.cardsInGraveyards(players, filter)} ("any creature card
 * in a graveyard", "a Doctor card in your graveyard that was put there from your library this turn") or
 * {@code Subjects.exiledCards(filter)} ("a creature card in exile with a takeover counter on it"; a card exiled face
 * down matches no filter, CR 406.3a). Graveyard cards are chosen with the graveyard prompt, exiled cards with the
 * exiled-card prompt (a lone exiled candidate is taken unasked).
 *
 * @param candidates                         the cards the copy may be of
 * @param exceptions                         the copy's exceptions ("except his name is ... and he's a 4/4 Spider
 *                                           Human Hero in addition to his other types")
 * @param grantHaste                         "if you do, it gains haste until end of turn"
 * @param exileCopiedGraveyardCardAfterEntry "when you do, exile that card"
 * @param exileTwoAndAddOtherPowerCounters   The Mimeoplasm's "exile two creature cards from graveyards. If you do, it
 *                                           enters as a copy of one of those cards with additional +1/+1 counters equal
 *                                           to the power of the other card"
 */
public record CopyCreatureCardInGraveyardOnEnterEffect(
        CardSubject candidates,
        CopyExceptions exceptions,
        boolean grantHaste,
        boolean exileCopiedGraveyardCardAfterEntry,
        boolean exileTwoAndAddOtherPowerCounters) implements ReplacementEffect {

    public CopyCreatureCardInGraveyardOnEnterEffect {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(exceptions, "exceptions");
        if (!(candidates instanceof CardSubject.InGraveyards || candidates instanceof CardSubject.InExile)) {
            throw new IllegalArgumentException("A copy as it enters chooses among graveyard or exiled cards: "
                    + candidates);
        }
        if (!exceptions.cardTypes().isEmpty()) {
            throw new IllegalArgumentException("A copy as it enters replaces no card types");
        }
        if (!(candidates instanceof CardSubject.InGraveyards)
                && (exileCopiedGraveyardCardAfterEntry || exileTwoAndAddOtherPowerCounters)) {
            throw new IllegalArgumentException("Only a graveyard card is exiled by the copy");
        }
    }

    /** "You may have this creature enter as a copy of [a card {@code candidates} names]." */
    public static CopyCreatureCardInGraveyardOnEnterEffect of(CardSubject candidates) {
        return new CopyCreatureCardInGraveyardOnEnterEffect(candidates, CopyExceptions.NONE, false, false, false);
    }

    /** This copy "except ..." ({@link CopyExceptions}). */
    public CopyCreatureCardInGraveyardOnEnterEffect except(CopyExceptions exceptions) {
        return new CopyCreatureCardInGraveyardOnEnterEffect(candidates, exceptions, grantHaste,
                exileCopiedGraveyardCardAfterEntry, exileTwoAndAddOtherPowerCounters);
    }

    /** "If you do, it gains haste until end of turn." */
    public CopyCreatureCardInGraveyardOnEnterEffect gainingHaste() {
        return new CopyCreatureCardInGraveyardOnEnterEffect(candidates, exceptions, true,
                exileCopiedGraveyardCardAfterEntry, exileTwoAndAddOtherPowerCounters);
    }

    /** "When you do, exile that card." */
    public CopyCreatureCardInGraveyardOnEnterEffect exilingCopiedCard() {
        return new CopyCreatureCardInGraveyardOnEnterEffect(candidates, exceptions, grantHaste, true,
                exileTwoAndAddOtherPowerCounters);
    }

    /**
     * "You may exile two [candidates]. If you do, it enters as a copy of one of those cards with a number of additional
     * +1/+1 counters on it equal to the power of the other card."
     */
    public CopyCreatureCardInGraveyardOnEnterEffect exilingTwoAddingTheOthersPower() {
        return new CopyCreatureCardInGraveyardOnEnterEffect(candidates, exceptions, grantHaste,
                exileCopiedGraveyardCardAfterEntry, true);
    }

    /** Whether the candidates are exiled cards rather than graveyard cards. */
    public boolean choosesFromExile() {
        return candidates instanceof CardSubject.InExile;
    }
}
