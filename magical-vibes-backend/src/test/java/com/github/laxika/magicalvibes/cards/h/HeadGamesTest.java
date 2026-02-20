package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
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
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadGamesTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Head Games has correct card properties")
    void hasCorrectProperties() {
        HeadGames card = new HeadGames();

        assertThat(card.getName()).isEqualTo("Head Games");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{3}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(HeadGamesEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new HeadGames()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Head Games");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new HeadGames()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving — opponent's hand goes on top of library, caster searches =====

    @Test
    @DisplayName("Resolving puts opponent's hand on top of library and starts library search")
    void resolvingMovesHandAndStartsSearch() {
        Card handCard1 = new GrizzlyBears();
        Card handCard2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(handCard1, handCard2)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        // Opponent's hand should be empty (cards moved to library)
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Library search state should be set up for caster
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingLibrarySearchTargetPlayerId).isEqualTo(player2.getId());
        assertThat(gd.interaction.awaitingLibrarySearchRemainingCount).isEqualTo(2);

        // Search cards should contain the full library (including former hand cards)
        assertThat(gd.interaction.awaitingLibrarySearchCards).hasSize(4); // 2 library + 2 from hand
    }

    @Test
    @DisplayName("Caster picks cards from opponent's library into opponent's hand")
    void casterPicksCardsIntoOpponentHand() {
        Card handCard1 = new GrizzlyBears();
        Card handCard2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(handCard1, handCard2)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        // First pick
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Should still be in library search mode (1 remaining)
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchRemainingCount).isEqualTo(1);
        assertThat(gd.interaction.awaitingLibrarySearchTargetPlayerId).isEqualTo(player2.getId());

        // One card should be in opponent's hand now
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Library should have lost one card
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("After all picks are done, library is shuffled and state is cleared")
    void allPicksDoneShufflesAndClears() {
        Card handCard1 = new GrizzlyBears();
        Card handCard2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(handCard1, handCard2)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        // Pick card 1
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Pick card 2 (last pick)
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // State should be fully cleared
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchCards).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchTargetPlayerId).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchRemainingCount).isZero();

        // Opponent's hand should have exactly 2 cards
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        // Opponent's library should have lost 2 cards (4 - 2 = 2)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Caster can choose specific cards from opponent's library")
    void casterChoosesSpecificCards() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));

        // Set up opponent's library with known cards
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        Card swamp = new Swamp();
        Card plains = new Plains();
        deck.addAll(List.of(swamp, plains));

        castHeadGames();
        harness.passBothPriorities();

        // Library now has 3 cards (1 from hand + 2 original)
        assertThat(gd.interaction.awaitingLibrarySearchCards).hasSize(3);
        assertThat(gd.interaction.awaitingLibrarySearchRemainingCount).isEqualTo(1);

        // Find the Swamp in search cards
        int swampIndex = -1;
        for (int i = 0; i < gd.interaction.awaitingLibrarySearchCards.size(); i++) {
            if (gd.interaction.awaitingLibrarySearchCards.get(i).getName().equals("Swamp")) {
                swampIndex = i;
                break;
            }
        }
        assertThat(swampIndex).isGreaterThanOrEqualTo(0);

        harness.getGameService().handleLibraryCardChosen(gd, player1, swampIndex);

        // Opponent's hand should have the Swamp
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Swamp");
    }

    // ===== Empty hand edge case =====

    @Test
    @DisplayName("Resolving against empty hand shuffles library and does nothing else")
    void emptyHandShufflesAndDoesNothing() {
        harness.setHand(player2, List.of());
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        // No library search should be initiated
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.interaction.awaitingLibrarySearchPlayerId).isNull();

        // Opponent's hand is still empty
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Log should mention no cards in hand
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards in hand"));
    }

    // ===== Single card hand =====

    @Test
    @DisplayName("Resolving with a single card in hand requires one pick and finishes")
    void singleCardHandSinglePick() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        // Should be in library search with 1 remaining
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchRemainingCount).isEqualTo(1);

        // Pick a card — this is the only pick, so state should be cleared
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Wrong player cannot choose from library")
    void wrongPlayerCannotChoose() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.getGameService().handleLibraryCardChosen(gd, player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.getGameService().handleLibraryCardChosen(gd, player1, 999))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Cannot fail to find (unrestricted search)")
    void cannotFailToFind() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingLibrarySearchCanFailToFind).isFalse();

        assertThatThrownBy(() -> harness.getGameService().handleLibraryCardChosen(gd, player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
    }

    // ===== Sorcery goes to graveyard =====

    @Test
    @DisplayName("Head Games goes to caster's graveyard after fully resolving")
    void goesToGraveyardAfterResolving() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Head Games"));
    }

    // ===== Cards not added to caster's hand =====

    @Test
    @DisplayName("Chosen cards go to opponent's hand, not caster's hand")
    void cardsGoToOpponentHandNotCasterHand() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        harness.setHand(player1, new ArrayList<>(List.of(new HeadGames())));
        setupOpponentLibrary();

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Caster's hand should be empty (Head Games was cast from it)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Caster's hand should still be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Opponent's hand should have the card
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Hand-to-library step is logged")
    void handToLibraryIsLogged() {
        Card handCard1 = new GrizzlyBears();
        Card handCard2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(handCard1, handCard2)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("puts 2 cards from their hand on top of their library"));
    }

    @Test
    @DisplayName("Library search is logged")
    void librarySearchIsLogged() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("searches") && log.contains("library"));
    }

    @Test
    @DisplayName("Completion is logged with shuffle")
    void completionIsLogged() {
        Card handCard = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handCard)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is shuffled"));
    }

    // ===== Multi-pick search cards update between picks =====

    @Test
    @DisplayName("Search cards are refreshed after each pick (chosen card no longer available)")
    void searchCardsRefreshAfterPick() {
        Card handCard1 = new GrizzlyBears();
        Card handCard2 = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(handCard1, handCard2)));
        setupOpponentLibrary();

        castHeadGames();
        harness.passBothPriorities();

        int initialSearchSize = gd.interaction.awaitingLibrarySearchCards.size();

        // First pick
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Search cards should have one fewer card
        assertThat(gd.interaction.awaitingLibrarySearchCards).hasSize(initialSearchSize - 1);
    }

    // ===== Helpers =====

    private void castHeadGames() {
        harness.setHand(player1, List.of(new HeadGames()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void setupOpponentLibrary() {
        List<Card> deck = gd.playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp()));
    }
}

