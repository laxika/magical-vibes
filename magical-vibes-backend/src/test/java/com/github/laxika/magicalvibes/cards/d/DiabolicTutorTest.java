package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiabolicTutorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Diabolic Tutor has correct card properties")
    void hasCorrectProperties() {
        DiabolicTutor card = new DiabolicTutor();

        assertThat(card.getName()).isEqualTo("Diabolic Tutor");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(SearchLibraryForCardToHandEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Diabolic Tutor puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Diabolic Tutor");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Diabolic Tutor presents all cards from library for choice")
    void resolvingPresentsAllCardsForChoice() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId).isEqualTo(player1.getId());
        // All cards from library are presented (not just a subset)
        assertThat(gd.interaction.awaitingLibrarySearchCards).hasSize(4);
        assertThat(gd.interaction.awaitingLibrarySearchCards.stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Swamp", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and shuffles library")
    void choosingCardPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.awaitingLibrarySearchCards.getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card is in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));

        // Library lost one card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);

        // Awaiting state is cleared
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchCards).isNull();
    }

    @Test
    @DisplayName("Can choose a non-land card from library")
    void canChooseNonLandCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Find Grizzly Bears in the search cards
        int bearsIndex = -1;
        for (int i = 0; i < gd.interaction.awaitingLibrarySearchCards.size(); i++) {
            if (gd.interaction.awaitingLibrarySearchCards.get(i).getName().equals("Grizzly Bears")) {
                bearsIndex = i;
                break;
            }
        }
        assertThat(bearsIndex).isGreaterThanOrEqualTo(0);

        harness.getGameService().handleLibraryCardChosen(gd, player1, bearsIndex);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Diabolic Tutor does not reveal the chosen card (unrestricted search)")
    void doesNotRevealChosenCard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingLibrarySearchReveals).isFalse();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Log should NOT mention "reveals"
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("reveals") && entry.contains("puts it into their hand"));
        // Log should mention putting a card into hand
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("puts a card into their hand"));
    }

    // ===== Cannot fail to find (unrestricted search per MTG rule 701.19b) =====

    @Test
    @DisplayName("Unrestricted search sets canFailToFind to false")
    void canFailToFindIsFalse() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingLibrarySearchCanFailToFind).isFalse();
    }

    @Test
    @DisplayName("Player cannot fail to find with unrestricted search")
    void cannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() -> harness.getGameService().handleLibraryCardChosen(gd, player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with empty library logs and does not crash")
    void emptyLibrary() {
        setupAndCast();

        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Sorcery goes to graveyard after resolution =====

    @Test
    @DisplayName("Diabolic Tutor goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery → library search prompt

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Diabolic Tutor"));
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}

