package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCardsFromGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPlayerGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnCardFromGraveyardEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnTargetCardsFromGraveyardToHandEffectHandler;
import com.github.laxika.magicalvibes.service.exile.ExileService;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LifeSupport lifeSupport;
    @Mock
    private ExileService exileService;
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
                gameQueryService, gameBroadcastService, lifeSupport, support);

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
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("exiles") && msg.contains("Grizzly Bears")
                                && msg.contains("Leonin Scimitar")));
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
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
            }
}
