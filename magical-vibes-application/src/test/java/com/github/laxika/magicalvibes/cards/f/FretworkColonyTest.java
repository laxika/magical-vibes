package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FretworkColony.class})
class FretworkColonyTest extends BaseCardTest {

    @Test
    @DisplayName("At its controller's upkeep, Fretwork Colony gets a +1/+1 counter and its controller loses 1 life")
    void upkeepAddsCounterAndLosesLife() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new FretworkColony());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Fretwork Colony does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new FretworkColony());
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Fretwork Colony cannot be declared as a blocker")
    void cannotBlock() {
        addCreatureReady(player2, new FretworkColony());

        Permanent attacker = addCreatureReady(player1, new FretworkColony());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void countersAccumulateOverSuccessiveUpkeeps() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new FretworkColony());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void eachColonyTriggersIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FretworkColony());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FretworkColony());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void lifeLossStillResolvesAfterSourceLeavesBattlefield() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new FretworkColony());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(colony);
        gd.playerGraveyards.get(player1.getId()).add(colony.getCard());
        harness.passBothPriorities();

        assertThat(colony.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }
}
