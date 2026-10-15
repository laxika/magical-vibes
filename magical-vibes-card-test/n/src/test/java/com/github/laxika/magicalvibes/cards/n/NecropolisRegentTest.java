package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecropolisRegent.class, GrizzlyBears.class, SerraAngel.class})
class NecropolisRegentTest extends BaseCardTest {

    private Permanent addReady(Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, card);
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Another creature gets +1/+1 counters equal to the combat damage it dealt")
    void allyGetsCountersEqualToDamage() {
        addReady(new NecropolisRegent());
        Permanent bears = addReady(new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities(); // resolve the Regent's trigger

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Necropolis Regent triggers for itself, gaining counters equal to its own damage")
    void triggersForItself() {
        Permanent regent = addReady(new NecropolisRegent());
        regent.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        harness.passBothPriorities(); // resolve the Regent's trigger

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("No trigger when the attacker is blocked and deals no damage to the player")
    void noTriggerWhenBlocked() {
        addReady(new NecropolisRegent());
        Permanent bears = addReady(new GrizzlyBears());
        bears.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Regent triggers separately for every creature dealing combat damage")
    void multipleRegentsAndDealers() {
        Permanent first = addReady(new NecropolisRegent());
        Permanent second = addReady(new NecropolisRegent());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLife(player2, 30);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
    }

    @Test
    @DisplayName("The damage amount remains fixed if the dealer's power changes before resolution")
    void countersUseDamageRatherThanCurrentPower() {
        Permanent regent = addReady(new NecropolisRegent());
        regent.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        regent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("A Regent does not trigger for an opposing creature")
    void opponentCreatureDoesNotTriggerRegent() {
        Permanent regent = addReady(new NecropolisRegent());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new NecropolisRegent());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
