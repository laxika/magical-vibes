package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DragonbornImmolator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderbridgeWarlock.class, DragonbornImmolator.class})
class UnderbridgeWarlockTest extends BaseCardTest {

    @Test
    void qualifyingEndStepResolvesTheBoonAndConsumesIt() {
        addWarlock();
        setLibraryWithFourCards();
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        triggerEndStep();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.boons).isEmpty();
    }

    @Test
    void boonRiderAppliesWhileTheDeathConditionIsFalseAndTheBoonWaits() {
        addWarlock();
        setLibraryWithFourCards();
        gd.creatureDeathCountThisTurn.put(player2.getId(), 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        triggerEndStep();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.boons).hasSize(1);
    }

    @Test
    void noBoonMeansNoMillDrawOrLifeLoss() {
        harness.addToBattlefield(player1, new UnderbridgeWarlock());
        setLibraryWithFourCards();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        triggerEndStep();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void qualifyingDeathsCanBeSplitBetweenBothPlayers() {
        addWarlock();
        setLibraryWithFourCards();
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 2);

        triggerEndStep();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 15);
        assertThat(gd.boons).isEmpty();
    }

    @Test
    void opponentsBoonDoesNotEnableTheRider() {
        harness.addToBattlefield(player1, new UnderbridgeWarlock());
        harness.enterBattlefieldAndReturn(player2, new UnderbridgeWarlock());
        harness.passBothPriorities();
        setLibraryWithFourCards();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        triggerEndStep();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.boons).hasSize(1);
    }

    @Test
    void boonResolvesAfterWarlockLeavesWithoutItsPermanentRider() {
        Permanent warlock = harness.enterBattlefieldAndReturn(player1, new UnderbridgeWarlock());
        harness.passBothPriorities();
        setLibraryWithFourCards();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, warlock));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 3);

        triggerEndStep();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.boons).isEmpty();
    }

    @Test
    @CardUsed({UnderbridgeWarlock.class, DragonbornImmolator.class})
    void riderRecognizesGiftOfTiamatBoonWithoutWarlocksOwnBoon() {
        harness.addToBattlefield(player1, new UnderbridgeWarlock());
        Permanent immolator = harness.addToBattlefieldAndReturn(player1, new DragonbornImmolator());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, immolator));
        harness.passBothPriorities();
        setLibraryWithFourCards();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        triggerEndStep();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void addWarlock() {
        harness.enterBattlefieldAndReturn(player1, new UnderbridgeWarlock());
        harness.passBothPriorities();
    }

    private void setLibraryWithFourCards() {
        List<Card> cards = List.of(
                new UnderbridgeWarlock(),
                new UnderbridgeWarlock(),
                new UnderbridgeWarlock(),
                new UnderbridgeWarlock());
        harness.setLibrary(player1, cards);
    }

    private void triggerEndStep() {
        harness.forceActivePlayer(player1);
        harness.passUntil(player1, TurnStep.CLEANUP);
    }
}
