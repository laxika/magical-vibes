package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealTopCardMayPlayFreeOrExileEffectHandler;
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

@ExtendWith(MockitoExtension.class)
class RevealTopCardMayPlayFreeOrExileEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
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
    private RevealTopCardMayPlayFreeOrExileEffectHandler revealTopCardMayPlayFreeOrExileEffectHandler;

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

        libraryRevealSupport = new LibraryRevealSupport(gameBroadcastService, sessionManager, cardViewFactory);
        revealTopCardMayPlayFreeOrExileEffectHandler = new RevealTopCardMayPlayFreeOrExileEffectHandler(gameBroadcastService, exileService);

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
            @DisplayName("Empty library logs and returns")
            void emptyLibraryLogs() {
                RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                        player1Id, "Djinn of Wishes", List.of(effect));

                revealTopCardMayPlayFreeOrExileEffectHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("Land card exiled when not controller's turn")
            void landExiledNotControllersTurn() {
                Card land = createCard("Forest", CardType.LAND);
                gd.playerDecks.get(player1Id).add(land);
                gd.activePlayerId = player2Id; // not controller's turn

                RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                        player1Id, "Djinn of Wishes", List.of(effect));

                revealTopCardMayPlayFreeOrExileEffectHandler.resolve(gd, entry, effect);

                verify(exileService).exileCard(gd, player1Id, land);
                assertThat(gd.playerDecks.get(player1Id)).isEmpty();
                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("Land card exiled when land already played this turn")
            void landExiledAlreadyPlayed() {
                Card land = createCard("Forest", CardType.LAND);
                gd.playerDecks.get(player1Id).add(land);
                gd.activePlayerId = player1Id;
                gd.landsPlayedThisTurn.put(player1Id, 1);

                RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                        player1Id, "Djinn of Wishes", List.of(effect));

                revealTopCardMayPlayFreeOrExileEffectHandler.resolve(gd, entry, effect);

                verify(exileService).exileCard(gd, player1Id, land);
                assertThat(gd.pendingMayAbilities).isEmpty();
            }

            @Test
            @DisplayName("Playable card queues may ability")
            void playableCardQueuesMayAbility() {
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt", CardType.INSTANT));

                RevealTopCardMayPlayFreeOrExileEffect effect = new RevealTopCardMayPlayFreeOrExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Djinn of Wishes"),
                        player1Id, "Djinn of Wishes", List.of(effect));

                revealTopCardMayPlayFreeOrExileEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).hasSize(1);
                assertThat(gd.pendingMayAbilities.getFirst().description()).contains("Play").contains("Lightning Bolt");
            }
}
