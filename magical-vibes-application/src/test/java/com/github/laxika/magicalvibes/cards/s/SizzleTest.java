package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SizzleTest extends BaseCardTest {

    @Test
    @DisplayName("Sizzle deals 3 damage to the opponent and none to its controller")
    void dealsThreeToOpponent() {
        castSizzle();
        harness.passBothPriorities(); // resolve the sorcery

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sizzle deals 3 damage with non-default life totals")
    void dealsThreeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        castSizzle();
        harness.passBothPriorities(); // resolve the sorcery

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private void castSizzle() {
        harness.setHand(player1, List.of(new Sizzle()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 0);
    }
}
