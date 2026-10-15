package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedDragon.class})
class RedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 4 damage to each opponent")
    void etbDealsDamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new RedDragon(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage waits for the entry trigger to resolve")
    void damageWaitsForTriggerResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new RedDragon(), "{4}{R}{R}");

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entry without casting damages the entering creature controller's opponent")
    void enteringWithoutCastingDamagesControllersOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new RedDragon());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
