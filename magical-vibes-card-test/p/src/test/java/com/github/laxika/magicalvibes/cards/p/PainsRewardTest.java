package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    @DisplayName("The caster chooses the opening bid and wins when the opponent passes")
    void casterChoosesOpeningBidAndWinsWhenOpponentPasses() {
        harness.setLife(player1, 20);
        setFourCardLibrary(player1);

        cast();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LifeBidChoice.class);
        assertThat(((PendingInteraction.LifeBidChoice) gd.interaction.activeInteraction())
                .openingBid()).isTrue();
        harness.handleXValueChosen(player1, 4);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player1, 16);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The high bidder loses life equal to the high bid and draws four cards")
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
    @DisplayName("A player may bid more life than they have")
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
    @DisplayName("An opening bid of 0 that stands still draws four cards")
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
    @DisplayName("The caster can retake the high bid after several raises")
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
    @DisplayName("A high bidder brought to 0 life still draws before losing the game")
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

    @Test
    @DisplayName("After the caster's opening bid, the other players bid in turn order from the caster, and a player who passed may top a later bid")
    void otherPlayersBidInTurnOrderFromTheCaster() {
        Player player3 = seatThirdPlayerBeforePlayer1();
        setFourCardLibrary(player2);

        cast();

        harness.handleXValueChosen(player1, 1);
        assertThat(biddingPlayerId()).isEqualTo(player2.getId());
        harness.handleXValueChosen(player2, 0);
        assertThat(biddingPlayerId()).isEqualTo(player3.getId());
        harness.handleXValueChosen(player3, 2);
        assertThat(biddingPlayerId()).isEqualTo(player1.getId());
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 3);
        harness.handleXValueChosen(player3, 0);
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.assertLife(player3, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private UUID biddingPlayerId() {
        return gd.interaction.activeInteraction(PendingInteraction.LifeBidChoice.class).playerId();
    }

    /** Seats a third player first, so turn order after player 1 is player 2, then the third player. */
    private Player seatThirdPlayerBeforePlayer1() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(0, player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
