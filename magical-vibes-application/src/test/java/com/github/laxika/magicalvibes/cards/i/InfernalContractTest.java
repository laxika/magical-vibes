package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalContractTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards and loses half life rounded up (even life total)")
    void drawsFourAndLosesHalfEven() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new InfernalContract()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        // Started with one card, cast it, drew four.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        harness.assertInGraveyard(player1, "Infernal Contract");
    }

    @Test
    @DisplayName("Odd life total rounds the loss up")
    void oddLifeRoundsUp() {
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new InfernalContract()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        // Half of 15 rounded up is 8, leaving 7.
        assertThat(gd.getLife(player1.getId())).isEqualTo(7);
    }
}
