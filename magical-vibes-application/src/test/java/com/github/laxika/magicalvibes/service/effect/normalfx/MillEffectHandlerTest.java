package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MillEffectHandlerTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private MillEffectHandler handler;

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
        handler = new MillEffectHandler(graveyardService, gameQueryService,
                new AmountEvaluationService(predicateEvaluationService, gameQueryService));
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    @Nested
    @DisplayName("CONTROLLER")
    class Controller {

        @Test
        @DisplayName("Mills the controller for a fixed count")
        void millsControllerFixedCount() {
            MillEffect effect = new MillEffect(4, MillRecipient.CONTROLLER);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Armored Skaab"),
                    player1Id, "Armored Skaab", List.of(effect));

            handler.resolve(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player1Id, 4);
        }
    }

    @Nested
    @DisplayName("TARGET_PLAYER")
    class TargetPlayer {

        @Test
        @DisplayName("Mills the target player for a fixed count")
        void millsTargetPlayerFixedCount() {
            MillEffect effect = new MillEffect(3, MillRecipient.TARGET_PLAYER);
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Thought Scour"),
                    player1Id, "Thought Scour", List.of(effect), 0, player2Id, null);

            handler.resolve(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 3);
        }

        @Test
        @DisplayName("Mills the target player by the number of cards in their hand (Dreamborn Muse)")
        void millsTargetPlayerByHandSize() {
            gd.playerHands.get(player1Id).addAll(List.of(
                    createCard("Bear1"), createCard("Bear2"), createCard("Bear3")));

            MillEffect effect = new MillEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                    MillRecipient.TARGET_PLAYER);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                    player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

            handler.resolve(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player1Id, 3);
        }

        @Test
        @DisplayName("Mills zero when the target player's hand is empty (Dreamborn Muse)")
        void millsZeroWhenHandEmpty() {
            MillEffect effect = new MillEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                    MillRecipient.TARGET_PLAYER);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Dreamborn Muse"),
                    player1Id, "Dreamborn Muse", List.of(effect), 0, player1Id, null);

            handler.resolve(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player1Id, 0);
        }
    }

    @Nested
    @DisplayName("EACH_OPPONENT")
    class EachOpponent {

        @Test
        @DisplayName("Mills each opponent but not the controller")
        void millsEachOpponentNotController() {
            MillEffect effect = new MillEffect(2, MillRecipient.EACH_OPPONENT);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Undead Alchemist"),
                    player1Id, "Undead Alchemist", List.of(effect));

            handler.resolve(gd, entry, effect);

            verify(graveyardService).resolveMillPlayer(gd, player2Id, 2);
            verify(graveyardService, never()).resolveMillPlayer(eq(gd), eq(player1Id), any(int.class));
        }
    }
}
