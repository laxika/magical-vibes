package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AethertideWhaleTest extends BaseCardTest {

    @Test
    void entersWithSixEnergyCounters() {
        harness.setHand(player1, java.util.List.of(new AethertideWhale()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(6);
    }

    @Test
    void paysEnergyToReturnItselfToItsOwnersHand() {
        harness.addToBattlefield(player1, new AethertideWhale());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertNotOnBattlefield(player1, "Aethertide Whale");
        harness.assertInHand(player1, "Aethertide Whale");
    }

    @Test
    void cannotActivateWithoutFourEnergyCounters() {
        harness.addToBattlefield(player1, new AethertideWhale());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four energy counters");
    }
}
