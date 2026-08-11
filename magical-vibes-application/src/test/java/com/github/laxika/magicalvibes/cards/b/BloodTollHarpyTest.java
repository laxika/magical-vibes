package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Blood-Toll Harpy")
class BloodTollHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB causes each player to lose 1 life")
    void etbCausesEachPlayerToLoseLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.setHand(player1, List.of(new BloodTollHarpy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Blood-Toll Harpy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB life loss applies to both players at arbitrary life totals")
    void etbLifeLossAppliesAtArbitraryLifeTotals() {
        harness.setLife(player1, 1);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new BloodTollHarpy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(1);
    }
}
