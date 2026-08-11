package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardPileDisposition;

/**
 * "Reveal the top {@code count} cards of your library. An opponent separates those cards into two
 * piles. Put one pile into your hand and the other ..." — the tail depends on {@code disposition}:
 * {@link CardPileDisposition#HAND} sends the unchosen pile to the controller's graveyard (Fact or
 * Fiction; the enters trigger on Unesh, Criosphinx Sovereign) and
 * {@link CardPileDisposition#HAND_AND_BOTTOM} sends it to the bottom of their library in any order
 * (Jace, Architect of Thought's −2).
 *
 * <p>Non-targeting. Resolution removes the top {@code count} cards from the controller's library,
 * hands the pile split to the appropriate player, then lets the appropriate player choose which
 * pile goes to their hand. When {@code controllerSeparates} is true, the controller separates and
 * an opponent chooses. Reuses the shared card-pile flow ({@code PendingPileSeparation}).
 */
public record RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition,
                                              boolean controllerSeparates) implements CardEffect {

    /** Fact-or-Fiction default: the unchosen pile goes to the controller's graveyard. */
    public RevealTopCardsAndSeparateEffect(int count) {
        this(count, CardPileDisposition.HAND, false);
    }

    public RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition) {
        this(count, disposition, false);
    }

    /** Steam Augury variant: the controller separates and an opponent chooses a pile. */
    public RevealTopCardsAndSeparateEffect(int count, boolean controllerSeparates) {
        this(count, CardPileDisposition.HAND, controllerSeparates);
    }
}
