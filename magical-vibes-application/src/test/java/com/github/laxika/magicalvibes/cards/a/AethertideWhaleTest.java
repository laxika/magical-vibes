package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethertideWhale.class})
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

    @Test
    void paysEnergyImmediatelyAndCanActivateWhileTappedAndSummoningSick() {
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new AethertideWhale());
        whale.tap();
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Aethertide Whale");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Aethertide Whale");
        harness.assertNotOnBattlefield(player1, "Aethertide Whale");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void returnsToOwnersHandUsingControllersEnergy() {
        AethertideWhale card = new AethertideWhale();
        card.setOwnerId(player2.getId());
        Permanent whale = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(whale.getId(), player2.getId());
        gd.playerEnergyCounters.put(player1.getId(), 4);
        gd.playerEnergyCounters.put(player2.getId(), 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aethertide Whale");
        harness.assertInHand(player2, "Aethertide Whale");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(7);
    }

    @Test
    void entryTriggerStillAddsSixEnergyAfterWhaleReturnsToHand() {
        gd.playerEnergyCounters.put(player1.getId(), 5);
        harness.enterBattlefieldAndReturn(player1, new AethertideWhale());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aethertide Whale");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);
    }

    @Test
    void multipleActivationsPaySeparatelyButReturnWhaleOnlyOnce() {
        harness.addToBattlefield(player1, new AethertideWhale());
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aethertide Whale");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Aethertide Whale");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }
}
