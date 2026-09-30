package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderbridgeWarlock.class, GrizzlyBears.class})
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

    private void addWarlock() {
        harness.enterBattlefieldAndReturn(player1, new UnderbridgeWarlock());
        harness.passBothPriorities();
    }

    private void setLibraryWithFourCards() {
        List<Card> cards = List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears());
        harness.setLibrary(player1, cards);
    }

    private void triggerEndStep() {
        harness.forceActivePlayer(player1);
        harness.passUntil(player1, TurnStep.CLEANUP);
    }
}
