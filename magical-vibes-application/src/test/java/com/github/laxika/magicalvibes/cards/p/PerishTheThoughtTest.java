package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerishTheThoughtTest extends BaseCardTest {

    private void castPerishTheThought() {
        harness.setHand(player1, List.of(new PerishTheThought()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Chooses a card from the opponent's revealed hand and shuffles it into their library")
    void choosesCardAndShufflesItIntoLibrary() {
        Card chosen = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(chosen, remaining)));
        harness.setLibrary(player2, List.of(libraryCard));

        castPerishTheThought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(chosen, libraryCard);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffles") && log.contains("into their library"));
    }

    @Test
    @DisplayName("Does not shuffle when the targeted opponent's hand is empty")
    void emptyHandDoesNotShuffle() {
        Card libraryCard = new GrizzlyBears();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));

        castPerishTheThought();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("shuffles") && log.contains("into their library"));
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PerishTheThought()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
