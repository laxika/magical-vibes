package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterWurm.class, Island.class})
class WaterWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Has base 1/1 while no opponent controls an Island")
    void baseStatsWithoutOpponentIsland() {
        harness.addToBattlefield(player1, new WaterWurm());

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Gets +0/+1 while an opponent controls an Island")
    void getsToughnessBoostFromOpponentIsland() {
        harness.addToBattlefield(player1, new WaterWurm());
        harness.addToBattlefield(player2, new Island());

        assertStats(1, 2);
    }

    @Test
    @DisplayName("The controller's own Island does not grant the boost")
    void ownIslandDoesNotGrantBoost() {
        harness.addToBattlefield(player1, new WaterWurm());
        harness.addToBattlefield(player1, new Island());

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Loses the boost when the opponent's Island leaves the battlefield")
    void losesBoostWhenOpponentIslandLeaves() {
        harness.addToBattlefield(player1, new WaterWurm());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertStats(1, 2);

        gd.playerBattlefields.get(player2.getId()).remove(island);

        assertStats(1, 1);
    }

    private void assertStats(int power, int toughness) {
        Permanent wurm = findPermanent(player1, "Water Wurm");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(toughness);
    }
}
