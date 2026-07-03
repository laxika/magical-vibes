package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryRevealSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler;
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
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
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
    private LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler;

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
        lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler = new LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler(gameQueryService, predicateEvaluationService, gameBroadcastService, sessionManager, cardViewFactory);

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

    private LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect createBottomRandomEffect(CardPredicate predicate) {
                return new LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(predicate, null, true);
            }

            @Test
            @DisplayName("Empty library does nothing")
            void emptyLibraryDoesNothing() {
                var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                        player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

                lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isNull();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(s -> s.contains("library is empty")));
            }

            @Test
            @DisplayName("X=0 reveals zero cards and does nothing")
            void xZeroDoesNothing() {
                gd.playerDecks.get(player1Id).add(createCard("Some Card"));

                var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                        player1Id, "Gishath's triggered ability", List.of(effect), 0, null, null);

                lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isNull();
                assertThat(gd.playerDecks.get(player1Id)).hasSize(1); // untouched
            }

            @Test
            @DisplayName("No matching cards puts all on bottom immediately")
            void noMatchingCardsPutsAllOnBottom() {
                Card land = createCard("Forest", CardType.LAND);
                Card instant = createCard("Shock", CardType.INSTANT);
                gd.playerDecks.get(player1Id).add(land);
                gd.playerDecks.get(player1Id).add(instant);

                when(predicateEvaluationService.matchesCardPredicate(any(), any(), any(), any(), any())).thenReturn(false);

                var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                        player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

                lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isNull();
                // Cards should be back on the bottom of the library
                assertThat(gd.playerDecks.get(player1Id)).hasSize(2);
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(s -> s.contains("no eligible cards")));
            }

            @Test
            @DisplayName("Matching cards enter LIBRARY_REVEAL_CHOICE state with randomRemainingToBottom")
            void matchingCardsEnterRevealChoiceState() {
                stubCardViewFactory();
                Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
                Card land = createCard("Forest", CardType.LAND);
                Card instant = createCard("Shock", CardType.INSTANT);
                gd.playerDecks.get(player1Id).add(dino);
                gd.playerDecks.get(player1Id).add(land);
                gd.playerDecks.get(player1Id).add(instant);

                when(predicateEvaluationService.matchesCardPredicate(eq(dino), any(), any(), any(), any())).thenReturn(true);
                when(predicateEvaluationService.matchesCardPredicate(eq(land), any(), any(), any(), any())).thenReturn(false);
                when(predicateEvaluationService.matchesCardPredicate(eq(instant), any(), any(), any(), any())).thenReturn(false);

                var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                        player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

                lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
                assertThat(gd.interaction.libraryRevealChoiceContext()).isNotNull();
                assertThat(gd.interaction.libraryRevealChoiceContext().randomRemainingToBottom()).isTrue();
                assertThat(gd.interaction.libraryRevealChoiceContext().validCardIds()).containsExactly(dino.getId());
                verify(sessionManager).sendToPlayer(eq(player1Id), any());
            }

            @Test
            @DisplayName("Reveals fewer cards when library is smaller than X")
            void fewerCardsThanX() {
                stubCardViewFactory();
                Card dino = createCard("Colossal Dreadmaw", CardType.CREATURE);
                gd.playerDecks.get(player1Id).add(dino);

                when(predicateEvaluationService.matchesCardPredicate(eq(dino), any(), any(), any(), any())).thenReturn(true);

                var effect = createBottomRandomEffect(new CardTypePredicate(CardType.CREATURE));
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Gishath, Sun's Avatar"),
                        player1Id, "Gishath's triggered ability", List.of(effect), 7, null, null);

                lookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffectHandler.resolve(gd, entry, effect);

                assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
                // Only 1 card was in library, so only 1 revealed
                assertThat(gd.interaction.libraryRevealChoiceContext().allCards()).hasSize(1);
            }
}
