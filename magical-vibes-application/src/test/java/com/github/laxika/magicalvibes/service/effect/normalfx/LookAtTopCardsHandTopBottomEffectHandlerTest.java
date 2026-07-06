package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.HandTopBottomChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
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
class LookAtTopCardsHandTopBottomEffectHandlerTest {

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
    private LookAtTopCardsHandTopBottomEffectHandler lookAtTopCardsHandTopBottomEffectHandler;

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
        InteractionHandlerRegistry interactionHandlerRegistry = new InteractionHandlerRegistry();
        interactionHandlerRegistry.register(new HandTopBottomChoiceInteractionHandler(
                sessionManager, cardViewFactory, gameBroadcastService, mock(TurnProgressionService.class)));
        lookAtTopCardsHandTopBottomEffectHandler = new LookAtTopCardsHandTopBottomEffectHandler(
                gameBroadcastService, interactionHandlerRegistry, libraryRevealSupport);

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
            @DisplayName("Empty library does nothing")
            void emptyLibraryDoesNothing() {
                LookAtTopCardsHandTopBottomEffect effect = new LookAtTopCardsHandTopBottomEffect(3);
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Telling Time"),
                        player1Id, "Telling Time", List.of(effect));

                lookAtTopCardsHandTopBottomEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isNull();
                assertThat(gd.playerHands.get(player1Id)).isEmpty();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("library is empty")));
            }

            @Test
            @DisplayName("Single card automatically goes to hand")
            void singleCardAutoToHand() {
                Card singleCard = createCard("Grizzly Bears");
                gd.playerDecks.get(player1Id).add(singleCard);

                LookAtTopCardsHandTopBottomEffect effect = new LookAtTopCardsHandTopBottomEffect(3);
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Telling Time"),
                        player1Id, "Telling Time", List.of(effect));

                lookAtTopCardsHandTopBottomEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isNull();
                assertThat(gd.playerHands.get(player1Id)).contains(singleCard);
                assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            }

            @Test
            @DisplayName("Multiple cards enters HAND_TOP_BOTTOM_CHOICE state")
            void multipleCardsEntersChoice() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
                gd.playerDecks.get(player1Id).add(createCard("Lightning Bolt"));

                LookAtTopCardsHandTopBottomEffect effect = new LookAtTopCardsHandTopBottomEffect(3);
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Telling Time"),
                        player1Id, "Telling Time", List.of(effect));

                lookAtTopCardsHandTopBottomEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandTopBottomChoice.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.HandTopBottomChoice.class).playerId()).isEqualTo(player1Id);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.HandTopBottomChoice.class).cards()).hasSize(3);
                verify(sessionManager).sendToPlayer(eq(player1Id), any());
            }
}
