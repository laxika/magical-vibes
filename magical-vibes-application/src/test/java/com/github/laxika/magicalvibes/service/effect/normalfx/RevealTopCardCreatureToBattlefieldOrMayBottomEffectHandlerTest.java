package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevealTopCardCreatureToBattlefieldOrMayBottomEffectHandlerTest {

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
    private RevealTopCardCreatureToBattlefieldOrMayBottomEffectHandler revealTopCardCreatureToBattlefieldOrMayBottomEffectHandler;

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
        revealTopCardCreatureToBattlefieldOrMayBottomEffectHandler = new RevealTopCardCreatureToBattlefieldOrMayBottomEffectHandler(gameBroadcastService, battlefieldEntryService);

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
                RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                        new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                        player1Id, "Lurking Predators", List.of(effect));

                revealTopCardCreatureToBattlefieldOrMayBottomEffectHandler.resolve(gd, entry, new RevealTopCardCreatureToBattlefieldOrMayBottomEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
            }

            @Test
            @DisplayName("Creature card enters battlefield")
            void creatureEntersBattlefield() {
                Card creature = createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}");
                gd.playerDecks.get(player1Id).add(creature);

                RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                        new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                        player1Id, "Lurking Predators", List.of(effect));

                revealTopCardCreatureToBattlefieldOrMayBottomEffectHandler.resolve(gd, entry, new RevealTopCardCreatureToBattlefieldOrMayBottomEffect());

                assertThat(gd.playerDecks.get(player1Id)).isEmpty();
                verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
                verify(battlefieldEntryService).handleCreatureEnteredBattlefield(
                        eq(gd), eq(player1Id), eq(creature), eq(null), eq(false));
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("enters the battlefield")));
            }

            @Test
            @DisplayName("Non-creature card queues may ability to put on bottom")
            void nonCreatureQueuesMayAbility() {
                Card instant = createCard("Lightning Bolt", CardType.INSTANT, "{R}");
                gd.playerDecks.get(player1Id).add(instant);

                RevealTopCardCreatureToBattlefieldOrMayBottomEffect effect =
                        new RevealTopCardCreatureToBattlefieldOrMayBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Lurking Predators"),
                        player1Id, "Lurking Predators", List.of(effect));

                revealTopCardCreatureToBattlefieldOrMayBottomEffectHandler.resolve(gd, entry, new RevealTopCardCreatureToBattlefieldOrMayBottomEffect());

                assertThat(gd.pendingMayAbilities).hasSize(1);
                assertThat(gd.pendingMayAbilities.getFirst().description())
                        .contains("Put").contains("Lightning Bolt").contains("bottom");
                verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(any(), any(), any(Permanent.class));
            }
}
