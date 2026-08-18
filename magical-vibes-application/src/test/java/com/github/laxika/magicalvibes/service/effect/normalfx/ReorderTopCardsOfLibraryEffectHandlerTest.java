package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
class ReorderTopCardsOfLibraryEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AmountEvaluationService amountEvaluationService;
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
    private PredicateEvaluationService predicateEvaluationService;
    private LibraryRevealSupport libraryRevealSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ReorderTopCardsOfLibraryEffectHandler reorderTopCardsOfLibraryEffectHandler;
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
        reorderTopCardsOfLibraryEffectHandler = new ReorderTopCardsOfLibraryEffectHandler(gameLogService,
                InteractionRegistryTestSupport.registryFor(sessionManager, cardViewFactory, gameLogService),
                gameQueryService, amountEvaluationService);
        lenient().when(amountEvaluationService.evaluate(
                        any(GameData.class), argThat(amount -> amount instanceof Fixed), any()))
                .thenAnswer(invocation -> ((Fixed) invocation.getArgument(1)).value());

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
            @DisplayName("Evaluates a dynamic reorder count at resolution")
            void evaluatesDynamicCount() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("First"));
                gd.playerDecks.get(player1Id).add(createCard("Second"));
                gd.playerDecks.get(player1Id).add(createCard("Third"));
                when(amountEvaluationService.evaluate(any(GameData.class), any(), any())).thenReturn(3);

                ReorderTopCardsOfLibraryEffect effect =
                        new ReorderTopCardsOfLibraryEffect(new CardsInHand(CountScope.CONTROLLER));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                        player1Id, "Sage Owl", List.of(effect));

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                        .hasSize(3);
            }

    @Test
            @DisplayName("Empty library skips reorder")
            void emptyLibrarySkipsReorder() {
                ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                        player1Id, "Sage Owl", List.of(effect));

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("library is empty")));
                assertThat(gd.interaction.activeInteraction()).isNull();
            }

            @Test
            @DisplayName("Single card skips reorder prompt")
            void singleCardSkipsReorder() {
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));

                ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                        player1Id, "Sage Owl", List.of(effect));

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("looks at the top card")));
                assertThat(gd.interaction.activeInteraction()).isNull();
            }

            @Test
            @DisplayName("Multiple cards enters LIBRARY_REORDER state")
            void multipleCardsEntersReorderState() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

                ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Sage Owl"),
                        player1Id, "Sage Owl", List.of(effect));

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId()).isEqualTo(player1Id);
                verifyNoInteractions(sessionManager);
            }

            @Test
            @DisplayName("Controller owner reorders the controller's own library even when the entry carries a target")
            void controllerOwnerIgnoresTargetId() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));

                ReorderTopCardsOfLibraryEffect effect = new ReorderTopCardsOfLibraryEffect(2);
                StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Discombobulate"),
                        player1Id, "Discombobulate", List.of(effect), 0,
                        player2Id, null);

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                PendingInteraction.LibraryReorder reorder =
                        gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
                assertThat(reorder.deckOwnerId()).isEqualTo(player1Id);
                assertThat(gd.playerDecks.get(player2Id)).isEmpty();
            }

            @Test
            @DisplayName("Target-player owner takes the cards from the target's library, decided by the controller")
            void targetPlayerOwnerReadsTargetLibrary() {
                stubCardViewFactory();
                gd.playerDecks.get(player2Id).add(createCard("Grizzly Bears"));
                gd.playerDecks.get(player2Id).add(createCard("Llanowar Elves"));

                ReorderTopCardsOfLibraryEffect effect =
                        new ReorderTopCardsOfLibraryEffect(2, LibraryOwner.TARGET_PLAYER);
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Portent"),
                        player1Id, "Portent", List.of(effect), 0,
                        player2Id, null);

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                PendingInteraction.LibraryReorder reorder =
                        gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
                assertThat(reorder.playerId()).isEqualTo(player1Id);
                assertThat(reorder.deckOwnerId()).isEqualTo(player2Id);
                assertThat(reorder.cards()).hasSize(2);
                assertThat(gd.playerDecks.get(player2Id)).isEmpty();
            }

            @Test
            @DisplayName("Dynamic count uses the stack entry's X value")
            void dynamicCountUsesStackEntryXValue() {
                stubCardViewFactory();
                gd.playerDecks.get(player1Id).add(createCard("Grizzly Bears"));
                gd.playerDecks.get(player1Id).add(createCard("Llanowar Elves"));
                gd.playerDecks.get(player1Id).add(createCard("Serra Angel"));

                ReorderTopCardsOfLibraryEffect effect =
                        new ReorderTopCardsOfLibraryEffect(new XValue());
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Soothsaying"),
                        player1Id, "Soothsaying", List.of(effect), 3);
                when(amountEvaluationService.evaluate(eq(gd), eq(new XValue()),
                        argThat(context -> context.xValue() == 3))).thenReturn(3);

                reorderTopCardsOfLibraryEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                        .hasSize(3);
            }
}
