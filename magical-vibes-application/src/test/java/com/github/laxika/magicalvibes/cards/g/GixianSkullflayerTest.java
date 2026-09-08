package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GixianSkullflayerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself with three creature cards in its controller's graveyard")
    void upkeepAddsCounterWithThreeCreatureCards() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        resolveUpkeepTrigger();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count noncreature or opponent graveyard cards")
    void upkeepDoesNotAddCounterWithoutThreeOwnCreatureCards() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Opt()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveUpkeepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
