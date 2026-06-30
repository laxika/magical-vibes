package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPlayerGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExileTargetPlayerGraveyardEffectHandlerTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private LegendRuleService legendRuleService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private ExileService exileService;
    @Mock
    private TriggerCollectionService triggerCollectionService;
    @InjectMocks
    private GraveyardReturnSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileTargetPlayerGraveyardEffectHandler exileTargetPlayerGraveyardHandler;

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
        GraveyardService graveyardService = new GraveyardService(
                gameQueryService, gameBroadcastService, exileService, triggerCollectionService);
        exileTargetPlayerGraveyardHandler =
                new ExileTargetPlayerGraveyardEffectHandler(gameBroadcastService, graveyardService);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // describeFilter â€” static utility method
        // =========================================================================

    @Test
            @DisplayName("Exiles all cards from target player's graveyard")
            void exilesEntireGraveyard() {
                Card creature = createCard("Grizzly Bears");
                Card artifact = createCard("Leonin Scimitar");
                gd.playerGraveyards.get(player2Id).addAll(List.of(creature, artifact));

                ExileTargetPlayerGraveyardEffect effect = new ExileTargetPlayerGraveyardEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Nihil Spellbomb"),
                        player1Id, "Nihil Spellbomb", List.of(effect), 0,
                        player2Id, null);

                exileTargetPlayerGraveyardHandler.resolve(gd, entry, effect);

                assertThat(gd.playerGraveyards.get(player2Id)).isEmpty();
                assertThat(gd.getPlayerExiledCards(player2Id))
                        .extracting(Card::getName)
                        .containsExactlyInAnyOrder("Grizzly Bears", "Leonin Scimitar");
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("exiled") && msg.contains("2 cards")));
                // Two cards leaving the graveyard in one event fires a single leave-graveyard trigger
                verify(triggerCollectionService).checkControllerCardsLeaveGraveyardTriggers(gd, player2Id);
            }

            @Test
            @DisplayName("Logs message when targeting empty graveyard")
            void logsMessageForEmptyGraveyard() {
                ExileTargetPlayerGraveyardEffect effect = new ExileTargetPlayerGraveyardEffect();
                StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Nihil Spellbomb"),
                        player1Id, "Nihil Spellbomb", List.of(effect), 0,
                        player2Id, null);

                exileTargetPlayerGraveyardHandler.resolve(gd, entry, effect);

                assertThat(gd.getPlayerExiledCards(player2Id)).isEmpty();
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("already empty")));
                // No cards left the graveyard, so no trigger fires
                verify(triggerCollectionService, never()).checkControllerCardsLeaveGraveyardTriggers(eq(gd), any());
            }
}
