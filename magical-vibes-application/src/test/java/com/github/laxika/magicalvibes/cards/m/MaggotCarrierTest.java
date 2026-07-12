package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaggotCarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes each player lose 1 life")
    void etbMakesEachPlayerLose1Life() {
        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        castMaggotCarrier();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.stack).isEmpty();
    }

    private void castMaggotCarrier() {
        harness.setHand(player1, List.of(new MaggotCarrier()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
    }
}
