package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IdleThoughts.class)
class IdleThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the controller has no cards in hand")
    void drawsWhenHandEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws nothing when the controller already has cards in hand")
    void noDrawWhenHandNotEmpty() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of(new IdleThoughts()));
        harness.setLibrary(player1, List.of(new IdleThoughts(), new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Condition not met on resolution → hand unchanged (still the single starting card).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Checks the empty-hand condition when the ability resolves")
    void noDrawWhenHandBecomesNonEmptyBeforeResolution() {
        harness.addToBattlefield(player1, new IdleThoughts());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IdleThoughts()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new IdleThoughts()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
