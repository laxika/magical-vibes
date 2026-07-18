package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithAPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LookAtTopCardsEffectHandler}, the collapsed "look at top N, choose some"
 * family (formerly the ChooseOne / ChooseN / PerChargeCounter / MayRevealByPredicate /
 * PutMatchingOnBattlefield / PutOneOnTop records). Nested by flow.
 */
@ExtendWith(MockitoExtension.class)
class LookAtTopCardsEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private AmountEvaluationService amountEvaluationService;

    private LibraryRevealSupport libraryRevealSupport;
    private LookAtTopCardsEffectHandler handler;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.activePlayerId = player1Id;

        // Fixed amounts resolve to their literal value; anything else (unused here) resolves to 0.
        lenient().when(amountEvaluationService.evaluate(any(), any(), any())).thenAnswer(inv -> {
            DynamicAmount a = inv.getArgument(1);
            return a instanceof Fixed f ? f.value() : 0;
        });

        libraryRevealSupport = new LibraryRevealSupport(gameBroadcastService, sessionManager, cardViewFactory,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameBroadcastService));
        InteractionHandlerRegistry interactionHandlerRegistry =
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameBroadcastService);
        handler = new LookAtTopCardsEffectHandler(gameBroadcastService, libraryRevealSupport, gameQueryService,
                predicateEvaluationService, amountEvaluationService, interactionHandlerRegistry);
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private void stubCardViewFactory() {
        lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
    }

    private StackEntry entryFor(String cardName, LookAtTopCardsEffect effect) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, createCard(cardName), player1Id, cardName, List.of(effect));
    }

    // =========================================================================
    // Rest on the bottom of the library (Stress Dream / Shrine / Jar of Eyeballs)
    // =========================================================================

    @Nested
    class RestOnBottom {

        @Test
        @DisplayName("Empty library logs and stops")
        void emptyLibraryLogs() {
            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2));
            handler.resolve(gd, entryFor("Stress Dream", effect), effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("library is empty")));
        }

        @Test
        @DisplayName("Single card goes directly to hand, nothing on bottom")
        void singleCardGoesToHand() {
            Card single = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(single);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2));
            handler.resolve(gd, entryFor("Stress Dream", effect), effect);

            assertThat(gd.playerHands.get(player1Id)).contains(single);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Multiple cards enter LIBRARY_REVEAL_CHOICE (not the old hand/top/bottom split)")
        void multipleCardsEntersLibraryRevealChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2));
            handler.resolve(gd, entryFor("Stress Dream", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.reorderRemainingToBottom()).isTrue();
            assertThat(choice.remainingToGraveyard()).isFalse();
            assertThat(choice.maxCount()).isEqualTo(1);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }

        @Test
        @DisplayName("Zero look count (no counters) does nothing")
        void zeroLookCountDoesNothing() {
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(0));
            handler.resolve(gd, entryFor("Shrine of Piercing Vision", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
        }
    }

    // =========================================================================
    // Rest into the graveyard (Forbidden Alchemy / Dark Bargain / Tower Geist / Tracker's)
    // =========================================================================

    @Nested
    class RestToGraveyard {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseNToHandRestToGraveyard(4, 1);
            handler.resolve(gd, entryFor("Forbidden Alchemy", effect), effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("library is empty")));
        }

        @Test
        @DisplayName("Single card goes directly to hand")
        void singleCardGoesToHand() {
            Card single = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(single);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseNToHandRestToGraveyard(4, 1);
            handler.resolve(gd, entryFor("Forbidden Alchemy", effect), effect);

            assertThat(gd.playerHands.get(player1Id)).contains(single);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Multiple cards enter LIBRARY_REVEAL_CHOICE with graveyard rest destination")
        void multipleCardsEntersLibraryRevealChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth"));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseNToHandRestToGraveyard(4, 1);
            handler.resolve(gd, entryFor("Forbidden Alchemy", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.remainingToGraveyard()).isTrue();
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }

        @Test
        @DisplayName("No eligible cards (predicate matches none) puts all into graveyard")
        void noEligibleCardsToGraveyard() {
            Card a = createCard("Mountain");
            Card b = createCard("Forest");
            gd.playerDecks.get(player1Id).add(a);
            gd.playerDecks.get(player1Id).add(b);
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                    2, 1, new CardTypePredicate(CardType.CREATURE), false);
            handler.resolve(gd, entryFor("Tracker's Instincts", effect), effect);

            assertThat(gd.playerGraveyards.get(player1Id)).contains(a, b);
            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("into their graveyard")));
        }

        @Test
        @DisplayName("Reveal logs a public reveal message")
        void revealLogsPublicly() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(true);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                    2, 1, new CardTypePredicate(CardType.CREATURE), true);
            handler.resolve(gd, entryFor("Tracker's Instincts", effect), effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("reveals") && logEntry.plainText().contains("Tracker's Instincts")));
        }

        @Test
        @DisplayName("All matching cards auto-move to hand when chooseCount == lookCount (Mulch)")
        void allMatchingAutoMoveToHand() {
            Card land1 = createCard("Forest");
            Card land2 = createCard("Mountain");
            Card spell = createCard("Lightning Bolt");
            gd.playerDecks.get(player1Id).add(land1);
            gd.playerDecks.get(player1Id).add(spell);
            gd.playerDecks.get(player1Id).add(land2);
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> ((Card) inv.getArgument(0)).getName().contains("Forest")
                            || ((Card) inv.getArgument(0)).getName().contains("Mountain"));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.chooseNToHandRestToGraveyard(
                    3, 3, new CardTypePredicate(CardType.LAND), true);
            handler.resolve(gd, entryFor("Mulch", effect), effect);

            assertThat(gd.playerHands.get(player1Id)).containsExactlyInAnyOrder(land1, land2);
            assertThat(gd.playerGraveyards.get(player1Id)).containsExactly(spell);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    // =========================================================================
    // Optional may-reveal to hand (Commune with Nature / Lead the Stampede / Follow the Lumarets)
    // =========================================================================

    @Nested
    class MayRevealToHand {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                    3, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Commune with Nature", effect), effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("library is empty")));
        }

        @Test
        @DisplayName("No matching cards reorders remaining to bottom")
        void noMatchesReordersToBottom() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                    3, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Commune with Nature", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        }

        @Test
        @DisplayName("Single non-matching card goes back on the bottom without a reorder prompt")
        void singleNonMatchGoesToBottom() {
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                    3, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Commune with Nature", effect), effect);

            assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Single-pick 'may reveal one' enters LIBRARY_SEARCH")
        void singlePickEntersLibrarySearch() {
            stubCardViewFactory();
            Card bears = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(bears);
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(0) == bears);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(
                    3, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Commune with Nature", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().canFailToFind()).isTrue();
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }

        @Test
        @DisplayName("Any-number 'may reveal' enters LIBRARY_REVEAL_CHOICE")
        void anyNumberEntersRevealChoice() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> !((Card) inv.getArgument(0)).getName().contains("Bolt"));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealAnyNumberToHandRestOnBottom(
                    3, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Lead the Stampede", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.maxCount()).isEqualTo(2);
            assertThat(choice.reorderRemainingToBottom()).isTrue();
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }

        @Test
        @DisplayName("Bounded 'up to N' caps the pick count (Follow the Lumarets)")
        void boundedUpToCapsCount() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            gd.playerDecks.get(player1Id).add(createCard("Runeclaw Bear"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(true);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealUpToToHandRestOnBottom(
                    3, new CardTypePredicate(CardType.CREATURE), 2);
            handler.resolve(gd, entryFor("Follow the Lumarets", effect), effect);

            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.maxCount()).isEqualTo(2);
        }
    }

    // =========================================================================
    // May put one matching card onto the battlefield (Mayael / Mitotic Manipulation)
    // =========================================================================

    @Nested
    class MayPutOntoBattlefield {

        @Test
        @DisplayName("Empty library logs")
        void emptyLibraryLogs() {
            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(
                    7, new CardSharesNameWithAPermanentPredicate());
            handler.resolve(gd, entryFor("Mitotic Manipulation", effect), effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("library is empty")));
        }

        @Test
        @DisplayName("No matching cards reorders remaining to bottom")
        void noMatchesReordersToBottom() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenCallRealMethod();

            // No permanents on the battlefield share a name with the looked-at cards.
            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(
                    4, new CardSharesNameWithAPermanentPredicate());
            handler.resolve(gd, entryFor("Mitotic Manipulation", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        }

        @Test
        @DisplayName("Matching permanent name enters LIBRARY_SEARCH with battlefield destination")
        void matchingNameEntersSearchState() {
            stubCardViewFactory();
            Card bfCard = createCard("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(new Permanent(bfCard));
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenCallRealMethod();

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutMatchingOntoBattlefield(
                    4, new CardSharesNameWithAPermanentPredicate());
            handler.resolve(gd, entryFor("Mitotic Manipulation", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
        }
    }

    // =========================================================================
    // Put one on top, rest on the bottom (Cream of the Crop)
    // =========================================================================

    @Nested
    class PutOneOnTop {

        @Test
        @DisplayName("Single looked-at card goes back on top without a prompt")
        void singleCardGoesBackOnTop() {
            Card single = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(single);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.putOneOnTopRestOnBottom(3);
            handler.resolve(gd, entryFor("Cream of the Crop", effect), effect);

            assertThat(gd.playerDecks.get(player1Id)).containsExactly(single);
            assertThat(gd.interaction.activeInteraction()).isNull();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("on top of their library")));
        }

        @Test
        @DisplayName("Multiple cards enter LIBRARY_SEARCH with top-of-library destination")
        void multipleCardsEnterLibrarySearch() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.putOneOnTopRestOnBottom(3);
            handler.resolve(gd, entryFor("Cream of the Crop", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
            assertThat(search.params().reorderRemainingToBottom()).isTrue();
        }
    }
}
