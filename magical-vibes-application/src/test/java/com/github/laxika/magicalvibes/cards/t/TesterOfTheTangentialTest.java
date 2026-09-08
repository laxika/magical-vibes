package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TesterOfTheTangentialTest extends BaseCardTest {

    @Test
    void paysXThenMovesCountersToAnotherCreature() {
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void choosingZeroDoesNotPayOrMoveCounters() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        harness.handleXValueChosen(player1, 0);

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
    }

    @Test
    void cannotChooseTheSourceAsAnotherCreature() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNull();
        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
