package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WuAdmiral.class, Island.class, Forest.class})
class WuAdmiralTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/3 when no opponent controls an Island")
    void baseStatsWithoutOpponentIsland() {
        harness.addToBattlefield(player1, new WuAdmiral());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Gets +1/+1 becoming 4/4 when an opponent controls an Island")
    void boostedWhenOpponentControlsIsland() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player2, new Island());

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Controller's own Island does NOT grant the boost")
    void ownIslandDoesNotBoost() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player1, new Island());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Non-Island opponent land does NOT grant the boost")
    void opponentNonIslandDoesNotBoost() {
        harness.addToBattlefield(player1, new WuAdmiral());
        harness.addToBattlefield(player2, new Forest());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Loses boost when opponent's Island leaves the battlefield")
    void losesBoostWhenIslandLeaves() {
        harness.addToBattlefield(player1, new WuAdmiral());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertStats(4, 4);

        gd.playerBattlefields.get(player2.getId()).remove(island);

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Remains boosted while at least one of multiple opponent Islands remains")
    void remainsBoostedWhileOpponentControlsAnotherIsland() {
        harness.addToBattlefield(player1, new WuAdmiral());
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        assertStats(4, 4);

        gd.playerBattlefields.get(player2.getId()).remove(firstIsland);

        assertStats(4, 4);
    }

    private void assertStats(int power, int toughness) {
        Permanent admiral = findPermanent(player1, "Wu Admiral");
        assertThat(gqs.getEffectivePower(gd, admiral)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, admiral)).isEqualTo(toughness);
    }
}
