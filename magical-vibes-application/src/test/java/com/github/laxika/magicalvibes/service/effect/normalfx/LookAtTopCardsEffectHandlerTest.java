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
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithAPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock private GameLogService gameLogService;
    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @Mock private GraveyardService graveyardService;
    @Mock private LifeSupport lifeSupport;

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
        lenient().doAnswer(inv -> {
            UUID playerId = inv.getArgument(1);
            Card card = inv.getArgument(2);
            gd.playerGraveyards.get(playerId).add(card);
            return null;
        }).when(graveyardService).addCardToGraveyard(
                eq(gd), any(UUID.class), any(Card.class), any(Zone.class));

        libraryRevealSupport = new LibraryRevealSupport(gameLogService,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameLogService));
        InteractionHandlerRegistry interactionHandlerRegistry =
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameLogService);
        handler = new LookAtTopCardsEffectHandler(gameLogService, libraryRevealSupport, gameQueryService,
                predicateEvaluationService, amountEvaluationService, interactionHandlerRegistry, lifeSupport);
        ReflectionTestUtils.setField(handler, "graveyardService", graveyardService);
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

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("library is empty")));
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
            verifyNoInteractions(sessionManager);
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

        @Test
        @DisplayName("All matching cards auto-move to hand and the rest go to the bottom (Lair Delve)")
        void allMatchingAutoMoveToHandRestOnBottom() {
            Card land = createCard("Forest");
            Card spell = createCard("Lightning Bolt");
            gd.playerDecks.get(player1Id).add(land);
            gd.playerDecks.get(player1Id).add(spell);
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> ((Card) inv.getArgument(0)).getName().equals("Forest"));

            LookAtTopCardsEffect effect = new LookAtTopCardsEffect(new Fixed(2), new Fixed(2),
                    new CardTypePredicate(CardType.LAND), LookDestination.BOTTOM_OF_LIBRARY, true);
            handler.resolve(gd, entryFor("Lair Delve", effect), effect);

            assertThat(gd.playerHands.get(player1Id)).containsExactly(land);
            assertThat(gd.playerDecks.get(player1Id)).containsExactly(spell);
            assertThat(gd.interaction.activeInteraction()).isNull();
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("reveals") && logEntry.plainText().contains("Lair Delve")));
        }

        @Test
        @DisplayName("Non-matching cards never reach hand when a predicate is set")
        void noEligibleCardsAllGoToBottom() {
            Card spell1 = createCard("Lightning Bolt");
            Card spell2 = createCard("Shock");
            gd.playerDecks.get(player1Id).add(spell1);
            gd.playerDecks.get(player1Id).add(spell2);
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

            LookAtTopCardsEffect effect = new LookAtTopCardsEffect(new Fixed(2), new Fixed(2),
                    new CardTypePredicate(CardType.LAND), LookDestination.BOTTOM_OF_LIBRARY, true);
            handler.resolve(gd, entryFor("Lair Delve", effect), effect);

            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.LibraryReorder.class);
        }

        @Test
        @DisplayName("More eligible cards than chooseCount prompts a bottom-rest reveal choice")
        void moreEligibleThanChooseCountPrompts() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Forest"));
            gd.playerDecks.get(player1Id).add(createCard("Mountain"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(true);

            LookAtTopCardsEffect effect = new LookAtTopCardsEffect(new Fixed(2), new Fixed(1),
                    new CardTypePredicate(CardType.LAND), LookDestination.BOTTOM_OF_LIBRARY, false);
            handler.resolve(gd, entryFor("Lair Delve", effect), effect);

            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice.reorderRemainingToBottom()).isTrue();
            assertThat(choice.maxCount()).isEqualTo(1);
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

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("library is empty")));
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
            verifyNoInteractions(sessionManager);
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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) -> logEntry.plainText().contains("into their graveyard")));
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

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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
            verifyNoInteractions(sessionManager);
        }

        @Test
        @DisplayName("Public may-reveal one to hand rest to graveyard enters LIBRARY_SEARCH with restToGraveyard")
        void mayRevealOneRestToGraveyard() {
            stubCardViewFactory();
            Card bears = createCard("Grizzly Bears");
            gd.playerDecks.get(player1Id).add(bears);
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(0) == bears);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(
                    5, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Grisly Salvage", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().canFailToFind()).isTrue();
            assertThat(search.params().restToGraveyard()).isTrue();
            assertThat(search.params().reorderRemainingToBottom()).isFalse();
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("reveals")));
        }

        @Test
        @DisplayName("Public may-reveal with no matches bins everything to the graveyard")
        void mayRevealOneRestToGraveyardNoMatches() {
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            gd.playerDecks.get(player1Id).add(createCard("Giant Growth"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayRevealOneToHandRestToGraveyard(
                    5, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Grisly Salvage", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerGraveyards.get(player1Id)).hasSize(2);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
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
            verifyNoInteractions(sessionManager);
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

            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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
            verifyNoInteractions(sessionManager);
        }

        @Test
        @DisplayName("Dynamic mana-value cap filters battlefield candidates")
        void dynamicManaValueCapFiltersCandidates() {
            stubCardViewFactory();
            Card eligible = createCard("Grizzly Bears");
            eligible.setManaCost("{2}");
            Card tooExpensive = createCard("Hill Giant");
            tooExpensive.setManaCost("{3}");
            gd.playerDecks.get(player1Id).add(eligible);
            gd.playerDecks.get(player1Id).add(tooExpensive);
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenReturn(true);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                    2, new CardTypePredicate(CardType.CREATURE), new Fixed(2));
            handler.resolve(gd, entryFor("Loot, Exuberant Explorer", effect), effect);

            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validCardIds()).containsExactly(eligible.getId());
            assertThat(choice.maxCount()).isEqualTo(1);
            assertThat(choice.randomRemainingToBottom()).isTrue();
        }

        @Test
        @DisplayName("Any-number battlefield choice can exile the remaining revealed cards")
        void anyNumberBattlefieldChoiceExilesRemainingCards() {
            stubCardViewFactory();
            gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player1Id).add(createCard("Forest"));
            gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> !((Card) inv.getArgument(0)).getName().equals("Lightning Bolt"));

            LookAtTopCardsEffect effect = new LookAtTopCardsEffect(
                    new Fixed(3), new Fixed(3), new CardTypePredicate(CardType.CREATURE),
                    LookDestination.EXILE, true, LibrarySearchDestination.BATTLEFIELD, true);
            handler.resolve(gd, entryFor("Xenagos, the Reveler", effect), effect);

            PendingInteraction.LibraryRevealChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.remainingToExile()).isTrue();
            assertThat(choice.reorderRemainingToBottom()).isFalse();
            assertThat(choice.randomRemainingToBottom()).isFalse();
        }

        @Test
        @DisplayName("Mandatory battlefield pick sends the other revealed cards to the graveyard")
        void mandatoryBattlefieldPickRestToGraveyard() {
            stubCardViewFactory();
            Card land = createCard("Forest");
            Card spell = createCard("Lightning Bolt");
            gd.playerDecks.get(player1Id).add(land);
            gd.playerDecks.get(player1Id).add(spell);
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(0) == land);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.putOneMatchingOntoBattlefieldRestToGraveyard(
                    2, new CardTypePredicate(CardType.LAND));
            handler.resolve(gd, entryFor("Cavalier of Thorns", effect), effect);

            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search).isNotNull();
            assertThat(search.params().cards()).containsExactly(land);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
            assertThat(search.params().canFailToFind()).isFalse();
            assertThat(search.params().restToGraveyard()).isTrue();
            assertThat(search.params().reorderRemainingToBottom()).isFalse();
            assertThat(search.params().sourceCards()).containsExactly(land, spell);
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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
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

        @Test
        @DisplayName("Optional top-card pick can send the rest to the graveyard")
        void optionalTopPickRestToGraveyard() {
            stubCardViewFactory();
            Card first = createCard("Forest");
            Card second = createCard("Shock");
            Card third = createCard("Llanowar Elves");
            gd.playerDecks.get(player1Id).addAll(List.of(first, second, third));

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutOneOnTopRestToGraveyard(3);
            handler.resolve(gd, entryFor("Gutless Plunderer", effect), effect);

            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search).isNotNull();
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
            assertThat(search.params().canFailToFind()).isTrue();
            assertThat(search.params().restToGraveyard()).isTrue();
            assertThat(search.params().reorderRemainingToBottom()).isFalse();
            assertThat(search.params().sourceCards()).containsExactly(first, second, third);
        }

        @Test
        @DisplayName("Optional filtered top pick offers only matching cards and randomizes the rest")
        void optionalFilteredTopPickRandomizesRest() {
            stubCardViewFactory();
            Card land = createCard("Forest");
            Card spell = createCard("Shock");
            Card creature = createCard("Llanowar Elves");
            gd.playerDecks.get(player1Id).addAll(List.of(land, spell, creature));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> inv.getArgument(0) == land);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutMatchingOnTopRestOnBottomRandom(
                    3, new CardTypePredicate(CardType.LAND));
            handler.resolve(gd, entryFor("Silhana Wayfinder", effect), effect);

            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search).isNotNull();
            assertThat(search.params().cards()).containsExactly(land);
            assertThat(search.params().sourceCards()).containsExactly(land, spell, creature);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
            assertThat(search.params().canFailToFind()).isTrue();
            assertThat(search.params().reveals()).isTrue();
            assertThat(search.params().followUp().secondBoundedPick().randomRest()).isTrue();
        }

        @Test
        @DisplayName("Optional filtered top pick bottoms all cards randomly when nothing matches")
        void optionalFilteredTopPickNoMatchBottomsAllRandomly() {
            Card first = createCard("Shock");
            Card second = createCard("Giant Growth");
            gd.playerDecks.get(player1Id).addAll(List.of(first, second));
            when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any()))
                    .thenReturn(false);

            LookAtTopCardsEffect effect = LookAtTopCardsEffect.mayPutMatchingOnTopRestOnBottomRandom(
                    2, new CardTypePredicate(CardType.CREATURE));
            handler.resolve(gd, entryFor("Silhana Wayfinder", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerDecks.get(player1Id)).containsExactlyInAnyOrder(first, second);
        }
    }
}
