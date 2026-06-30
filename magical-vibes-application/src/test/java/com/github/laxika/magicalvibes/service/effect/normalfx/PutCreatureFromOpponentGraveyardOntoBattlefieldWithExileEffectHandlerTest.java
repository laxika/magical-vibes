package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandler;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandlerTest {

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
    private GraveyardService graveyardService;
    @InjectMocks
    private GraveyardReturnSupport support;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandler putCreatureFromOpponentGraveyardHandler;

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
        putCreatureFromOpponentGraveyardHandler = new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffectHandler(
                battlefieldEntryService, gameBroadcastService, support);

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
            @DisplayName("Puts creature from opponent's graveyard onto battlefield with haste")
            void putsCreatureWithHaste() {
                Card target = createCard("Grizzly Bears");

                PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect effect =
                        new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Gruesome Encore"),
                        player1Id, "Gruesome Encore", List.of(effect), 0,
                        target.getId(), null);

                when(gameQueryService.findCardInGraveyardById(gd, target.getId())).thenReturn(target);
                when(gameQueryService.findGraveyardOwnerById(gd, target.getId())).thenReturn(player2Id);
                when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());
                lenient().when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);

                putCreatureFromOpponentGraveyardHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removeCardFromGraveyardById(gd, target.getId());
                verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id),
                        argThat(p -> p.getGrantedKeywords().contains(Keyword.HASTE)
                                && p.isExileIfLeavesBattlefield()),
                        eq(Set.of()));
                assertThat(gd.pendingTokenExilesAtEndStep).isNotEmpty();
                assertThat(gd.stolenCreatures).isNotEmpty();
                assertThat(gd.permanentControlStolenCreatures).isNotEmpty();
            }

            @Test
            @DisplayName("Fizzles if target card is no longer in graveyard")
            void fizzlesIfTargetGone() {
                UUID targetId = UUID.randomUUID();

                PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect effect =
                        new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Gruesome Encore"),
                        player1Id, "Gruesome Encore", List.of(effect), 0,
                        targetId, null);

                when(gameQueryService.findCardInGraveyardById(gd, targetId)).thenReturn(null);

                putCreatureFromOpponentGraveyardHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("fizzles")));
                verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(
                        any(), any(), any(Permanent.class), any());
            }

            @Test
            @DisplayName("Fizzles if target is in controller's own graveyard")
            void fizzlesIfTargetInOwnGraveyard() {
                Card target = createCard("Grizzly Bears");

                PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect effect =
                        new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect();
                StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Gruesome Encore"),
                        player1Id, "Gruesome Encore", List.of(effect), 0,
                        target.getId(), null);

                when(gameQueryService.findCardInGraveyardById(gd, target.getId())).thenReturn(target);
                when(gameQueryService.findGraveyardOwnerById(gd, target.getId())).thenReturn(player1Id);

                putCreatureFromOpponentGraveyardHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        argThat(msg -> msg.contains("fizzles") && msg.contains("not in opponent's graveyard")));
                verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(
                        any(), any(), any(Permanent.class), any());
            }
}
