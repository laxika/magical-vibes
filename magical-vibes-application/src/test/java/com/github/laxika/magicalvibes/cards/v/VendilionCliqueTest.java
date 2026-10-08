package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.r.RusticClachan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VendilionClique.class, ElvishWarrior.class, RusticClachan.class})
class VendilionCliqueTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger prompts caster to choose a nonland card from target's hand")
    void promptsForNonlandChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new ElvishWarrior(), new RusticClachan())));
        resolveVendilionCliqueTargeting(player2.getId());

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.optional()).isTrue();
        // Only index 0 (Elvish Warrior) is a valid choice; Rustic Clachan (land) is excluded.
        assertThat(choice.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a nonland card bottoms it and the target draws a card")
    void choosingBottomsCardAndDraws() {
        harness.setHand(player2, new ArrayList<>(List.of(new ElvishWarrior())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new RusticClachan())));
        resolveVendilionCliqueTargeting(player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Elvish Warrior was put on the bottom of player2's library.
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getName()).isEqualTo("Elvish Warrior");
        // player2 drew the Rustic Clachan that was on top.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Rustic Clachan");
    }

    @Test
    @DisplayName("Declining the optional choice leaves the hand and library untouched, no draw")
    void decliningDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new ElvishWarrior())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new RusticClachan())));
        resolveVendilionCliqueTargeting(player2.getId());

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Card stays in hand; nothing was drawn or bottomed.
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Elvish Warrior");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Rustic Clachan");
    }

    @Test
    @DisplayName("Hand with only lands produces no prompt and does nothing")
    void onlyLandsNoPrompt() {
        harness.setHand(player2, new ArrayList<>(List.of(new RusticClachan(), new RusticClachan())));
        harness.setLibrary(player2, new ArrayList<>(List.of(new RusticClachan())));
        resolveVendilionCliqueTargeting(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Both lands remain; no draw happened.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gameLogContains("no nonland")).isTrue();
    }

    @Test
    @DisplayName("Empty hand produces no prompt and logs empty")
    void emptyHandLogged() {
        harness.setHand(player2, new ArrayList<>());
        resolveVendilionCliqueTargeting(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("empty")).isTrue();
    }

    @Test
    @DisplayName("Selecting a land index is rejected")
    void selectingLandRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new ElvishWarrior(), new RusticClachan())));
        resolveVendilionCliqueTargeting(player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new VendilionClique(), new ElvishWarrior())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new RusticClachan())));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        // Elvish Warrior (index 0 after Vendilion Clique left hand) is the only nonland card.
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getName()).isEqualTo("Elvish Warrior");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Rustic Clachan");
    }

    @Test
    @DisplayName("Looking at a hand does not publish its cards in the public log")
    void handContentsRemainPrivate() {
        harness.setHand(player2, List.of(new ElvishWarrior(), new RusticClachan()));
        resolveVendilionCliqueTargeting(player2.getId());

        assertThat(gameLogContains("Elvish Warrior")).isFalse();
        assertThat(gameLogContains("Rustic Clachan")).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gameLogContains("Elvish Warrior")).isTrue();
        assertThat(gameLogContains("Rustic Clachan")).isFalse();
    }

    @Test
    @DisplayName("An all-land hand remains private even when there is no choice prompt")
    void allLandHandContentsRemainPrivate() {
        harness.setHand(player2, List.of(new RusticClachan()));
        resolveVendilionCliqueTargeting(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Rustic Clachan")).isFalse();
    }

    @Test
    @DisplayName("With an empty library the target draws the chosen card back")
    void emptyLibraryDrawsChosenCardBack() {
        ElvishWarrior chosen = new ElvishWarrior();
        harness.setHand(player2, List.of(chosen));
        harness.setLibrary(player2, List.of());
        resolveVendilionCliqueTargeting(player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void resolveVendilionCliqueTargeting(UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new VendilionClique())));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetPlayerId);
        resolveAllTriggers();
    }
}
