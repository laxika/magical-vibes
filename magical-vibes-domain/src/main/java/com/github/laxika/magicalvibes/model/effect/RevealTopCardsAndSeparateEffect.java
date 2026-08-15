package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardPileDisposition;

/**
 * "Reveal the top {@code count} cards of your library. An opponent separates those cards into two
 * piles. Put one pile into your hand and the other ..." — the tail depends on {@code disposition}:
 * {@link CardPileDisposition#HAND} sends the unchosen pile to the controller's graveyard (Fact or
 * Fiction; the enters trigger on Unesh, Criosphinx Sovereign), while
 * {@link CardPileDisposition#HAND_WITH_FACE_DOWN_PILE} supports Curator of Destinies and Fortune's
 * Favor by keeping one pile face down and using the same final destinations, and
 * {@link CardPileDisposition#HAND_AND_BOTTOM} sends it to the bottom of their library in any order
 * (Jace, Architect of Thought's −2).
 *
 * <p>The default form is non-targeting. Resolution removes the top {@code count} cards from the
 * controller's library, hands the pile split to the appropriate player, then lets the appropriate
 * player choose which pile goes to their hand. When {@code controllerSeparates} is true, the
 * controller separates and an opponent chooses. When {@code targetedSeparator} is true, the stack
 * entry's target is the player who separates the piles. Reuses the shared card-pile flow
 * ({@code PendingPileSeparation}).
 */
public record RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition,
                                              boolean controllerSeparates, boolean targetedSeparator,
                                              boolean faceDownPile) implements CardEffect {

    /** Fact-or-Fiction default: the unchosen pile goes to the controller's graveyard. */
    public RevealTopCardsAndSeparateEffect(int count) {
        this(count, CardPileDisposition.HAND, false, false, false);
    }

    public RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition) {
        this(count, disposition, false, false, false);
    }

    public RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition,
                                           boolean controllerSeparates) {
        this(count, disposition, controllerSeparates, false,
                disposition == CardPileDisposition.HAND_WITH_FACE_DOWN_PILE);
    }

    /** Steam Augury variant: the controller separates and an opponent chooses a pile. */
    public RevealTopCardsAndSeparateEffect(int count, boolean controllerSeparates) {
        this(count, CardPileDisposition.HAND, controllerSeparates, false, false);
    }

    public RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition,
                                            boolean controllerSeparates, boolean targetedSeparator,
                                            boolean faceDownPile) {
        this.count = count;
        this.disposition = disposition;
        this.controllerSeparates = controllerSeparates;
        this.targetedSeparator = targetedSeparator;
        this.faceDownPile = faceDownPile;
    }

    @Override
    public TargetSpec targetSpec() {
        return targetedSeparator ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
