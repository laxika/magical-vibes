package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.AvenWindreader;
import com.github.laxika.magicalvibes.cards.c.CloneShell;
import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.d.DistantMemories;
import com.github.laxika.magicalvibes.cards.d.DreambornMuse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Grindclock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeadGames;
import com.github.laxika.magicalvibes.cards.l.LeoninArbiter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.r.RedSunsZenith;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.SageOwl;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SylvanScrying;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.cards.t.Traumatize;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryResolutionServiceTest extends BaseCardTest {

    // =========================================================================
    // Helpers
    // =========================================================================

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

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
    // resolveShuffleIntoLibrary (via RedSunsZenith)
    // =========================================================================

    @Nested
    @DisplayName("resolveShuffleIntoLibrary")
    class ResolveShuffleIntoLibrary {

        @Test
        @DisplayName("Card is shuffled into library instead of going to graveyard")
        void cardShuffledIntoLibrary() {
            harness.setHand(player1, List.of(new RedSunsZenith()));
            harness.addMana(player1, ManaColor.RED, 4);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 3, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Red Sun's Zenith"));
            assertThat(gd.playerDecks.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Red Sun's Zenith"));
        }

        @Test
        @DisplayName("Shuffle log is recorded")
        void shuffleLogRecorded() {
            harness.setHand(player1, List.of(new RedSunsZenith()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled into its owner's library"));
        }
    }

    // =========================================================================
    // resolveMillByHandSize (via DreambornMuse)
    // =========================================================================

    @Nested
    @DisplayName("resolveMillByHandSize")
    class ResolveMillByHandSize {

        @Test
        @DisplayName("Mills cards equal to target player's hand size")
        void millsByHandSize() {
            harness.addToBattlefield(player1, new DreambornMuse());
            harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        }

        @Test
        @DisplayName("Mills nothing when hand is empty")
        void millsNothingWithEmptyHand() {
            harness.addToBattlefield(player1, new DreambornMuse());
            harness.setHand(player1, List.of());
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("mills nothing"));
        }

        @Test
        @DisplayName("Mills only remaining cards when hand size exceeds library")
        void cappedByDeckSize() {
            harness.addToBattlefield(player1, new DreambornMuse());
            harness.setHand(player1, List.of(
                    new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                    new GrizzlyBears(), new GrizzlyBears()));

            List<Card> deck = gd.playerDecks.get(player1.getId());
            while (deck.size() > 2) {
                deck.removeFirst();
            }

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        }
    }

    // =========================================================================
    // resolveMillTargetPlayerByChargeCounters (via Grindclock)
    // =========================================================================

    @Nested
    @DisplayName("resolveMillTargetPlayerByChargeCounters")
    class ResolveMillTargetPlayerByChargeCounters {

        @Test
        @DisplayName("Mills nothing with zero charge counters")
        void millsNothingWithZeroCounters() {
            Permanent grindclock = addReadyPermanent(player1, new Grindclock());
            List<Card> deck = gd.playerDecks.get(player2.getId());
            int deckSizeBefore = deck.size();

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("mills 0 cards"));
        }

        @Test
        @DisplayName("Mills capped by library size when counters exceed cards")
        void cappedByLibrarySize() {
            Permanent grindclock = addReadyPermanent(player1, new Grindclock());
            grindclock.setChargeCounters(10);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 3) {
                deck.removeFirst();
            }

            harness.activateAbility(player1, 0, 1, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        }
    }

    // =========================================================================
    // resolveMillHalfLibrary (via Traumatize)
    // =========================================================================

    @Nested
    @DisplayName("resolveMillHalfLibrary")
    class ResolveMillHalfLibrary {

        @Test
        @DisplayName("Mills half of library rounded down")
        void millsHalfRoundedDown() {
            harness.setHand(player1, List.of(new Traumatize()));
            harness.addMana(player1, ManaColor.BLUE, 5);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 11) {
                deck.removeFirst();
            }

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        }

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            harness.setHand(player1, List.of(new Traumatize()));
            harness.addMana(player1, ManaColor.BLUE, 5);
            gd.playerDecks.get(player2.getId()).clear();

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("mills nothing"));
        }
    }

    // =========================================================================
    // resolveShuffleGraveyardIntoLibrary (via Reminisce)
    // =========================================================================

    @Nested
    @DisplayName("resolveShuffleGraveyardIntoLibrary")
    class ResolveShuffleGraveyardIntoLibrary {

        @Test
        @DisplayName("Shuffles graveyard into library")
        void shufflesGraveyardIntoLibrary() {
            Card bear1 = new GrizzlyBears();
            Card bear2 = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bear1, bear2));
            harness.setHand(player1, List.of(new Reminisce()));
            harness.addMana(player1, ManaColor.BLUE, 3);
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            harness.castSorcery(player1, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their graveyard"));
        }

        @Test
        @DisplayName("Empty graveyard still shuffles library")
        void emptyGraveyardStillShuffles() {
            harness.setGraveyard(player1, new ArrayList<>());
            harness.setHand(player1, List.of(new Reminisce()));
            harness.addMana(player1, ManaColor.BLUE, 3);
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            harness.castSorcery(player1, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("graveyard is empty"));
        }
    }

    // =========================================================================
    // resolveRevealTopCardOfLibrary (via AvenWindreader)
    // =========================================================================

    @Nested
    @DisplayName("resolveRevealTopCardOfLibrary")
    class ResolveRevealTopCardOfLibrary {

        @Test
        @DisplayName("Reveals top card name in log")
        void revealsTopCard() {
            Permanent windreader = addReadyPermanent(player1, new AvenWindreader());
            harness.addMana(player1, ManaColor.BLUE, 2);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            deck.clear();
            Card topCard = new GrizzlyBears();
            deck.add(topCard);

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("reveals") && log.contains("Grizzly Bears"));
        }

        @Test
        @DisplayName("Empty library logs appropriately")
        void emptyLibraryLogged() {
            Permanent windreader = addReadyPermanent(player1, new AvenWindreader());
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player2.getId()).clear();

            harness.activateAbility(player1, 0, null, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }
    }

    // =========================================================================
    // resolveReorderTopCardsOfLibrary (via SageOwl)
    // =========================================================================

    @Nested
    @DisplayName("resolveReorderTopCardsOfLibrary")
    class ResolveReorderTopCardsOfLibrary {

        @Test
        @DisplayName("Empty library skips reorder")
        void emptyLibrarySkipsReorder() {
            harness.setHand(player1, List.of(new SageOwl()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }

        @Test
        @DisplayName("Single card skips reorder prompt")
        void singleCardSkipsReorder() {
            harness.setHand(player1, List.of(new SageOwl()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player1.getId()).clear();
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_REORDER state")
        void multipleCardsEntersReorderState() {
            harness.setHand(player1, List.of(new SageOwl()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
            assertThat(gd.interaction.awaitingLibraryReorderPlayerId()).isEqualTo(player1.getId());
        }
    }

    // =========================================================================
    // resolveLookAtTopCardsHandTopBottom (via TellingTime)
    // =========================================================================

    @Nested
    @DisplayName("resolveLookAtTopCardsHandTopBottom")
    class ResolveLookAtTopCardsHandTopBottom {

        @Test
        @DisplayName("Empty library does nothing")
        void emptyLibraryDoesNothing() {
            harness.setHand(player1, List.of(new TellingTime()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }

        @Test
        @DisplayName("Single card automatically goes to hand")
        void singleCardAutoToHand() {
            harness.setHand(player1, List.of(new TellingTime()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            gd.playerDecks.get(player1.getId()).clear();
            Card singleCard = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).add(singleCard);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Multiple cards enters HAND_TOP_BOTTOM_CHOICE state")
        void multipleCardsEntersChoice() {
            harness.setHand(player1, List.of(new TellingTime()));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
            assertThat(gd.interaction.awaitingHandTopBottomPlayerId()).isEqualTo(player1.getId());
            assertThat(gd.interaction.awaitingHandTopBottomCards()).hasSize(3);
        }
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
    // resolveImprintFromTopCards (via CloneShell ETB)
    // =========================================================================

    @Nested
    @DisplayName("resolveImprintFromTopCards")
    class ResolveImprintFromTopCards {

        @Test
        @DisplayName("Empty library logs and does nothing")
        void emptyLibraryLogs() {
            harness.setHand(player1, List.of(new CloneShell()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);
            gd.playerDecks.get(player1.getId()).clear();

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
        }

        @Test
        @DisplayName("Single card is automatically exiled face down")
        void singleCardAutoExiled() {
            harness.setHand(player1, List.of(new CloneShell()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);

            gd.playerDecks.get(player1.getId()).clear();
            Card singleCard = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).add(singleCard);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerExiledCards.get(player1.getId())).contains(singleCard);
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles a card face down"));
        }

        @Test
        @DisplayName("Multiple cards enters LIBRARY_SEARCH state for exile choice")
        void multipleCardsEntersSearchState() {
            harness.setHand(player1, List.of(new CloneShell()));
            harness.addMana(player1, ManaColor.COLORLESS, 5);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
            assertThat(gd.interaction.awaitingLibrarySearchPlayerId()).isEqualTo(player1.getId());
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
