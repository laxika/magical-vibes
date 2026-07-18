package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealLibraryTopMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
 * Unit tests for {@link LookAtTopCardsOfTargetLibraryEffectHandler}, the collapsed target-library
 * look family (formerly the peek / MayExileOne / MayShuffle / PutOneIntoGraveyard records).
 * Nested by action.
 */
@ExtendWith(MockitoExtension.class)
class LookAtTopCardsOfTargetLibraryEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;

    private LookAtTopCardsOfTargetLibraryEffectHandler handler;
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

        handler = new LookAtTopCardsOfTargetLibraryEffectHandler(gameBroadcastService,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameBroadcastService),
                cardViewFactory, sessionManager);
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private void stubCardViewFactory() {
        lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
    }

    private StackEntry entryTargeting(String cardName, LookAtTopCardsOfTargetLibraryEffect effect) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard(cardName),
                player1Id, cardName, List.of(effect), 0, player2Id, null);
    }

    @Test
    @DisplayName("Empty target library logs and stops")
    void emptyLibraryLogs() {
        LookAtTopCardsOfTargetLibraryEffect effect =
                new LookAtTopCardsOfTargetLibraryEffect(2, TargetLibraryAction.MAY_EXILE_ONE);
        handler.resolve(gd, entryTargeting("Psychic Surgery", effect), effect);

        verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                logEntry.plainText().contains("library is empty")));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Nested
    class LookOnly {

        @Test
        @DisplayName("Count 1 is a blocking acknowledge that keeps the card on top (Dewdrop Spy)")
        void singleCardBlockingLook() {
            stubCardViewFactory();
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));

            LookAtTopCardsOfTargetLibraryEffect effect =
                    new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.LOOK_ONLY);
            handler.resolve(gd, entryTargeting("Dewdrop Spy", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        }

        @Test
        @DisplayName("Count > 1 is a non-blocking private reveal that leaves the library untouched (Orcish Spy)")
        void multiCardNonBlockingLook() {
            stubCardViewFactory();
            Card a = createCard("Grizzly Bears");
            Card b = createCard("Llanowar Elves");
            gd.playerDecks.get(player2Id).add(a);
            gd.playerDecks.get(player2Id).add(b);

            LookAtTopCardsOfTargetLibraryEffect effect =
                    new LookAtTopCardsOfTargetLibraryEffect(3, TargetLibraryAction.LOOK_ONLY);
            handler.resolve(gd, entryTargeting("Orcish Spy", effect), effect);

            assertThat(gd.playerDecks.get(player2Id)).containsExactly(a, b);
            assertThat(gd.interaction.activeInteraction()).isNull();
            verify(sessionManager).sendToPlayer(eq(player1Id), any(RevealLibraryTopMessage.class));
        }
    }

    @Nested
    class MayExileOne {

        @Test
        @DisplayName("Has cards enters LIBRARY_SEARCH with exile destination")
        void hasCardsEntersSearchState() {
            stubCardViewFactory();
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player2Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsOfTargetLibraryEffect effect =
                    new LookAtTopCardsOfTargetLibraryEffect(2, TargetLibraryAction.MAY_EXILE_ONE);
            handler.resolve(gd, entryTargeting("Psychic Surgery", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.EXILE);
            assertThat(search.params().canFailToFind()).isTrue();
            verify(sessionManager).sendToPlayer(eq(player1Id), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("Player1") && logEntry.plainText().contains("Player2")));
        }
    }

    @Nested
    class MayShuffle {

        @Test
        @DisplayName("Queues a may-ability naming the looked-at cards (Visions)")
        void queuesMayAbility() {
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player2Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsOfTargetLibraryEffect effect =
                    new LookAtTopCardsOfTargetLibraryEffect(5, TargetLibraryAction.MAY_SHUFFLE);
            handler.resolve(gd, entryTargeting("Visions", effect), effect);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Grizzly Bears", "Llanowar Elves");
            assertThat(gd.playerDecks.get(player2Id)).hasSize(2);
        }
    }

    @Nested
    class PutOneIntoGraveyard {

        @Test
        @DisplayName("Enters a mandatory LIBRARY_SEARCH with graveyard destination (Cruel Fate)")
        void entersMandatorySearch() {
            stubCardViewFactory();
            gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));
            gd.playerDecks.get(player2Id).add(createCard("Llanowar Elves"));

            LookAtTopCardsOfTargetLibraryEffect effect =
                    new LookAtTopCardsOfTargetLibraryEffect(5, TargetLibraryAction.PUT_ONE_INTO_GRAVEYARD);
            handler.resolve(gd, entryTargeting("Cruel Fate", effect), effect);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
            assertThat(search.params().canFailToFind()).isFalse();
        }
    }
}
