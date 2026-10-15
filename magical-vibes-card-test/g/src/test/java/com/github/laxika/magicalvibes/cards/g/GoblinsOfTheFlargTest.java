package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinsOfTheFlarg.class, DwarvenTrader.class, Mountain.class, LightningBolt.class})
class GoblinsOfTheFlargTest extends BaseCardTest {

    @Test
    @DisplayName("Survives when its controller controls no Dwarves")
    void survivesWithoutDwarf() {
        castGoblins();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Is sacrificed when its controller controls a Dwarf")
    void sacrificedWithDwarf() {
        harness.addToBattlefield(player1, new DwarvenTrader());
        castGoblins();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblins of the Flarg");
        harness.assertInGraveyard(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Is sacrificed when a Dwarf later becomes controlled")
    void sacrificedWhenDwarfEnters() {
        castGoblins();
        harness.addToBattlefield(player1, new DwarvenTrader());
        harness.runStateBasedActions();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblins of the Flarg");
        harness.assertInGraveyard(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("An opponent's Dwarf does not cause the sacrifice")
    void opponentDwarfDoesNotCount() {
        harness.addToBattlefield(player2, new DwarvenTrader());
        castGoblins();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Does not duplicate its sacrifice trigger while that trigger is on the stack")
    void doesNotRetriggerWhilePending() {
        harness.addToBattlefield(player1, new DwarvenTrader());
        harness.addToBattlefield(player1, new DwarvenTrader());
        castGoblins();

        assertThat(gd.stack).hasSize(1);
        harness.runStateBasedActions();
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblins of the Flarg");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still sacrifices itself if the Dwarf dies in response to the trigger")
    void sacrificesAfterDwarfLeaves() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenTrader());
        castGoblins();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dwarf.getId());

        harness.assertInGraveyard(player1, "Dwarven Trader");
        harness.assertOnBattlefield(player1, "Goblins of the Flarg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblins of the Flarg");
        harness.assertInGraveyard(player1, "Goblins of the Flarg");
    }

    @Test
    @DisplayName("Its controller's Mountain does not prevent an opponent from blocking")
    void controllersMountainDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new GoblinsOfTheFlarg());
        harness.addToBattlefield(player1, new Mountain());
        Permanent blocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountainwalk prevents blocking when the defending player controls a Mountain")
    void mountainwalkPreventsBlockingWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent attacker = addCreatureReady(player1, new GoblinsOfTheFlarg());
        Permanent blocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountainwalk allows blocking when the defending player controls no Mountain")
    void mountainwalkAllowsBlockingWithoutMountain() {
        Permanent attacker = addCreatureReady(player1, new GoblinsOfTheFlarg());
        Permanent blocker = addCreatureReady(player2, new DwarvenTrader());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castGoblins() {
        harness.setHand(player1, List.of(new GoblinsOfTheFlarg()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
