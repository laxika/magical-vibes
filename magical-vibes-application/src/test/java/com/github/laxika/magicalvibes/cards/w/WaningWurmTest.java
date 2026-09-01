package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed(WaningWurm.class)
class WaningWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two time counters")
    void entersWithTimeCounters() {
        harness.setHand(player1, List.of(new WaningWurm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
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
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent wurm = addCreatureReady(player1, new WaningWurm());
        wurm.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Waning Wurm");
        harness.assertInGraveyard(player1, "Waning Wurm");
    }
}
