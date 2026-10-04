package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenbeltRampager.class})
class GreenbeltRampagerTest extends BaseCardTest {

    @Test
    void paysTwoEnergyToStayOnTheBattlefield() {
        castRampager();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertOnBattlefield(player1, "Greenbelt Rampager");
    }

    @Test
    void returnsToHandAndGainsEnergyWhenItCannotPay() {
        castRampager();

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Greenbelt Rampager");
        harness.assertInHand(player1, "Greenbelt Rampager");
    }

    @Test
    void keepsRemainingEnergyAfterPayingTwo() {
        castRampager();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Greenbelt Rampager");
    }

    @Test
    void cannotPartiallyPayWithOneEnergy() {
        castRampager();
        gd.playerEnergyCounters.put(player1.getId(), 1);

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Greenbelt Rampager");
        harness.assertInHand(player1, "Greenbelt Rampager");
    }

    @Test
    void repeatedCastingBuildsEnergyThenPaysIt() {
        castRampager();
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInHand(player1, "Greenbelt Rampager");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertOnBattlefield(player1, "Greenbelt Rampager");
        harness.assertNotInHand(player1, "Greenbelt Rampager");
    }

    private void castRampager() {
        harness.setHand(player1, List.of(new GreenbeltRampager()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }
}
