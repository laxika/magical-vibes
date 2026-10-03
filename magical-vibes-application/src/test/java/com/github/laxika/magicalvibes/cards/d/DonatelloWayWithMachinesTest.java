package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HardWonJitte;
import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloWayWithMachines.class, HardWonJitte.class, MouserMarkIII.class})
class DonatelloWayWithMachinesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on Donatello when an artifact enters under its controller's control")
    void addsCounterForControlledArtifactEntry() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        harness.castFromHand(player1, new HardWonJitte(), "{1}{R}");
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers once for each controlled artifact that enters")
    void triggersForEachArtifactEntry() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        harness.castFromHand(player1, new HardWonJitte(), "{1}{R}");
        resolveAllTriggers();
        harness.setHand(player1, List.of(new MouserMarkIII()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger Donatello")
    void opponentArtifactDoesNotTrigger() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        harness.enterBattlefieldAndReturn(player2, new HardWonJitte());
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A nonartifact creature entering does not trigger Donatello")
    void nonartifactEntryDoesNotTrigger() {
        harness.enterBattlefieldAndReturn(player1, new DonatelloWayWithMachines());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter is added on resolution even if the entering artifact has left")
    void artifactNeedNotRemainOnBattlefield() {
        Permanent donatello = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());
        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new HardWonJitte());

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        resolveAllTriggers();

        assertThat(donatello.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A pending trigger does not put a counter on Donatello after it leaves and returns")
    void returningDonatelloIsANewObject() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new DonatelloWayWithMachines());
        harness.enterBattlefieldAndReturn(player1, new HardWonJitte());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.enterBattlefieldAndReturn(player1, original.getCard());
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
