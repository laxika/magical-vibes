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
 * hands the pile split to an opponent, then lets the controller choose which pile goes to their
 * hand. Reuses the shared card-pile flow ({@code PendingPileSeparation}).
 */
public record RevealTopCardsAndSeparateEffect(int count, CardPileDisposition disposition) implements CardEffect {

    /** Fact-or-Fiction default: the unchosen pile goes to the controller's graveyard. */
    public RevealTopCardsAndSeparateEffect(int count) {
        this(count, CardPileDisposition.HAND);
    }
}
