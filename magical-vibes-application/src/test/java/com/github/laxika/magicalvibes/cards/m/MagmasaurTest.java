package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagmasaurTest extends BaseCardTest {

    @Test
    @DisplayName("Magmasaur enters the battlefield with five +1/+1 counters")
    void entersWithFiveCounters() {
        harness.setHand(player1, List.of(new Magmasaur()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent magmasaur = findPermanent(player1, "Magmasaur");
        assertThat(magmasaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(magmasaur.getEffectivePower()).isEqualTo(5);
        assertThat(magmasaur.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter at upkeep keeps Magmasaur alive and deals no damage")
    void removingCounterKeepsItAlive() {
        Permanent magmasaur = addMagmasaur(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Magmasaur");
        assertThat(magmasaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining sacrifices Magmasaur and blasts each creature without flying and each player")
    void decliningSacrificesAndBlasts() {
        addMagmasaur(player1, 5);
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent airElemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmasaur");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(airElemental.getMarkedDamage()).isZero();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The damage is only as large as the counters left on Magmasaur")
    void damageScalesWithRemainingCounters() {
        addMagmasaur(player1, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmasaur");
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("With no +1/+1 counters left Magmasaur is sacrificed without a prompt and deals no damage")
    void noCountersSacrificesWithoutPrompt() {
        addMagmasaur(player1, 0);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Magmasaur");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addMagmasaur(Player player, int counters) {
        Permanent magmasaur = harness.addToBattlefieldAndReturn(player, new Magmasaur());
        magmasaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return magmasaur;
    }
}
