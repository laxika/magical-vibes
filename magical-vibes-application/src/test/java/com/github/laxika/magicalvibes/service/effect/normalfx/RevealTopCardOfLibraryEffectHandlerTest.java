package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevealTopCardOfLibraryEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private SessionManager sessionManager;
    @Mock
    private CardViewFactory cardViewFactory;
    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private ExileService exileService;
    @Mock
    private LifeSupport lifeSupport;
    private LibraryRevealSupport libraryRevealSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private RevealTopCardOfLibraryEffectHandler revealTopCardOfLibraryEffectHandler;

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

        libraryRevealSupport = new LibraryRevealSupport(gameLogService,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameLogService));
        revealTopCardOfLibraryEffectHandler = new RevealTopCardOfLibraryEffectHandler(gameLogService, lifeSupport);

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
            @DisplayName("Reveals top card name in log")
            void revealsTopCard() {
                Card topCard = createCard("Grizzly Bears");
                gd.playerDecks.get(player2Id).add(topCard);

                RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect(LibraryOwner.TARGET_PLAYER);
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Aven Windreader"),
                        player1Id, "Aven Windreader", List.of(effect), 0,
                        player2Id, null);

                revealTopCardOfLibraryEffectHandler.resolve(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("reveals") && logEntry.plainText().contains("Grizzly Bears")));
            }

            @Test
            @DisplayName("Empty library logs appropriately")
            void emptyLibraryLogged() {
                RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect(LibraryOwner.TARGET_PLAYER);
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Aven Windreader"),
                        player1Id, "Aven Windreader", List.of(effect), 0,
                        player2Id, null);

                revealTopCardOfLibraryEffectHandler.resolve(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("library is empty")));
            }

            @Test
            @DisplayName("Gains life when the revealed card is a land and lifeGainIfLand is set")
            void gainsLifeOnRevealedLand() {
                Card topCard = createCard("Forest", CardType.LAND);
                gd.playerDecks.get(player2Id).add(topCard);

                RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect(LibraryOwner.TARGET_PLAYER, 1);
                Card source = createCard("Prophecy", CardType.SORCERY);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, source,
                        player1Id, "Prophecy", List.of(effect), 0,
                        player2Id, null);

                revealTopCardOfLibraryEffectHandler.resolve(gd, entry, effect);

                verify(lifeSupport).applyGainLife(gd, player1Id, 1, "Prophecy", source, StackEntryType.SORCERY_SPELL);
            }

            @Test
            @DisplayName("No life gain when the revealed card is not a land")
            void noLifeGainOnNonLand() {
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears", CardType.CREATURE));

                RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect(LibraryOwner.TARGET_PLAYER, 1);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Prophecy", CardType.SORCERY),
                        player1Id, "Prophecy", List.of(effect), 0,
                        player2Id, null);

                revealTopCardOfLibraryEffectHandler.resolve(gd, entry, effect);

                verifyNoInteractions(lifeSupport);
            }

            @Test
            @DisplayName("Controller owner reveals the controller's own top card even when the entry carries a target")
            void controllerOwnerIgnoresTargetId() {
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));

                RevealTopCardOfLibraryEffect effect = new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER);
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Cruel Deceiver"),
                        player1Id, "Cruel Deceiver", List.of(effect), 0,
                        player2Id, null);

                revealTopCardOfLibraryEffectHandler.resolve(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("Llanowar Elves")));
                verify(gameLogService, never()).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("Grizzly Bears")));
            }
}
