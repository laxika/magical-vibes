package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerExilesTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MirrorOfFateEffect;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerExilesTopCardsToSourceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileAllPermanentsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFromHandToImprintEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExilePermanentDamagedPlayerControlsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSelfAndReturnAtEndStepEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentAndImprintEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentAndReturnAtEndStepEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentAndTrackWithSourceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileTargetPermanentUntilSourceLeavesEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ImprintDyingCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.KnowledgePoolExileAndCastEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.MirrorOfFateEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.OmenMachineDrawStepEffectHandler;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExilePermanentDamagedPlayerControlsEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private ExileService exileService;
    @InjectMocks
    private ExileSupport exileSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ExilePermanentDamagedPlayerControlsEffectHandler exilePermanentDamagedPlayerControlsHandler;

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
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        exilePermanentDamagedPlayerControlsHandler = new ExilePermanentDamagedPlayerControlsEffectHandler(gameQueryService, gameBroadcastService, playerInputService);

    }

    // ===== Helper methods =====

        private Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.ARTIFACT);
            return card;
        }

        private Card createCreatureCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.setPower(2);
            card.setToughness(2);
            return card;
        }

        private Card createSorceryCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.SORCERY);
            return card;
        }

        private Card createLandCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.LAND);
            return card;
        }

        private Permanent addPermanent(UUID playerId, Card card) {
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(playerId).add(perm);
            return perm;
        }

        private StackEntry createSingleTargetEntry(Card sourceCard, UUID controllerId, UUID targetId) {
            return new StackEntry(
                    StackEntryType.SORCERY_SPELL, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentEffect()), 0, targetId, null
            );
        }

        private StackEntry createMultiTargetEntry(Card sourceCard, UUID controllerId, List<UUID> targetIds) {
            return new StackEntry(
                    StackEntryType.INSTANT_SPELL, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentEffect()), 0, targetIds
            );
        }

        /** Makes the mocked exileService actually add cards to exile so assertions on GameData work. */
        private void stubExileCardTrackedWithSource() {
            doAnswer(inv -> {
                GameData gameData = inv.getArgument(0);
                UUID ownerId = inv.getArgument(1);
                Card card = inv.getArgument(2);
                UUID sourcePermanentId = inv.getArgument(3);
                gameData.addToExile(ownerId, card, sourcePermanentId);
                return null;
            }).when(exileService).exileCard(any(), any(), any(), any());
        }

        // =========================================================================
        // ExileAllPermanentsEffect
        // =========================================================================

    @Test
            @DisplayName("Presents permanent choice to controller when defender has valid targets")
            void presentsChoiceWhenValidTargetsExist() {
                Card sourceCard = createCreatureCard("Daxos of Meletis");
                Permanent defenderPerm = addPermanent(player2Id, createCreatureCard("Grizzly Bears"));

                ExilePermanentDamagedPlayerControlsEffect effect =
                        new ExilePermanentDamagedPlayerControlsEffect(null);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, player2Id, null
                );

                exilePermanentDamagedPlayerControlsHandler.resolve(gd, entry, effect);

                assertThat(gd.pendingExileDamagedPlayerControlsPermanent).isTrue();
                verify(playerInputService).beginMultiPermanentChoice(
                        eq(gd), eq(player1Id), eq(List.of(defenderPerm.getId())), eq(1), anyString());
            }

            @Test
            @DisplayName("Filters permanents by predicate")
            void filtersWithPredicate() {
                Card sourceCard = createCreatureCard("Daxos of Meletis");
                addPermanent(player2Id, createCreatureCard("Grizzly Bears"));
                Permanent artifact = addPermanent(player2Id, createCard("Spellbook"));

                ExilePermanentDamagedPlayerControlsEffect effect =
                        new ExilePermanentDamagedPlayerControlsEffect(new PermanentIsCreaturePredicate());
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, player2Id, null
                );

                // Creature matches, artifact does not
                when(gameQueryService.matchesPermanentPredicate(eq(gd), any(), any()))
                        .thenAnswer(inv -> {
                            Permanent p = inv.getArgument(1);
                            return p.getCard().getType() == CardType.CREATURE;
                        });

                exilePermanentDamagedPlayerControlsHandler.resolve(gd, entry, effect);

                verify(playerInputService).beginMultiPermanentChoice(
                        eq(gd), eq(player1Id),
                        eq(List.of(gd.playerBattlefields.get(player2Id).getFirst().getId())),
                        eq(1), anyString());
            }

            @Test
            @DisplayName("Logs and returns when defender has no valid targets")
            void logsWhenNoValidTargets() {
                Card sourceCard = createCreatureCard("Daxos of Meletis");
                // Defender's battlefield is empty

                ExilePermanentDamagedPlayerControlsEffect effect =
                        new ExilePermanentDamagedPlayerControlsEffect(null);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, player2Id, null
                );

                exilePermanentDamagedPlayerControlsHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), anyString());
                verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), anyInt(), anyString());
            }

            @Test
            @DisplayName("Does nothing when defenderId is null")
            void doesNothingWhenDefenderIdNull() {
                Card sourceCard = createCreatureCard("Daxos of Meletis");

                ExilePermanentDamagedPlayerControlsEffect effect =
                        new ExilePermanentDamagedPlayerControlsEffect(null);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(effect), 0, (UUID) null, null
                );

                exilePermanentDamagedPlayerControlsHandler.resolve(gd, entry, effect);

                verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), anyInt(), anyString());
                verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
            }
}
