package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StreamOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains X life")
    void targetPlayerGainsXLife() {
        harness.setHand(player1, List.of(new StreamOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new StreamOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 3, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("X=0 gains no life")
    void xZeroGainsNoLife() {
        harness.setHand(player1, List.of(new StreamOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
