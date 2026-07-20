package com.github.laxika.magicalvibes.service.effect.normalfx;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit coverage for the collapsed {@link SacrificePermanentsEffectHandler}. Consolidates the former
 * {@code SacrificeCreatureEffectHandlerTest} and {@code EachOpponentSacrificesCreatureEffectHandlerTest}
 * (the creature single-select mechanic) and adds coverage for the multi-permanent mechanic, nested by
 * recipient. Behavioural end-to-end coverage lives in the card tests (Cruel Edict, Geth's Verdict,
 * Grave Pact, Storm Fleet Arsonist, Yawning Fissure, Destructive Force, The Eldest Reborn).
 */
@ExtendWith(MockitoExtension.class)
class SacrificePermanentsEffectHandlerTest {

    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private GraveyardService graveyardService;
    @Mock private DamagePreventionService damagePreventionService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private LifeSupport lifeSupport;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @InjectMocks private DestructionSupport destructionSupport;

    private SacrificePermanentsEffectHandler handler;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

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
        handler = new SacrificePermanentsEffectHandler(destructionSupport, gameBroadcastService,
                gameQueryService, predicateEvaluationService, playerInputService, amountEvaluationService);
    }

    // ===== Helpers =====

    private Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private Permanent addPermanent(UUID playerId, String name, CardType type) {
        Permanent permanent = new Permanent(createCard(name, type));
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    private StackEntry entry(UUID controllerId, UUID targetId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Source", CardType.SORCERY),
                controllerId, "Source", List.of(), 0, targetId, null);
    }

    private SacrificePermanentsEffect creatureSac(SacrificeRecipient recipient) {
        return new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), recipient);
    }

    private void stubCount(int count) {
        lenient().when(amountEvaluationService.evaluate(any(GameData.class), any(DynamicAmount.class),
                any(AmountContext.class))).thenReturn(count);
    }

    // ===================================================================================
    // TARGET_PLAYER — creature single-select mechanic (former SacrificeCreatureEffectHandler)
    // ===================================================================================

    @Nested
    class TargetPlayerCreature {

        @Test
        @DisplayName("Opponent with one creature sacrifices it automatically")
        void autoSacrificesOnlyCreature() {
            Permanent bears = addPermanent(player2Id, "Grizzly Bears", CardType.CREATURE);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            handler.resolve(gd, entry(player1Id, player2Id), creatureSac(SacrificeRecipient.TARGET_PLAYER));

            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Player2 sacrifices Grizzly Bears.")));
        }

        @Test
        @DisplayName("Opponent with multiple creatures is prompted with SacrificeCreature single-select")
        void promptsChoiceWithMultipleCreatures() {
            Permanent bears = addPermanent(player2Id, "Grizzly Bears", CardType.CREATURE);
            Permanent spider = addPermanent(player2Id, "Giant Spider", CardType.CREATURE);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.isCreature(gd, spider)).thenReturn(true);

            handler.resolve(gd, entry(player1Id, player2Id), creatureSac(SacrificeRecipient.TARGET_PLAYER));

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player2Id), any(), anyString());
        }

        @Test
        @DisplayName("No effect when opponent has no creatures")
        void noEffectWithNoCreatures() {
            handler.resolve(gd, entry(player1Id, player2Id), creatureSac(SacrificeRecipient.TARGET_PLAYER));

            verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Player2 has no creatures to sacrifice."));
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("No effect when there is no valid target player")
        void noEffectWithoutTargetPlayer() {
            handler.resolve(gd, entry(player1Id, null), creatureSac(SacrificeRecipient.TARGET_PLAYER));

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), any(GameLogEntry.class));
        }
    }

    // ===================================================================================
    // TARGET_PLAYER — multi-permanent mechanic (former TargetPlayerSacrificesPermanentsEffect)
    // ===================================================================================

    @Nested
    class TargetPlayerPermanents {

        private SacrificePermanentsEffect landSac() {
            return new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                    SacrificeRecipient.TARGET_PLAYER);
        }

        @Test
        @DisplayName("Sacrifices the only matching permanent automatically")
        void autoSacrificesOnlyMatch() {
            Permanent forest = addPermanent(player2Id, "Forest", CardType.LAND);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(forest),
                    any(PermanentPredicate.class))).thenReturn(true);

            handler.resolve(gd, entry(player1Id, player2Id), landSac());

            verify(permanentRemovalService).removePermanentToGraveyard(gd, forest);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Player2 sacrifices Forest.")));
        }

        @Test
        @DisplayName("Prompts multi-permanent ForcedSacrifice choice when more matches than count")
        void promptsForcedSacrificeChoice() {
            Permanent forest = addPermanent(player2Id, "Forest", CardType.LAND);
            Permanent island = addPermanent(player2Id, "Island", CardType.LAND);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), any(Permanent.class),
                    any(PermanentPredicate.class))).thenReturn(true);

            handler.resolve(gd, entry(player1Id, player2Id), landSac());

            verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player2Id),
                    any(), eq(1), any(MultiPermanentChoiceContext.ForcedSacrifice.class), anyString());
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("Logs when the target player has no permanents at all")
        void logsWhenNoPermanents() {
            stubCount(1);

            handler.resolve(gd, entry(player1Id, player2Id), landSac());

            verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Player2 has no permanents to sacrifice."));
        }

        @Test
        @DisplayName("Logs when the target player has no matching permanents")
        void logsWhenNoMatch() {
            Permanent bears = addPermanent(player2Id, "Grizzly Bears", CardType.CREATURE);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(bears),
                    any(PermanentPredicate.class))).thenReturn(false);

            handler.resolve(gd, entry(player1Id, player2Id), landSac());

            verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Player2 has no matching permanents to sacrifice."));
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }
    }

    // ===================================================================================
    // CONTROLLER — creature single-select mechanic (former ControllerSacrificesCreatureEffect)
    // ===================================================================================

    @Nested
    class ControllerCreature {

        @Test
        @DisplayName("Controller with one creature sacrifices it automatically")
        void controllerSacrificesOnlyCreature() {
            Permanent bears = addPermanent(player1Id, "Grizzly Bears", CardType.CREATURE);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            handler.resolve(gd, entry(player1Id, null), creatureSac(SacrificeRecipient.CONTROLLER));

            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Player1 sacrifices Grizzly Bears.")));
        }
    }

    // ===================================================================================
    // EACH_OPPONENT — creature single-select mechanic (former EachOpponentSacrificesCreatureEffect)
    // ===================================================================================

    @Nested
    class EachOpponentCreature {

        @Test
        @DisplayName("Each opponent with one creature sacrifices it")
        void opponentSacrificesOnlyCreature() {
            Permanent bears = addPermanent(player2Id, "Grizzly Bears", CardType.CREATURE);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            handler.resolve(gd, entry(player1Id, null), creatureSac(SacrificeRecipient.EACH_OPPONENT));

            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Player2 sacrifices Grizzly Bears.")));
        }

        @Test
        @DisplayName("No effect when opponent has no creatures; controller is not asked")
        void noEffectWhenOpponentHasNoCreatures() {
            handler.resolve(gd, entry(player1Id, null), creatureSac(SacrificeRecipient.EACH_OPPONENT));

            verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Player2 has no creatures to sacrifice."));
            verify(gameBroadcastService, never()).logAndBroadcast(gd, GameLog.text("Player1 has no creatures to sacrifice."));
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }
    }

    // ===================================================================================
    // EACH_PLAYER — multi-permanent APNAP mechanic (former EachPlayerSacrificesPermanentsEffect)
    // ===================================================================================

    @Nested
    class EachPlayerPermanents {

        private SacrificePermanentsEffect landSac(SacrificeRecipient recipient) {
            return new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), recipient);
        }

        @Test
        @DisplayName("Each player auto-sacrifices when matches do not exceed the count (simultaneous)")
        void eachPlayerAutoSacrificesSimultaneously() {
            Permanent forest = addPermanent(player1Id, "Forest", CardType.LAND);
            Permanent island = addPermanent(player2Id, "Island", CardType.LAND);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), any(Permanent.class),
                    any(PermanentPredicate.class))).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            when(gameQueryService.findPermanentById(gd, island.getId())).thenReturn(island);
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player1Id);
            when(gameQueryService.findPermanentController(gd, island.getId())).thenReturn(player2Id);

            handler.resolve(gd, entry(player1Id, null), landSac(SacrificeRecipient.EACH_PLAYER));

            verify(permanentRemovalService).removePermanentToGraveyard(gd, forest);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, island);
        }

        @Test
        @DisplayName("Queues a forced-sacrifice choice when a player has more matches than the count")
        void queuesChoiceWhenMoreMatchesThanCount() {
            addPermanent(player2Id, "Forest", CardType.LAND);
            addPermanent(player2Id, "Island", CardType.LAND);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), any(Permanent.class),
                    any(PermanentPredicate.class))).thenReturn(true);

            handler.resolve(gd, entry(player1Id, null), landSac(SacrificeRecipient.EACH_PLAYER));

            verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player2Id), any(), eq(1),
                    any(MultiPermanentChoiceContext.ForcedSacrifice.class), anyString());
        }
    }

    // ===================================================================================
    // TARGET_PLAYER_OR_PERMANENT_CONTROLLER — piggybacks on a companion player-or-planeswalker
    // damage effect's targetId (Nicol Bolas, Planeswalker -9)
    // ===================================================================================

    @Nested
    class TargetPlayerOrPermanentController {

        private SacrificePermanentsEffect anyPermanentSac() {
            return new SacrificePermanentsEffect(1, new PermanentTruePredicate(),
                    SacrificeRecipient.TARGET_PLAYER_OR_PERMANENT_CONTROLLER);
        }

        @Test
        @DisplayName("When targetId is a player, that player sacrifices")
        void targetedPlayerSacrifices() {
            Permanent forest = addPermanent(player2Id, "Forest", CardType.LAND);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(forest),
                    any(PermanentPredicate.class))).thenReturn(true);

            handler.resolve(gd, entry(player1Id, player2Id), anyPermanentSac());

            verify(permanentRemovalService).removePermanentToGraveyard(gd, forest);
            verify(gameBroadcastService).logAndBroadcast(gd, GameLog.playerSacrifices("Player2", forest.getCard()));
        }

        @Test
        @DisplayName("When targetId is a planeswalker, its controller sacrifices")
        void targetedPlaneswalkerControllerSacrifices() {
            UUID planeswalkerId = UUID.randomUUID();
            when(gameQueryService.findPermanentController(gd, planeswalkerId)).thenReturn(player2Id);
            Permanent forest = addPermanent(player2Id, "Forest", CardType.LAND);
            stubCount(1);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(forest),
                    any(PermanentPredicate.class))).thenReturn(true);

            handler.resolve(gd, entry(player1Id, planeswalkerId), anyPermanentSac());

            verify(permanentRemovalService).removePermanentToGraveyard(gd, forest);
            verify(gameBroadcastService).logAndBroadcast(gd, GameLog.playerSacrifices("Player2", forest.getCard()));
        }

        @Test
        @DisplayName("No effect when the targeted planeswalker has no controller")
        void noEffectWhenControllerMissing() {
            UUID planeswalkerId = UUID.randomUUID();
            when(gameQueryService.findPermanentController(gd, planeswalkerId)).thenReturn(null);

            handler.resolve(gd, entry(player1Id, planeswalkerId), anyPermanentSac());

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }
    }
}
