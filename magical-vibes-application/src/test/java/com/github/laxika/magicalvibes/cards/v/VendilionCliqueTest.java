package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VendilionCliqueTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger prompts caster to choose a nonland card from target's hand")
    void promptsForNonlandChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        resolveVendilionCliqueTargeting(player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.optional()).isTrue();
        // Only index 0 (Grizzly Bears) is a valid choice; the Forest (land) is excluded.
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a nonland card bottoms it and the target draws a card")
    void choosingBottomsCardAndDraws() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island())));
        resolveVendilionCliqueTargeting(player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Grizzly Bears was put on the bottom of player2's library.
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Grizzly Bears");
        // player2 drew the Island that was on top.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    @Test
    @DisplayName("Declining the optional choice leaves the hand and library untouched, no draw")
    void decliningDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island())));
        resolveVendilionCliqueTargeting(player2.getId());

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Card stays in hand; nothing was drawn or bottomed.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    @Test
    @DisplayName("Hand with only lands produces no prompt and does nothing")
    void onlyLandsNoPrompt() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island())));
        resolveVendilionCliqueTargeting(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Both lands remain; no draw happened.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no nonland"));
    }

    @Test
    @DisplayName("Empty hand produces no prompt and logs empty")
    void emptyHandLogged() {
        harness.setHand(player2, new ArrayList<>());
        resolveVendilionCliqueTargeting(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Selecting a land index is rejected")
    void selectingLandRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        resolveVendilionCliqueTargeting(player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new VendilionClique(), new Peek())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island())));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        // Peek (index 0 after Vendilion Clique left hand) is the only nonland card.
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Peek");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Island");
    }

    private void resolveVendilionCliqueTargeting(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new VendilionClique())));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }
}
