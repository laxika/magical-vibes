package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestsOfHonor.class, Forest.class, GrizzlyBears.class})
class PestsOfHonorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when two nonland permanents entered this turn")
    void putsCounterAfterTwoNonlandPermanentsEnter() {
        Permanent pests = harness.enterBattlefieldAndReturn(player1, new PestsOfHonor());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        resolveAllTriggers();

        assertThat(pests.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without two nonland permanents")
    void doesNotTriggerWithoutTwoNonlandPermanents() {
        Permanent pests = harness.enterBattlefieldAndReturn(player1, new PestsOfHonor());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(pests.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent's nonland entries do not enable the ability")
    void doesNotCountOpponentsPermanents() {
        Permanent pests = harness.enterBattlefieldAndReturn(player1, new PestsOfHonor());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(pests.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only triggers at the beginning of its controller's combat")
    void onlyTriggersDuringControllersCombat() {
        Permanent pests = harness.enterBattlefieldAndReturn(player1, new PestsOfHonor());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(pests.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
