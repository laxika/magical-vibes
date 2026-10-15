package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EverlastingTorment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfShards.class, EverlastingTorment.class})
class WallOfShardsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep gives the opponent life and keeps the Wall")
    void payingCumulativeUpkeepGivesOpponentLife() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.assertLife(player2, 21);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
    }

    @Test
    @DisplayName("Cumulative upkeep gives two life on the second upkeep")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during the Wall's controller's upkeep")
    void cumulativeUpkeepTriggersOnlyDuringControllersUpkeep() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wall.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the Wall")
    void decliningCumulativeUpkeepSacrificesWall() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Wall of Shards");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Wall is sacrificed when the opponent cannot gain life")
    void cannotPayWhenOpponentCannotGainLife() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        harness.addToBattlefield(player2, new EverlastingTorment());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Wall of Shards");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Preexisting age counters are included in the cumulative upkeep cost")
    void cumulativeUpkeepIncludesPreexistingAgeCounters() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        wall.setCounterCount(CounterType.AGE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
        harness.assertOnBattlefield(player1, "Wall of Shards");
    }

    @Test
    @DisplayName("A Wall controlled by the second player gives life to the first player")
    void secondPlayersWallGivesLifeToFirstPlayer() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfShards());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wall of Shards");
    }

    @Test
    @DisplayName("Declining a larger upkeep cost gives no partial life payment")
    void decliningLargerUpkeepGivesNoLife() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfShards());
        wall.setCounterCount(CounterType.AGE, 3);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Wall of Shards");
        harness.assertInGraveyard(player1, "Wall of Shards");
        harness.assertLife(player2, 20);
    }
}
