package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ShuffleIntoLibraryEffectHandler;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShuffleIntoLibraryEffectHandlerTest {

    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ShuffleIntoLibraryEffectHandler shuffleIntoLibraryEffectHandler;

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
        shuffleIntoLibraryEffectHandler = new ShuffleIntoLibraryEffectHandler(gameBroadcastService);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // resolveShuffleIntoLibrary (via RedSunsZenith)
        // =========================================================================

    @Test
            @DisplayName("Card is shuffled into library instead of going to graveyard")
            void cardShuffledIntoLibrary() {
                Card zenith = createCard("Red Sun's Zenith");
                ShuffleIntoLibraryEffect effect = new ShuffleIntoLibraryEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, zenith,
                        player1Id, "Red Sun's Zenith", List.of(effect));

                shuffleIntoLibraryEffectHandler.resolve(gd, entry, effect);

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

                shuffleIntoLibraryEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("shuffled into its owner's library")));
            }
}
