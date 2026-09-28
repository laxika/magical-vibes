package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFifthDoctor.class, GrizzlyBears.class})
class TheFifthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, peaceful creatures get a counter and untap")
    void rewardsCreaturesThatStayedOutOfCombat() {
        Permanent doctor = harness.enterBattlefieldAndReturn(player1, new TheFifthDoctor());
        Permanent peaceful = addCreatureReady(player1, new GrizzlyBears());
        peaceful.tap();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.tap();
        attacker.setAttackedThisTurn(true);
        Permanent entered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        entered.tap();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(entered.getCard());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(peaceful.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(peaceful.isTapped()).isFalse();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entered.isTapped()).isTrue();
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
