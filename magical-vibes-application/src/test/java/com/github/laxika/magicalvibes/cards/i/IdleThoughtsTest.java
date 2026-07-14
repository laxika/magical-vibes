package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdleThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws nothing when the controller already has cards in hand")
    void noDrawWhenHandNotEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Condition not met on resolution → hand unchanged (still the single starting card).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
