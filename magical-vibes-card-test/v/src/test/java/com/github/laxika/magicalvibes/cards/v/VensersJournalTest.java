package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VensersJournal.class, Forest.class, Mountain.class, Plains.class})
class VensersJournalTest extends BaseCardTest {

    @Test
    @DisplayName("Controller does not discard with more than 7 cards when Journal is on battlefield")
    void noMaxHandSizePreventsDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new VensersJournal());

        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        )));

        harness.getGameService().advanceStep(gd);

        // No discard prompt — Journal removes hand size limit
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Gains life equal to number of cards in hand at upkeep")
    void gainsLifeEqualToHandSize() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Mountain()
        )));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Gains no life when hand is empty")
    void gainsNoLifeWithEmptyHand() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Gains 1 life with exactly 1 card in hand")
    void gainsOneLifeWithOneCard() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Mountain()
        )));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent's Journal does not gain life for you")
    void opponentJournalDoesNotGainLifeForYou() {
        harness.addToBattlefield(player2, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest(), new Mountain()
        )));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1's life unchanged — the Journal belongs to player2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life gain is logged")
    void lifeGainIsLogged() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Forest(), new Forest()
        )));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gameLogContains("gains 2 life")).isTrue();
    }
    @Test
    @DisplayName("Counts cards in hand when the upkeep trigger resolves")
    void countsHandAtResolution() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Mountain(), new Plains(), new Forest()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Forest(), new Mountain(), new Plains()));
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Gains no life if the hand becomes empty before resolution")
    void countsEmptyHandAtResolution() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Journal triggers independently")
    void multipleJournalsEachGainLife() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Opponent gains life using their own hand on their upkeep")
    void opponentJournalCountsOpponentsHand() {
        harness.addToBattlefield(player2, new VensersJournal());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Mountain(), new Plains()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }
    @Test
    @DisplayName("Opponent's Journal does not remove your maximum hand size")
    void opponentJournalDoesNotPreventCleanupDiscard() {
        harness.addToBattlefield(player2, new VensersJournal());
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.cleanupDiscardPending).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Journal prevents a cleanup discard prompt for its controller")
    void journalPreventsCleanupDiscardPrompt() {
        harness.addToBattlefield(player1, new VensersJournal());
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.cleanupDiscardPending).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }
}
