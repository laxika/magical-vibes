package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnOfANewAge.class, GrizzlyBears.class})
class DawnOfANewAgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with hope counters equal to the creatures its controller controls")
    void entersWithHopeCountersForControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent dawn = harness.enterBattlefieldAndReturn(player1, new DawnOfANewAge());

        assertThat(dawn.getCounterCount(CounterType.HOPE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws a card and removes a hope counter at its controller's end step")
    void drawsAndRemovesHopeCounterAtEndStep() {
        Permanent dawn = harness.enterBattlefieldAndReturn(player1, new DawnOfANewAge());
        dawn.setCounterCount(CounterType.HOPE, 2);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(dawn.getCounterCount(CounterType.HOPE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("On its last hope counter, draws, sacrifices itself, and gains 4 life")
    void lastHopeCounterSacrificesAndGainsLife() {
        Permanent dawn = harness.enterBattlefieldAndReturn(player1, new DawnOfANewAge());
        dawn.setCounterCount(CounterType.HOPE, 1);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToEndStep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Dawn of a New Age");
        harness.assertInGraveyard(player1, "Dawn of a New Age");
        assertThat(dawn.getCounterCount(CounterType.HOPE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent dawn = harness.enterBattlefieldAndReturn(player1, new DawnOfANewAge());
        dawn.setCounterCount(CounterType.HOPE, 1);

        advanceToEndStep(player2);

        assertThat(dawn.getCounterCount(CounterType.HOPE)).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
