package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DaggerbackBasilisk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerishTheThought.class, DaggerbackBasilisk.class, Forest.class})
class PerishTheThoughtTest extends BaseCardTest {

    private void castPerishTheThought() {
        harness.setHand(player1, List.of(new PerishTheThought()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Chooses a card from the opponent's revealed hand and shuffles it into their library")
    void choosesCardAndShufflesItIntoLibrary() {
        Card chosen = new DaggerbackBasilisk();
        Card remaining = new DaggerbackBasilisk();
        Card libraryCard = new DaggerbackBasilisk();
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
        Card libraryCard = new DaggerbackBasilisk();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));

        castPerishTheThought();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("shuffles") && log.contains("into their library"));
    }

    @Test
    @DisplayName("Can choose a land and shuffle it into an empty library")
    void choosesLandIntoEmptyLibrary() {
        Card remaining = new DaggerbackBasilisk();
        Card chosen = new Forest();
        harness.setHand(player2, List.of(remaining, chosen));
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of());

        castPerishTheThought();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The caster must choose the opponent's only card")
    void choiceIsMandatoryAndBelongsToCaster() {
        Card chosen = new DaggerbackBasilisk();
        harness.setHand(player2, List.of(chosen));
        harness.setLibrary(player2, List.of());

        castPerishTheThought();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(chosen);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
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
