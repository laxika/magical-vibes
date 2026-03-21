package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LibraryShuffleResolutionServiceTest {

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @InjectMocks
    private LibraryShuffleResolutionService service;

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
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
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
            Card zenith = createCard("Red Sun's Zenith");
            ShuffleIntoLibraryEffect effect = new ShuffleIntoLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, zenith,
                    player1Id, "Red Sun's Zenith", List.of(effect));

            service.resolveShuffleIntoLibrary(gd, entry);

            assertThat(gd.playerGraveyards.get(player1Id))
                    .noneMatch(c -> c.getName().equals("Red Sun's Zenith"));
            assertThat(gd.playerDecks.get(player1Id))
                    .anyMatch(c -> c.getName().equals("Red Sun's Zenith"));
        }

        @Test
        @DisplayName("Shuffle log is recorded")
        void shuffleLogRecorded() {
            Card zenith = createCard("Red Sun's Zenith");
            ShuffleIntoLibraryEffect effect = new ShuffleIntoLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, zenith,
                    player1Id, "Red Sun's Zenith", List.of(effect));

            service.resolveShuffleIntoLibrary(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("shuffled into its owner's library")));
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
            Card bear1 = createCard("Grizzly Bears");
            Card bear2 = createCard("Grizzly Bears");
            gd.playerGraveyards.get(player1Id).addAll(List.of(bear1, bear2));
            int deckSizeBefore = gd.playerDecks.get(player1Id).size();

            ShuffleGraveyardIntoLibraryEffect effect = new ShuffleGraveyardIntoLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Reminisce"),
                    player1Id, "Reminisce", List.of(effect), 0, player1Id, null);

            service.resolveShuffleGraveyardIntoLibrary(gd, entry);

            assertThat(gd.playerDecks.get(player1Id)).hasSize(deckSizeBefore + 2);
            assertThat(gd.playerGraveyards.get(player1Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("shuffles their graveyard")));
        }

        @Test
        @DisplayName("Empty graveyard still shuffles library")
        void emptyGraveyardStillShuffles() {
            int deckSizeBefore = gd.playerDecks.get(player1Id).size();

            ShuffleGraveyardIntoLibraryEffect effect = new ShuffleGraveyardIntoLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Reminisce"),
                    player1Id, "Reminisce", List.of(effect), 0, player1Id, null);

            service.resolveShuffleGraveyardIntoLibrary(gd, entry);

            assertThat(gd.playerDecks.get(player1Id)).hasSize(deckSizeBefore);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("graveyard is empty")));
        }

        @Test
        @DisplayName("Falls back to controller when targetId is null (saga chapters / triggered abilities)")
        void fallsBackToControllerWhenNoTarget() {
            Card bear1 = createCard("Grizzly Bears");
            Card bear2 = createCard("Grizzly Bears");
            gd.playerGraveyards.get(player1Id).addAll(List.of(bear1, bear2));
            int deckSizeBefore = gd.playerDecks.get(player1Id).size();

            ShuffleGraveyardIntoLibraryEffect effect = new ShuffleGraveyardIntoLibraryEffect();
            // Saga chapter / triggered ability: no targetId
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("The Mending of Dominaria"),
                    player1Id, "The Mending of Dominaria's chapter III ability", List.of(effect));

            service.resolveShuffleGraveyardIntoLibrary(gd, entry);

            assertThat(gd.playerDecks.get(player1Id)).hasSize(deckSizeBefore + 2);
            assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("shuffles their graveyard")));
        }
    }
}
