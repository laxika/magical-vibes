package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AsmiraHolyAvengerTest extends BaseCardTest {

    private void advanceToEndStepAndResolve() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step (queues any trigger)
        harness.passBothPriorities(); // resolve the trigger
    }

    private Permanent addAsmira() {
        Permanent asmira = new Permanent(new AsmiraHolyAvenger());
        gd.playerBattlefields.get(player1.getId()).add(asmira);
        return asmira;
    }

    @Test
    @DisplayName("Gains a +1/+1 counter at end step for each of your creatures that died this turn")
    void gainsCountersForOwnDeaths() {
        Permanent asmira = addAsmira();
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 2, Integer::sum);

        harness.forceActivePlayer(player1);
        advanceToEndStepAndResolve();

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores creatures that died under an opponent's control")
    void ignoresOpponentDeaths() {
        Permanent asmira = addAsmira();
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 3, Integer::sum);

        harness.forceActivePlayer(player1);
        advanceToEndStepAndResolve();

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers at each end step, including an opponent's turn")
    void triggersOnOpponentEndStep() {
        Permanent asmira = addAsmira();
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player2);
        advanceToEndStepAndResolve();

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains no counter at end step when no creature of yours died")
    void noCounterWithoutDeaths() {
        Permanent asmira = addAsmira();

        harness.forceActivePlayer(player1);
        advanceToEndStepAndResolve();

        assertThat(asmira.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
