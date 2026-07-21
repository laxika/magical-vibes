package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ExileGraveyardCardsEffectHandler}. Consolidated from the old
 * {@code ExileTargetPlayerGraveyardEffectHandlerTest}; the remaining scopes are proven by the
 * behavioral card tests (Cemetery Reaper / Thraben Heretic / Conversion Chamber /
 * Ascendant Dustspeaker / Glorious Decay / Purify the Grave for single-card, Deadeye Tracker for
 * opponent-multi, Sentinel Totem for all-players, Phyrexian Scriptures for all-opponents,
 * Curse of Oblivion for own).
 */
@ExtendWith(MockitoExtension.class)
class ExileGraveyardCardsEffectHandlerTest {

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private ExileService exileService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private GraveyardReturnSupport graveyardReturnSupport;
    @Mock
    private TriggerCollectionService triggerCollectionService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileGraveyardCardsEffectHandler handler;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));

        GraveyardService graveyardService = new GraveyardService(
                gameQueryService, gameBroadcastService, exileService, predicateEvaluationService, triggerCollectionService);
        handler = new ExileGraveyardCardsEffectHandler(gameQueryService, gameBroadcastService, exileService,
                permanentRemovalService, predicateEvaluationService, graveyardReturnSupport, graveyardService);
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    @Nested
    @DisplayName("TARGET_PLAYER_ENTIRE — exile target player's whole graveyard")
    class TargetPlayerEntire {

        @Test
        @DisplayName("Exiles all cards from target player's graveyard")
        void exilesEntireGraveyard() {
            Card creature = createCard("Grizzly Bears");
            Card artifact = createCard("Leonin Scimitar");
            gd.playerGraveyards.get(player2Id).addAll(List.of(creature, artifact));

            ExileGraveyardCardsEffect effect = new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Nihil Spellbomb"),
                    player1Id, "Nihil Spellbomb", List.of(effect), 0,
                    player2Id, null);

            handler.resolve(gd, entry, effect);

            assertThat(gd.playerGraveyards.get(player2Id)).isEmpty();
            assertThat(gd.getPlayerExiledCards(player2Id))
                    .extracting(Card::getName)
                    .containsExactlyInAnyOrder("Grizzly Bears", "Leonin Scimitar");
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("exiled") && logEntry.plainText().contains("2 cards")));
            // Two cards leaving the graveyard in one event fires a single leave-graveyard trigger
            verify(triggerCollectionService).checkControllerCardsLeaveGraveyardTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Logs message when targeting empty graveyard")
        void logsMessageForEmptyGraveyard() {
            ExileGraveyardCardsEffect effect = new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Nihil Spellbomb"),
                    player1Id, "Nihil Spellbomb", List.of(effect), 0,
                    player2Id, null);

            handler.resolve(gd, entry, effect);

            assertThat(gd.getPlayerExiledCards(player2Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                    logEntry.plainText().contains("already empty")));
            // No cards left the graveyard, so no trigger fires
            verify(triggerCollectionService, never()).checkControllerCardsLeaveGraveyardTriggers(eq(gd), any());
        }
    }

    @Nested
    @DisplayName("EACH_OPPONENT_KEEP — each opponent keeps N cards, exiles the rest")
    class EachOpponentKeep {

        @Test
        @DisplayName("Prompts opponent to exile (size - keepCount) cards when graveyard exceeds keep count")
        void promptsExileChoiceWhenGraveyardExceedsKeepCount() {
            gd.playerGraveyards.get(player2Id).addAll(List.of(
                    createCard("A"), createCard("B"), createCard("C"), createCard("D")));

            ExileGraveyardCardsEffect effect = new ExileGraveyardCardsEffect(2, GraveyardExileScope.EACH_OPPONENT_KEEP);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Watchers of the Dead"),
                    player1Id, "Watchers of the Dead", List.of(effect), 0, null, null);

            handler.resolve(gd, entry, effect);

            // 4 cards, keep 2 -> opponent chooses 2 to exile
            verify(graveyardReturnSupport).beginGraveyardExileChoice(gd, player2Id, 2);
        }

        @Test
        @DisplayName("Exiles nothing and never prompts when opponent has keepCount or fewer cards")
        void keepsEverythingWhenAtOrBelowKeepCount() {
            gd.playerGraveyards.get(player2Id).addAll(List.of(createCard("A"), createCard("B")));

            ExileGraveyardCardsEffect effect = new ExileGraveyardCardsEffect(2, GraveyardExileScope.EACH_OPPONENT_KEEP);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Watchers of the Dead"),
                    player1Id, "Watchers of the Dead", List.of(effect), 0, null, null);

            handler.resolve(gd, entry, effect);

            verify(graveyardReturnSupport, never()).beginGraveyardExileChoice(any(), any(), anyInt());
            assertThat(gd.getPlayerExiledCards(player2Id)).isEmpty();
        }

        @Test
        @DisplayName("Does not touch the controller's own graveyard")
        void doesNotTouchControllerGraveyard() {
            gd.playerGraveyards.get(player1Id).addAll(List.of(
                    createCard("A"), createCard("B"), createCard("C"), createCard("D")));

            ExileGraveyardCardsEffect effect = new ExileGraveyardCardsEffect(2, GraveyardExileScope.EACH_OPPONENT_KEEP);
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Watchers of the Dead"),
                    player1Id, "Watchers of the Dead", List.of(effect), 0, null, null);

            handler.resolve(gd, entry, effect);

            // Controller (player1) is skipped; the only opponent (player2) has an empty graveyard.
            verify(graveyardReturnSupport, never()).beginGraveyardExileChoice(any(), any(), anyInt());
        }
    }
}
