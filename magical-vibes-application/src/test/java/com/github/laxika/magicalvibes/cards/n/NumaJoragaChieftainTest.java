package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NumaJoragaChieftain.class, LlanowarElves.class, GrizzlyBears.class})
class NumaJoragaChieftainTest extends BaseCardTest {

    @Test
    void paysTwiceAndDistributesCountersAmongTargetElves() {
        Permanent numa = harness.addToBattlefieldAndReturn(player1, new NumaJoragaChieftain());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        advanceToBeginningOfCombat(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        resolveAllTriggers();

        PendingInteraction.XValueChoice payment =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(payment).isNotNull();
        assertThat(payment.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 2);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(numa.getId(), elf.getId())
                .doesNotContain(nonElf.getId());

        harness.handlePermanentChosen(player1, numa.getId());
        harness.handlePermanentChosen(player1, elf.getId());
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();

        assertThat(numa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningPaymentDoesNothing() {
        Permanent numa = harness.addToBattlefieldAndReturn(player1, new NumaJoragaChieftain());
        advanceToBeginningOfCombat(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 0);

        assertThat(numa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
