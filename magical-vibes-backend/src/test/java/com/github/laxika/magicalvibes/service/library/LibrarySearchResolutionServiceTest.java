package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.d.DistantMemories;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeadGames;
import com.github.laxika.magicalvibes.cards.l.LeoninArbiter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SylvanScrying;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibrarySearchResolutionServiceTest extends BaseCardTest {

    private void setupDeckWithBasicLands(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Swamp(), new GrizzlyBears()));
    }

    private void setupDeckWithNoLands(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    // =========================================================================
    // resolveSearchLibraryForCardTypesToHand (via SylvanScrying)
    // =========================================================================

    @Nested
    @DisplayName("resolveSearchLibraryForCardTypesToHand")
    class ResolveSearchLibraryForCardTypesToHand {

        @Test
        @DisplayName("Filters to matching card types and enters LIBRARY_SEARCH")
        void filtersToMatchingTypes() {
            harness.setHand(player1, List.of(new SylvanScrying()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
            // Should only show lands (Plains, Forest, Swamp) — not GrizzlyBears
            assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(3);
            assertThat(gd.interaction.awaitingLibrarySearchCards())
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("No matching cards shuffles and logs")
        void noMatchingCardsShuffles() {
            harness.setHand(player1, List.of(new SylvanScrying()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithNoLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("finds no") && log.contains("Library is shuffled"));
        }

        @Test
        @DisplayName("Empty library logs without crash")
        void emptyLibraryLogs() {
            harness.setHand(player1, List.of(new SylvanScrying()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("it is empty"));
        }
    }

    // =========================================================================
    // resolveSearchLibraryForCardTypesToBattlefield (via RampantGrowth)
    // =========================================================================

    @Nested
    @DisplayName("resolveSearchLibraryForCardTypesToBattlefield")
    class ResolveSearchLibraryForCardTypesToBattlefield {

        @Test
        @DisplayName("Filters to basic lands and enters LIBRARY_SEARCH")
        void filtersToBasicLands() {
            harness.setHand(player1, List.of(new RampantGrowth()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            // RampantGrowth requires basic supertype — only basic lands
            assertThat(gd.interaction.awaitingLibrarySearchCards())
                    .allMatch(c -> c.getName().equals("Plains")
                            || c.getName().equals("Forest")
                            || c.getName().equals("Swamp"));
        }

        @Test
        @DisplayName("No matching cards shuffles and logs")
        void noMatchingCardsShuffles() {
            harness.setHand(player1, List.of(new RampantGrowth()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithNoLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("finds no basic land cards") && log.contains("Library is shuffled"));
        }
    }

    // =========================================================================
    // resolveSearchLibraryForCardToHand (via DiabolicTutor)
    // =========================================================================

    @Nested
    @DisplayName("resolveSearchLibraryForCardToHand")
    class ResolveSearchLibraryForCardToHand {

        @Test
        @DisplayName("Opens all cards in library for search")
        void opensAllCardsForSearch() {
            harness.setHand(player1, List.of(new DiabolicTutor()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
            // All 4 cards should be available (no filtering)
            assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(4);
        }

        @Test
        @DisplayName("Empty library logs without entering search")
        void emptyLibraryLogs() {
            harness.setHand(player1, List.of(new DiabolicTutor()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("it is empty"));
        }
    }

    // =========================================================================
    // resolveDistantMemories (via DistantMemories)
    // =========================================================================

    @Nested
    @DisplayName("resolveDistantMemories")
    class ResolveDistantMemories {

        @Test
        @DisplayName("Sets up exile search with all cards")
        void setsUpExileSearch() {
            harness.setHand(player1, List.of(new DistantMemories()));
            harness.addMana(player1, ManaColor.BLUE, 4);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
            assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(4);
        }

        @Test
        @DisplayName("Empty library skips search and logs")
        void emptyLibraryLogs() {
            harness.setHand(player1, List.of(new DistantMemories()));
            harness.addMana(player1, ManaColor.BLUE, 4);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("it is empty"));
        }
    }

    // =========================================================================
    // resolveHeadGames (via HeadGames)
    // =========================================================================

    @Nested
    @DisplayName("resolveHeadGames")
    class ResolveHeadGames {

        @Test
        @DisplayName("Empty hand shuffles library and logs")
        void emptyHandShufflesLibrary() {
            harness.setHand(player1, List.of(new HeadGames()));
            harness.setHand(player2, List.of());
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("has no cards in hand"));
        }

        @Test
        @DisplayName("Target hand cards are put on top of library then search begins")
        void handPutOnLibraryThenSearchBegins() {
            Card bear1 = new GrizzlyBears();
            Card bear2 = new GrizzlyBears();
            harness.setHand(player1, List.of(new HeadGames()));
            harness.setHand(player2, List.of(bear1, bear2));
            harness.addMana(player1, ManaColor.BLACK, 5);

            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Player2's hand should be empty (put on library)
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            // Library should grow by 2 (the 2 cards from hand)
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 2);
            // Player1 should be searching player2's library
            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
            assertThat(gd.gameLog).anyMatch(log -> log.contains("puts") && log.contains("from their hand"));
        }

        @Test
        @DisplayName("Caster searches for exactly as many cards as target's hand size")
        void searchesForHandSizeCards() {
            harness.setHand(player1, List.of(new HeadGames()));
            harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("3 cards"));
        }
    }

    // =========================================================================
    // checkSearchRestriction (Leonin Arbiter with various search types)
    // =========================================================================

    @Nested
    @DisplayName("checkSearchRestriction")
    class CheckSearchRestriction {

        @Test
        @DisplayName("Leonin Arbiter prevents SylvanScrying search without payment")
        void preventsSearchWithoutPayment() {
            harness.addToBattlefield(player2, new LeoninArbiter());
            harness.setHand(player1, List.of(new SylvanScrying()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("prevented by Leonin Arbiter"));
        }

        @Test
        @DisplayName("Search proceeds when no Leonin Arbiter is present")
        void searchProceedsWithoutArbiter() {
            harness.setHand(player1, List.of(new SylvanScrying()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        }

        @Test
        @DisplayName("Search proceeds after paying Leonin Arbiter tax")
        void searchProceedsAfterPayingTax() {
            harness.addToBattlefield(player2, new LeoninArbiter());
            harness.setHand(player1, List.of(new DiabolicTutor()));
            harness.addMana(player1, ManaColor.BLACK, 6); // 4 for Tutor + 2 for Arbiter

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();

            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.paySearchTax(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        }

        @Test
        @DisplayName("Leonin Arbiter prevents RampantGrowth search and shuffles library")
        void preventsRampantGrowthSearch() {
            harness.addToBattlefield(player2, new LeoninArbiter());
            harness.setHand(player1, List.of(new RampantGrowth()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            setupDeckWithBasicLands(player1);

            int deckSize = gd.playerDecks.get(player1.getId()).size();

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("prevented by Leonin Arbiter"));
            // Library should be same size (shuffled, not modified)
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSize);
        }

        @Test
        @DisplayName("Leonin Arbiter prevents Distant Memories search")
        void preventsDistantMemoriesSearch() {
            harness.addToBattlefield(player2, new LeoninArbiter());
            harness.setHand(player1, List.of(new DistantMemories()));
            harness.addMana(player1, ManaColor.BLUE, 4);
            setupDeckWithBasicLands(player1);

            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("prevented by Leonin Arbiter"));
        }

        @Test
        @DisplayName("Leonin Arbiter prevents Head Games search but hand still goes to library")
        void preventsHeadGamesSearchButHandMovesToLibrary() {
            harness.addToBattlefield(player1, new LeoninArbiter());
            Card bear = new GrizzlyBears();
            harness.setHand(player1, List.of(new HeadGames()));
            harness.setHand(player2, List.of(bear));
            harness.addMana(player1, ManaColor.BLACK, 5);

            int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            // Hand was moved to library even though search was prevented
            assertThat(gd.playerHands.get(player2.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
            // Search was prevented
            assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("prevented by Leonin Arbiter"));
        }
    }
}
