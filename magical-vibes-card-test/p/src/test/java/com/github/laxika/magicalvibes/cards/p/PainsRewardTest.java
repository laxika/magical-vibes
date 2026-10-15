package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PainsReward.class, OboroPalaceInTheClouds.class})
class PainsRewardTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new PainsReward(), "{2}{B}");
        harness.passBothPriorities();
    }

    private void setFourCardLibrary(Player player) {
        harness.setLibrary(player, List.of(
                new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds(),
                new OboroPalaceInTheClouds(), new OboroPalaceInTheClouds()));
    }

    @Test
    void casterChoosesOpeningBidAndWinsWhenOpponentPasses() {
        harness.setLife(player1, 20);
        setFourCardLibrary(player1);

        cast();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PainsRewardBidChoice.class);
        assertThat(((PendingInteraction.PainsRewardBidChoice) gd.interaction.activeInteraction())
                .openingBid()).isTrue();
        harness.handleXValueChosen(player1, 4);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, 16);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void highBidderLosesLifeAndDrawsFourCards() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        setFourCardLibrary(player2);
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        cast();

        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 5);
        harness.handleXValueChosen(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore + 4);
    }

    @Test
    void playersMayBidMoreLifeThanTheyHave() {
        harness.setLife(player1, 3);
        setFourCardLibrary(player1);

        cast();

        harness.handleXValueChosen(player1, 10);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, -7);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void zeroOpeningBidStillAwardsFourCards() {
        setFourCardLibrary(player1);

        cast();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void casterCanRetakeHighBidAfterMultipleRaises() {
        setFourCardLibrary(player1);
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        cast();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 4);
        harness.handleXValueChosen(player1, 6);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentAtZeroLifeStillDrawsBeforeLosingTheGame() {
        harness.setLife(player2, 3);
        setFourCardLibrary(player2);
        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        cast();

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 3);
        harness.handleXValueChosen(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 0);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore + 4);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
