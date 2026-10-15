package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.t.Timebender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaningWurm.class, Timebender.class})
class WaningWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two time counters")
    void entersWithTimeCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WaningWurm(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Waning Wurm").getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent wurm = addCreatureReady(player1, new WaningWurm());
        wurm.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(wurm.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Does not remove a time counter during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        Permanent wurm = addCreatureReady(player1, new WaningWurm());
        wurm.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(wurm.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent wurm = addCreatureReady(player1, new WaningWurm());
        wurm.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Waning Wurm");
        harness.assertInGraveyard(player1, "Waning Wurm");
    }

    @Test
    void noTimeCountersMeansNoUpkeepTrigger() {
        Permanent wurm = addCreatureReady(player1, new WaningWurm());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
    }

    @Test
    void lastCounterRemovalQueuesSeparateSacrificeTrigger() {
        Permanent wurm = addCreatureReady(player1, new WaningWurm());
        wurm.setCounterCount(CounterType.TIME, 1);
        advanceToUpkeep(player1);

        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Waning Wurm");
        harness.assertInGraveyard(player1, "Waning Wurm");
    }

    @Test
    void removingLastCountersWithTimebenderCausesSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent wurm = harness.enterBattlefieldAndReturn(player1, new WaningWurm());
        harness.setHand(player1, List.of(new Timebender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        Permanent timebender = findPermanent(player1, "Timebender");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(timebender));
        harness.handleListChoice(player1, "Remove two time counters");
        harness.handlePermanentChosen(player1, wurm.getId());

        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Waning Wurm");
        harness.assertInGraveyard(player1, "Waning Wurm");
    }
}
