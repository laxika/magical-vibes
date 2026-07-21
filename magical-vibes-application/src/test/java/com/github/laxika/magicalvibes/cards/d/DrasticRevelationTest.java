package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrasticRevelationTest extends BaseCardTest {

    private void addCost() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Drastic Revelation puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new DrasticRevelation()));
        addCost();

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new DrasticRevelation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Discards existing hand, draws seven, then discards three at random")
    void discardsHandDrawsSevenThenDiscardsThree() {
        // Two extra cards in hand alongside the spell; a fresh 7-card library to draw from.
        harness.setHand(player1, List.of(new DrasticRevelation(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Discard hand (the 2 leftover cards), draw 7, discard 3 at random => hand of 4.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        // Library emptied by the 7-card draw.
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // Graveyard: 2 discarded hand + 3 random discards + Drastic Revelation itself = 6.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Drastic Revelation"));
        // Random discard prompts nothing.
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Exactly three "at random" discards logged.
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(3);
    }

    @Test
    @DisplayName("Empty starting hand still draws seven then discards three at random")
    void emptyHandStillDrawsAndDiscards() {
        harness.setHand(player1, List.of(new DrasticRevelation()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCost();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // No leftover hand to discard; draw 7, discard 3 => hand of 4.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        // Graveyard: 3 random discards + the spell = 4.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
