package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManamorphoseTest extends BaseCardTest {

    @Test
    @DisplayName("Adds two mana in any combination of colors (two different colors), then draws")
    void addsTwoDifferentColorsAndDraws() {
        harness.setHand(player1, List.of(new Manamorphose()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        // Pay {1}{R/G}: colorless for the generic, green for the hybrid pip.
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // First any-color choice pauses resolution.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.clearMessages();
        harness.handleListChoice(player1, "RED");
        assertThat(harness.getConn1().getSentMessages()).hasSize(2);
        assertThat(harness.getConn1().getSentMessages().get(0)).contains("\"type\":\"GAME_STATE\"");
        assertThat(harness.getConn1().getSentMessages().get(1)).contains("\"type\":\"INTERACTION_PROMPT\"");
        // Second any-color choice is independent — a different color is allowed.
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        // Draw a card resolves after the mana is added.
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog)
                .extracting(entry -> entry.plainText())
                .contains("Alice adds one red mana.", "Alice adds one white mana.");
    }

    @Test
    @DisplayName("Both mana may be the same color")
    void addsTwoOfSameColor() {
        harness.setHand(player1, List.of(new Manamorphose()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
