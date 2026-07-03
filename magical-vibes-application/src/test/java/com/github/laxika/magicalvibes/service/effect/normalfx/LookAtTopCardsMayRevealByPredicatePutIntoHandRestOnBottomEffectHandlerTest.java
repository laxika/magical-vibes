package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private CardViewFactory cardViewFactory;
    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private ExileService exileService;
    private LibraryRevealSupport libraryRevealSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler;

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

        libraryRevealSupport = new LibraryRevealSupport(gameBroadcastService, sessionManager, cardViewFactory,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameBroadcastService));
        lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler = new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler(gameQueryService, predicateEvaluationService, sessionManager, cardViewFactory, libraryRevealSupport);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        private static Card createCard(String name, CardType type) {
            Card card = createCard(name);
            card.setType(type);
            return card;
        }

        private static Card createCard(String name, CardType type, String manaCost) {
            Card card = createCard(name, type);
            card.setManaCost(manaCost);
            return card;
        }

        private void stubCardViewFactory() {
            lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
        }

        // =========================================================================
        // resolveRevealTopCardOfLibrary
        // =========================================================================

    @Test
            @DisplayName("Empty library logs")
            void emptyLibraryLogs() {
                LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                        new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                        player1Id, "Mul Daya Channelers", List.of(effect));

                lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
            }

            @Test
            @DisplayName("No matching creatures reorders remaining to bottom")
            void noCreaturesReordersToBottom() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));
                gd.playerDecks.get(player1Id).add(createCard("Giant Growth", CardType.INSTANT));

                LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                        new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                        player1Id, "Mul Daya Channelers", List.of(effect));

                lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler.resolve(gd, entry, effect);

                // With 2 cards and no creatures, should begin library reorder (to bottom)
                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
            }

            @Test
            @DisplayName("Single non-creature card put back on bottom without reorder prompt")
            void singleNonCreaturePutOnBottom() {
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

                LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                        new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                        player1Id, "Mul Daya Channelers", List.of(effect));

                lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler.resolve(gd, entry, effect);

                // Single card goes back to deck without reorder prompt
                assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
                assertThat(gd.interaction.awaitingInputType()).isNull();
            }

            @Test
            @DisplayName("Has matching creature enters LIBRARY_SEARCH state")
            void hasCreatureEntersSearchState() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));
                gd.playerDecks.get(player1Id).add(createCard("Giant Growth", CardType.INSTANT));

                when(predicateEvaluationService.matchesCardPredicate(any(), any(), any())).thenCallRealMethod();
                when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenCallRealMethod();

                LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                        new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Mul Daya Channelers"),
                        player1Id, "Mul Daya Channelers", List.of(effect));

                lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
                verify(sessionManager).sendToPlayer(eq(player1Id), any());
            }

            @Test
            @DisplayName("anyNumber=true enters LIBRARY_REVEAL_CHOICE state")
            void anyNumberEntersRevealChoice() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears", CardType.CREATURE));
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves", CardType.CREATURE));
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

                when(predicateEvaluationService.matchesCardPredicate(any(), any(), any())).thenCallRealMethod();
                when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenCallRealMethod();

                LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                        new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(3, new CardTypePredicate(CardType.CREATURE), true);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lead the Stampede"),
                        player1Id, "Lead the Stampede", List.of(effect));

                lookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
                verify(sessionManager).sendToPlayer(eq(player1Id), any());
            }
}
