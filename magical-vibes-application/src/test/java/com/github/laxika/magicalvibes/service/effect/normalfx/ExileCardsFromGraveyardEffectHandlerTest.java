package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExileCardsFromGraveyardEffectHandlerTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private LegendRuleService legendRuleService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private ExileService exileService;
    @Mock
    private GraveyardService graveyardService;
    @InjectMocks
    private GraveyardReturnSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExileCardsFromGraveyardEffectHandler exileCardsFromGraveyardHandler;

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
        exileCardsFromGraveyardHandler = new ExileCardsFromGraveyardEffectHandler(
                gameQueryService, gameLogService, lifeSupport, support);

    }

    private static Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        // =========================================================================
        // describeFilter A?€�t static utility method
        // =========================================================================

    @Test
            @DisplayName("Exiles targeted cards still in graveyards")
            void exilesTargetedCards() {
                Card creature = createCard("Grizzly Bears");
                Card artifact = createCard("Leonin Scimitar");
                gd.playerGraveyards.get(player1Id).add(creature);
                gd.playerGraveyards.get(player2Id).add(artifact);

                ExileCardsFromGraveyardEffect effect = new ExileCardsFromGraveyardEffect(2, 0);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Scavenging Ooze"),
                        player1Id, "Scavenging Ooze", List.of(effect),
                        List.of(creature.getId(), artifact.getId()));

                when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);
                when(gameQueryService.findCardInGraveyardById(gd, artifact.getId())).thenReturn(artifact);

                exileCardsFromGraveyardHandler.resolve(gd, entry, effect);

                verify(exileService).exileCard(gd, player1Id, creature);
                verify(exileService).exileCard(gd, player2Id, artifact);
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("exiles") && logEntry.plainText().contains("Grizzly Bears")
                                && logEntry.plainText().contains("Leonin Scimitar")));
            }

            @Test
            @DisplayName("Gains life after exiling when lifeGain is positive")
            void gainsLifeAfterExiling() {
                Card creature = createCard("Grizzly Bears");
                gd.playerGraveyards.get(player1Id).add(creature);

                ExileCardsFromGraveyardEffect effect = new ExileCardsFromGraveyardEffect(1, 3);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Scavenging Ooze"),
                        player1Id, "Scavenging Ooze", List.of(effect),
                        List.of(creature.getId()));

                when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);

                exileCardsFromGraveyardHandler.resolve(gd, entry, effect);

                verify(exileService).exileCard(gd, player1Id, creature);
                verify(lifeSupport).applyGainLife(gd, player1Id, 3);
            }

            @Test
            @DisplayName("Per-card life gain scales with the number of cards actually exiled, and the "
                    + "source assigns no combat damage")
            void gainsLifePerExiledCardAndPreventsCombatDamage() {
                Card creature = createCard("Grizzly Bears");
                Card other = createCard("Hill Giant");
                UUID goneCardId = UUID.randomUUID();
                gd.playerGraveyards.get(player2Id).add(creature);
                gd.playerGraveyards.get(player2Id).add(other);
                UUID attackerId = UUID.randomUUID();

                ExileCardsFromGraveyardEffect effect =
                        new ExileCardsFromGraveyardEffect(2, 1, true, null, true);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Rysorian Badger"),
                        player1Id, "Rysorian Badger", List.of(effect), 0, null, attackerId, Map.of(), null,
                        List.of(creature.getId(), other.getId(), goneCardId), List.of());

                when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);
                when(gameQueryService.findCardInGraveyardById(gd, other.getId())).thenReturn(other);
                when(gameQueryService.findCardInGraveyardById(gd, goneCardId)).thenReturn(null);

                exileCardsFromGraveyardHandler.resolve(gd, entry, effect);

                verify(lifeSupport).applyGainLife(gd, player1Id, 2);
                assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attackerId);
            }

            @Test
            @DisplayName("Nothing exiled means no life gain and combat damage is still dealt")
            void noExileMeansNoLifeGainAndNoCombatDamagePrevention() {
                UUID goneCardId = UUID.randomUUID();
                UUID attackerId = UUID.randomUUID();

                ExileCardsFromGraveyardEffect effect =
                        new ExileCardsFromGraveyardEffect(2, 1, true, null, true);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Rysorian Badger"),
                        player1Id, "Rysorian Badger", List.of(effect), 0, null, attackerId, Map.of(), null,
                        List.of(goneCardId), List.of());

                when(gameQueryService.findCardInGraveyardById(gd, goneCardId)).thenReturn(null);

                exileCardsFromGraveyardHandler.resolve(gd, entry, effect);

                verify(lifeSupport, never()).applyGainLife(any(), any(), anyInt());
                assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
            }

            @Test
            @DisplayName("Skips cards no longer in graveyard")
            void skipsCardsNoLongerInGraveyard() {
                UUID goneCardId = UUID.randomUUID();

                ExileCardsFromGraveyardEffect effect = new ExileCardsFromGraveyardEffect(1, 0);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Scavenging Ooze"),
                        player1Id, "Scavenging Ooze", List.of(effect),
                        List.of(goneCardId));

                when(gameQueryService.findCardInGraveyardById(gd, goneCardId)).thenReturn(null);

                exileCardsFromGraveyardHandler.resolve(gd, entry, effect);

                verify(exileService, never()).exileCard(any(), any(), any());
                verify(gameLogService, never()).append(any(), any(GameLogEntry.class));
            }
}
