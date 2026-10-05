package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavabrinkFloodgates.class, CanyonWildcat.class, WindDrake.class})
class LavabrinkFloodgatesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds two red mana")
    void tappingAddsTwoRedMana() {
        Permanent floodgates = harness.addToBattlefieldAndReturn(player1, new LavabrinkFloodgates());
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, null, null);

        assertThat(floodgates.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 2);
    }

    @Test
    @DisplayName("The active player may add a doom counter, triggering the blast at three")
    void activePlayerMayAddCounterAndTriggerBlast() {
        Permanent floodgates = addFloodgates(player1, 2);
        harness.addToBattlefield(player1, new CanyonWildcat());
        harness.addToBattlefield(player2, new CanyonWildcat());
        harness.addToBattlefield(player2, new WindDrake());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Put a doom counter on Lavabrink Floodgates");

        harness.assertNotOnBattlefield(player1, "Lavabrink Floodgates");
        harness.assertOnBattlefield(player1, "Canyon Wildcat");
        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canyon Wildcat");
        harness.assertNotOnBattlefield(player2, "Canyon Wildcat");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
        assertThat(floodgates.getCounterCount(CounterType.DOOM)).isEqualTo(3);
    }

    @Test
    @DisplayName("The active player may remove a doom counter")
    void activePlayerMayRemoveCounter() {
        Permanent floodgates = addFloodgates(player1, 3);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Remove a doom counter from Lavabrink Floodgates");

        harness.assertOnBattlefield(player1, "Lavabrink Floodgates");
        assertThat(floodgates.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining both counter actions still causes the three-counter blast")
    void decliningBothCounterActionsTriggersBlast() {
        Permanent floodgates = addFloodgates(player1, 3);
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Done");

        harness.assertNotOnBattlefield(player1, "Lavabrink Floodgates");
        assertThat(wildcat.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(wildcat.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Adding a counter below the threshold leaves Floodgates on the battlefield")
    void addingCounterBelowThresholdDoesNotBlast() {
        Permanent floodgates = addFloodgates(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a doom counter on Lavabrink Floodgates");

        harness.assertOnBattlefield(player1, "Lavabrink Floodgates");
        assertThat(floodgates.getCounterCount(CounterType.DOOM)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing a counter from an artifact with none does nothing")
    void removingCounterAtZeroDoesNothing() {
        Permanent floodgates = addFloodgates(player1, 0);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Remove a doom counter from Lavabrink Floodgates");

        harness.assertOnBattlefield(player1, "Lavabrink Floodgates");
        assertThat(floodgates.getCounterCount(CounterType.DOOM)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining counter changes below the threshold does nothing")
    void decliningBelowThresholdDoesNothing() {
        Permanent floodgates = addFloodgates(player1, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Done");

        harness.assertOnBattlefield(player1, "Lavabrink Floodgates");
        assertThat(floodgates.getCounterCount(CounterType.DOOM)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing a counter from four still causes a blast")
    void removingCounterStillAtThresholdTriggersBlast() {
        Permanent floodgates = addFloodgates(player1, 4);
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Remove a doom counter from Lavabrink Floodgates");

        assertThat(floodgates.getCounterCount(CounterType.DOOM)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Lavabrink Floodgates");
        assertThat(wildcat.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(wildcat.getMarkedDamage()).isEqualTo(6);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Changing control before resolution prevents the original controller from sacrificing Floodgates")
    void failedSacrificeDoesNotDealDamage() {
        Permanent floodgates = addFloodgates(player1, 3);
        Permanent wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(floodgates);
        gd.playerBattlefields.get(player2.getId()).add(floodgates);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Done");

        harness.assertOnBattlefield(player2, "Lavabrink Floodgates");
        harness.assertOnBattlefield(player2, "Canyon Wildcat");
        assertThat(wildcat.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFloodgates(Player player, int doomCounters) {
        Permanent floodgates = harness.addToBattlefieldAndReturn(player, new LavabrinkFloodgates());
        floodgates.setCounterCount(CounterType.DOOM, doomCounters);
        return floodgates;
    }
}
