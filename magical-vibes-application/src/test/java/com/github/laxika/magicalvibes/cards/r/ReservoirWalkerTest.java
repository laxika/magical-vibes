package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReservoirWalker.class})
class ReservoirWalkerTest extends BaseCardTest {

    @Test
    void entersWithLifeGainAndEnergyCounters() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new ReservoirWalker(), "{5}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void rewardsWaitForTheEnterTriggerToResolve() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.castFromHand(player1, new ReservoirWalker(), "{5}");

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void addsEnergyToExistingCountersOnlyForItsController() {
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 4);
        int controllerLifeBefore = gd.getLife(player2.getId());
        int opponentLifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player2, new ReservoirWalker(), "{5}");
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(controllerLifeBefore + 3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }
}
