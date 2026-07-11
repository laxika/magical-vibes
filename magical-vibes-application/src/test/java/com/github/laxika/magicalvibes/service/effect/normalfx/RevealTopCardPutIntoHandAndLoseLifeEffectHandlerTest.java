package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
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
class RevealTopCardPutIntoHandAndLoseLifeEffectHandlerTest {

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
    private RevealTopCardPutIntoHandAndLoseLifeEffectHandler revealTopCardPutIntoHandAndLoseLifeEffectHandler;

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
        revealTopCardPutIntoHandAndLoseLifeEffectHandler = new RevealTopCardPutIntoHandAndLoseLifeEffectHandler(gameQueryService, gameBroadcastService);

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
                RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                        player1Id, "Dark Tutelage", List.of(effect));

                revealTopCardPutIntoHandAndLoseLifeEffectHandler.resolve(gd, entry, new RevealTopCardPutIntoHandAndLoseLifeEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
            }

            @Test
            @DisplayName("Reveals card, puts in hand, and loses life equal to mana value")
            void revealsAndLosesLife() {
                Card topCard = createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}");
                gd.playerDecks.get(player1Id).add(topCard);
                when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

                RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                        player1Id, "Dark Tutelage", List.of(effect));

                revealTopCardPutIntoHandAndLoseLifeEffectHandler.resolve(gd, entry, new RevealTopCardPutIntoHandAndLoseLifeEffect());

                assertThat(gd.playerHands.get(player1Id)).contains(topCard);
                assertThat(gd.playerDecks.get(player1Id)).isEmpty();
                // Life should decrease by 2 (MV of {1}{G})
                assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(18);
            }

            @Test
            @DisplayName("MV 0 card does not change life total")
            void mvZeroNoLifeLoss() {
                Card topCard = createCard("Ornithopter", CardType.CREATURE, "{0}");
                gd.playerDecks.get(player1Id).add(topCard);

                RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                        player1Id, "Dark Tutelage", List.of(effect));

                revealTopCardPutIntoHandAndLoseLifeEffectHandler.resolve(gd, entry, new RevealTopCardPutIntoHandAndLoseLifeEffect());

                assertThat(gd.playerHands.get(player1Id)).contains(topCard);
                assertThat(gd.playerLifeTotals).doesNotContainKey(player1Id); // unchanged from default
            }

            @Test
            @DisplayName("Life can't change still puts card in hand")
            void lifeCantChangeStillDraws() {
                Card topCard = createCard("Grizzly Bears", CardType.CREATURE, "{1}{G}");
                gd.playerDecks.get(player1Id).add(topCard);
                when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(false);

                RevealTopCardPutIntoHandAndLoseLifeEffect effect = new RevealTopCardPutIntoHandAndLoseLifeEffect();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dark Tutelage"),
                        player1Id, "Dark Tutelage", List.of(effect));

                revealTopCardPutIntoHandAndLoseLifeEffectHandler.resolve(gd, entry, new RevealTopCardPutIntoHandAndLoseLifeEffect());

                assertThat(gd.playerHands.get(player1Id)).contains(topCard);
                assertThat(gd.playerLifeTotals).doesNotContainKey(player1Id); // life didn't change
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("life total can't change")));
            }
}
