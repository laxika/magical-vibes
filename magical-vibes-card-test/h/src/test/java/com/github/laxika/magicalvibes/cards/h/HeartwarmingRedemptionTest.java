package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartwarmingRedemption.class, GrizzlyBears.class, Island.class, NarsetParterOfVeils.class})
class HeartwarmingRedemptionTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the hand, draws that many plus one, and gains life equal to the resulting hand size")
    void redrawsHandAndGainsLife() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(
                new HeartwarmingRedemption(), new GrizzlyBears(), new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws one card and gains one life when the spell is cast with no other cards in hand")
    void emptyHandAfterCastingStillDrawsAndGainsLife() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new HeartwarmingRedemption()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gains life for only the card actually drawn under an opponent's Narset")
    void restrictedDrawsReduceLifeGain() {
        harness.addToBattlefield(player2, new NarsetParterOfVeils());
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);
        Island drawn = new Island();
        Island remaining = new Island();
        Island last = new Island();
        Island discarded = new Island();
        harness.setLibrary(player1, List.of(drawn, remaining, last));
        harness.setHand(player1, List.of(new HeartwarmingRedemption(), discarded, new Island()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, last);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).hasSize(3);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still discards the hand but gains no life when Narset prevents every draw")
    void preventedDrawsLeaveNoCardsAndNoLifeGain() {
        harness.addToBattlefield(player2, new NarsetParterOfVeils());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        Island discarded = new Island();
        harness.setHand(player1, List.of(new HeartwarmingRedemption(), discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).hasSize(2);
        harness.assertLife(player1, 20);
    }
}
