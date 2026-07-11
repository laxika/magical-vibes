package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShuSoldierFarmersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger causes controller to gain 4 life")
    void etbGainsLife() {
        castShuSoldierFarmers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB gain life works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);

        castShuSoldierFarmers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    private void castShuSoldierFarmers() {
        harness.setHand(player1, List.of(new ShuSoldierFarmers()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }
}
