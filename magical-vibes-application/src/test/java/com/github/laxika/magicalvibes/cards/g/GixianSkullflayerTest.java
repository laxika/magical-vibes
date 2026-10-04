package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GixianSkullflayer.class, Disfigure.class})
class GixianSkullflayerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself with three creature cards in its controller's graveyard")
    void upkeepAddsCounterWithThreeCreatureCards() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new GixianSkullflayer()));

        resolveUpkeepTrigger();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count noncreature or opponent graveyard cards")
    void upkeepDoesNotAddCounterWithoutThreeOwnCreatureCards() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new Disfigure()));
        harness.setGraveyard(player2, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new GixianSkullflayer()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new GixianSkullflayer()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void rechecksCreatureThresholdWhenTriggerResolves() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new GixianSkullflayer()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer()));
        harness.passBothPriorities();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reachingThresholdAfterUpkeepBeginsDoesNotTrigger() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new GixianSkullflayer()));
        harness.passBothPriorities();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void moreThanThreeCreatureCardsStillAddsOnlyOneCounter() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(),
                new GixianSkullflayer(), new GixianSkullflayer()));

        resolveUpkeepTrigger();

        assertThat(skullflayer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggerDoesNotPutCounterOnAnotherCopyAfterSourceLeaves() {
        Permanent skullflayer = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.setGraveyard(player1, List.of(new GixianSkullflayer(), new GixianSkullflayer(), new GixianSkullflayer()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(skullflayer);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new GixianSkullflayer());
        harness.passBothPriorities();

        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveUpkeepTrigger() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
