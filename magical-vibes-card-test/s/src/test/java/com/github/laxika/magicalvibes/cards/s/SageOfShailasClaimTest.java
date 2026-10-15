package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfShailasClaim.class})
class SageOfShailasClaimTest extends BaseCardTest {

    @Test
    void entersAndGivesItsControllerThreeEnergyCounters() {
        harness.castFromHand(player1, new SageOfShailasClaim(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void enteringWithoutBeingCastGivesEnergyToItsController() {
        harness.enterBattlefieldAndReturn(player2, new SageOfShailasClaim());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void eachEntryAddsThreeToExistingEnergy() {
        gd.setPlayerEnergyCounters(player1.getId(), 4);
        gd.setPlayerEnergyCounters(player2.getId(), 2);

        harness.enterBattlefieldAndReturn(player1, new SageOfShailasClaim());
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);

        harness.enterBattlefieldAndReturn(player1, new SageOfShailasClaim());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(2);
    }
}
