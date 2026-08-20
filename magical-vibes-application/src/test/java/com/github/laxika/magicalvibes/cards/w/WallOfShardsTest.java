package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EverlastingTorment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfShardsTest extends BaseCardTest {

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

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

        advanceToNextTurn(player2);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wall.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.assertLife(player2, 23);
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
}
