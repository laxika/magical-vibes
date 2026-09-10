package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.cards.c.Capsize;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Magmasaur.class, CanyonWildcat.class, WindDrake.class, Capsize.class})
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
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Magmasaur");
        assertThat(magmasaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(wildcat.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining sacrifices Magmasaur and blasts each creature without flying and each player")
    void decliningSacrificesAndBlasts() {
        addMagmasaur(player1, 5);
        Permanent ownWildcat = harness.addToBattlefieldAndReturn(player1, new CanyonWildcat());
        harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());
        Permanent windDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmasaur");
        harness.assertNotOnBattlefield(player2, "Canyon Wildcat");
        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(ownWildcat.getMarkedDamage()).isEqualTo(5);
        assertThat(windDrake.getMarkedDamage()).isZero();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Leaving before resolution uses Magmasaur's last known counters for its damage")
    void damageUsesLastKnownCountersAfterLeavingBattlefield() {
        Permanent magmasaur = addMagmasaur(player1, 5);
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());
        Permanent windDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Capsize()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, magmasaur.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Magmasaur");
        assertThat(wildcat.getMarkedDamage()).isEqualTo(5);
        assertThat(windDrake.getMarkedDamage()).isZero();
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("The damage is only as large as the counters left on Magmasaur")
    void damageScalesWithRemainingCounters() {
        addMagmasaur(player1, 2);
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmasaur");
        assertThat(wildcat.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("With no +1/+1 counters left Magmasaur is sacrificed without a prompt and deals no damage")
    void noCountersSacrificesWithoutPrompt() {
        addMagmasaur(player1, 0);
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Magmasaur");
        assertThat(wildcat.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addMagmasaur(Player player, int counters) {
        Permanent magmasaur = harness.addToBattlefieldAndReturn(player, new Magmasaur());
        magmasaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return magmasaur;
    }
}
