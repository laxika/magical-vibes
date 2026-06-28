package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler;
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
class LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandlerTest {

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
    private LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler lookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler;

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
        lookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler = new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler(gameBroadcastService, sessionManager, cardViewFactory);

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
            @DisplayName("No charge counters logs and returns")
            void noChargeCountersLogs() {
                LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                        new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                        player1Id, "Shrine of Piercing Vision", List.of(effect), 0);

                lookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler.resolve(gd, entry, new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no charge counters")));
            }

            @Test
            @DisplayName("Empty library with counters logs")
            void emptyLibraryLogs() {
                LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                        new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                        player1Id, "Shrine of Piercing Vision", List.of(effect), 3);

                lookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler.resolve(gd, entry, new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
            }

            @Test
            @DisplayName("Single card goes directly to hand")
            void singleCardGoesToHand() {
                Card singleCard = createCard("Grizzly Bears");
                gd.playerDecks.get(player1Id).add(singleCard);

                LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                        new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                        player1Id, "Shrine of Piercing Vision", List.of(effect), 3);

                lookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler.resolve(gd, entry, new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect());

                assertThat(gd.playerHands.get(player1Id)).contains(singleCard);
            }

            @Test
            @DisplayName("Multiple cards enters HAND_TOP_BOTTOM_CHOICE state")
            void multipleCardsEntersChoice() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

                LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect effect =
                        new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Shrine of Piercing Vision"),
                        player1Id, "Shrine of Piercing Vision", List.of(effect), 3);

                lookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler.resolve(gd, entry, new LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect());

                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.HAND_TOP_BOTTOM_CHOICE);
                verify(sessionManager).sendToPlayer(eq(player1Id), any());
            }
}
