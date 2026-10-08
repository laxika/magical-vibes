package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmugglersShare.class, GrizzlyBears.class, Forest.class})
class SmugglersShareTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    void drawsAndCreatesTreasuresForEachQualifyingOpponent() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(new Forest(), new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNothingWhenNoOpponentMeetsEitherThreshold() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(new Forest()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void drawsOnlyOneCardForOpponentWhoDrewMoreThanTwoOnTheirTurn() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        gd.cardsDrawnThisTurn.put(player2.getId(), 4);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(new Forest()));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsOnlyOneTreasureForOpponentWithMoreThanTwoLandEntriesWithoutDrawing() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(new Forest(), new Forest(), new Forest()));

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void ignoresControllersDrawsAndLandEntriesAndOpponentsNonlandEntries() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                List.of(new Forest(), new Forest()));
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void evaluatesThresholdsAtResolutionEvenWhenNeitherWasMetAtTriggerTime() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(new Forest()));

        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void resolvesAfterSourceLeavesAndCountsLandEntriesNoLongerOnBattlefield() {
        harness.addToBattlefield(player1, new SmugglersShare());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.cardsDrawnThisTurn.put(player2.getId(), 2);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(new Forest(), new Forest()));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
