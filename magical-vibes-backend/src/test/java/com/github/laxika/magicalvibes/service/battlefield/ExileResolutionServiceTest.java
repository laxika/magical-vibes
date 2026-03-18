package com.github.laxika.magicalvibes.service.battlefield;

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
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
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
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExileResolutionServiceTest {

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
    private ExileResolutionService exileResolutionService;

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
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerExiledCards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerExiledCards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
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

    private StackEntry createSingleTargetEntry(Card sourceCard, UUID controllerId, UUID targetPermanentId) {
        return new StackEntry(
                StackEntryType.SORCERY_SPELL, sourceCard, controllerId, sourceCard.getName(),
                List.of(new ExileTargetPermanentEffect()), 0, targetPermanentId, null
        );
    }

    private StackEntry createMultiTargetEntry(Card sourceCard, UUID controllerId, List<UUID> targetIds) {
        return new StackEntry(
                StackEntryType.INSTANT_SPELL, sourceCard, controllerId, sourceCard.getName(),
                List.of(new ExileTargetPermanentEffect()), 0, targetIds
        );
    }

    // =========================================================================
    // ExileTargetPermanentEffect — single target
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanent — single target")
    class ResolveExileTargetPermanentSingle {

        @Test
        @DisplayName("Exiles target permanent and logs the exile")
        void exilesTargetPermanent() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCard("Revoke Existence");

            StackEntry entry = createSingleTargetEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Spellbook is exiled."));
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Does nothing when target permanent is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCard("Revoke Existence");

            StackEntry entry = createSingleTargetEntry(sourceCard, player1Id, targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }
    }

    // =========================================================================
    // ExileTargetPermanentEffect — multi-target
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanent — multi-target")
    class ResolveExileTargetPermanentMulti {

        @Test
        @DisplayName("Exiles two target artifacts")
        void exilesTwoTargetArtifacts() {
            Card targetCard1 = createCard("Spellbook");
            Permanent target1 = new Permanent(targetCard1);
            Card targetCard2 = createCard("Spellbook");
            Permanent target2 = new Permanent(targetCard2);
            Card sourceCard = createCard("Into the Core");

            StackEntry entry = createMultiTargetEntry(sourceCard, player1Id,
                    List.of(target1.getId(), target2.getId()));

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target1);
            verify(permanentRemovalService).removePermanentToExile(gd, target2);
            verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), eq("Spellbook is exiled."));
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Skips targets that were removed before resolution and exiles the rest")
        void skipsRemovedTargets() {
            UUID removedId = UUID.randomUUID();
            Card targetCard = createCard("Spellbook");
            Permanent target2 = new Permanent(targetCard);
            Card sourceCard = createCard("Into the Core");

            StackEntry entry = createMultiTargetEntry(sourceCard, player1Id,
                    List.of(removedId, target2.getId()));

            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target2);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }
    }

    // =========================================================================
    // ExilePermanentDamagedPlayerControlsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExilePermanentDamagedPlayerControls")
    class ResolveExilePermanentDamagedPlayerControls {

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

            exileResolutionService.resolveExilePermanentDamagedPlayerControls(gd, entry, effect);

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

            exileResolutionService.resolveExilePermanentDamagedPlayerControls(gd, entry, effect);

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

            exileResolutionService.resolveExilePermanentDamagedPlayerControls(gd, entry, effect);

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

            exileResolutionService.resolveExilePermanentDamagedPlayerControls(gd, entry, effect);

            verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), anyInt(), anyString());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }
    }

    // =========================================================================
    // ExileTargetPermanentAndTrackWithSourceEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentAndTrackWithSource")
    class ResolveExileTargetPermanentAndTrackWithSource {

        @Test
        @DisplayName("Exiles target and tracks exiled card with source permanent")
        void exilesAndTracksWithSource() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCard("Karn Liberated");
            Permanent source = addPermanent(player1Id, sourceCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                    target.getId(), source.getId()
            );

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            exileResolutionService.resolveExileTargetPermanentAndTrackWithSource(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.permanentExiledCards.get(source.getId()))
                    .containsExactly(targetCard);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled by Karn Liberated."));
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Falls back to finding source by card reference when sourcePermanentId is null")
        void fallsBackToCardReferenceForSource() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCard("Karn Liberated");
            Permanent source = addPermanent(player1Id, sourceCard);

            // No sourcePermanentId in this constructor
            StackEntry entry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                    0, target.getId(), null
            );

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            exileResolutionService.resolveExileTargetPermanentAndTrackWithSource(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.permanentExiledCards.get(source.getId()))
                    .containsExactly(targetCard);
        }

        @Test
        @DisplayName("Does nothing when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCard("Karn Liberated");

            StackEntry entry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndTrackWithSourceEffect()),
                    0, targetId, null
            );

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanentAndTrackWithSource(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
        }
    }

    // =========================================================================
    // ExileTargetPermanentAndImprintEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentAndImprint")
    class ResolveExileTargetPermanentAndImprint {

        @Test
        @DisplayName("Exiles target and imprints onto source permanent")
        void exilesAndImprints() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCard("Exclusion Ritual");
            Permanent source = addPermanent(player1Id, sourceCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.ENCHANTMENT_SPELL, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndImprintEffect()),
                    0, target.getId(), null
            );

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            exileResolutionService.resolveExileTargetPermanentAndImprint(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(source.getCard().getImprintedCard()).isSameAs(targetCard);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled by Exclusion Ritual."));
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Does nothing when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCard("Exclusion Ritual");

            StackEntry entry = new StackEntry(
                    StackEntryType.ENCHANTMENT_SPELL, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndImprintEffect()),
                    0, targetId, null
            );

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanentAndImprint(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
        }

        @Test
        @DisplayName("Still exiles when source is not on battlefield but does not imprint")
        void exilesWithoutImprintWhenSourceGone() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCard("Exclusion Ritual");
            // Source is NOT on battlefield

            StackEntry entry = new StackEntry(
                    StackEntryType.ENCHANTMENT_SPELL, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndImprintEffect()),
                    0, target.getId(), null
            );

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            exileResolutionService.resolveExileTargetPermanentAndImprint(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            // No permanent to imprint onto
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }
    }

    // =========================================================================
    // ExileTargetPermanentAndReturnAtEndStepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentAndReturnAtEndStep")
    class ResolveExileTargetPermanentAndReturnAtEndStep {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID targetPermanentId) {
            return new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndReturnAtEndStepEffect()), 0, targetPermanentId, null
            );
        }

        @Test
        @DisplayName("Exiles target permanent and adds a pending exile return")
        void exilesAndSchedulesReturn() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player1Id));
        }

        @Test
        @DisplayName("Does nothing when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1Id, targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.pendingExileReturns).isEmpty();
        }

        @Test
        @DisplayName("Stolen creature's pending return uses original owner")
        void stolenCreatureUsesOriginalOwner() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            // Mark target as stolen from player2
            gd.stolenCreatures.put(target.getId(), player2Id);

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player2Id));
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled. It will return at the beginning of the next end step."));
        }
    }

    // =========================================================================
    // ExileSelfAndReturnAtEndStepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileSelfAndReturnAtEndStep")
    class ResolveExileSelfAndReturnAtEndStep {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
            return new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileSelfAndReturnAtEndStepEffect()), null, sourcePermanentId
            );
        }

        @Test
        @DisplayName("Exiles self and adds a pending exile return for the controller")
        void exilesSelfAndSchedulesReturn() {
            Card sourceCard = createCreatureCard("Argent Sphinx");
            Permanent source = new Permanent(sourceCard);

            StackEntry entry = createEntry(sourceCard, player1Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            exileResolutionService.resolveExileSelfAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, source);
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Argent Sphinx")
                            && per.controllerId().equals(player1Id));
        }

        @Test
        @DisplayName("Does nothing when the source permanent is removed before resolution")
        void fizzlesWhenSourceRemoved() {
            UUID sourceId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Argent Sphinx");

            StackEntry entry = createEntry(sourceCard, player1Id, sourceId);

            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

            exileResolutionService.resolveExileSelfAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.pendingExileReturns).isEmpty();
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            Card sourceCard = createCreatureCard("Argent Sphinx");
            Permanent source = new Permanent(sourceCard);

            StackEntry entry = createEntry(sourceCard, player1Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            exileResolutionService.resolveExileSelfAndReturnAtEndStep(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Argent Sphinx is exiled. It will return at the beginning of the next end step."));
        }
    }

    // =========================================================================
    // ExileTargetPermanentUntilSourceLeavesEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentUntilSourceLeaves")
    class ResolveExileTargetPermanentUntilSourceLeaves {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID targetPermanentId) {
            return new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentUntilSourceLeavesEffect()), 0, targetPermanentId, null
            );
        }

        @Test
        @DisplayName("Exiles target permanent and tracks return on source leave")
        void exilesTargetAndTracksReturn() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            Permanent source = addPermanent(player1Id, sourceCard);

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
            assertThat(gd.exileReturnOnPermanentLeave.get(source.getId()))
                    .isNotNull()
                    .satisfies(per -> {
                        assertThat(per.card().getName()).isEqualTo("Spellbook");
                        assertThat(per.controllerId()).isEqualTo(player2Id);
                    });
        }

        @Test
        @DisplayName("Does nothing when target permanent is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");

            StackEntry entry = createEntry(sourceCard, player1Id, targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        }

        @Test
        @DisplayName("Still exiles target when source has left battlefield but without return tracking")
        void exilesWithoutTrackingWhenSourceGone() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            // Source is NOT on battlefield

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        }

        @Test
        @DisplayName("Stolen permanent tracks original owner for return")
        void stolenPermanentUsesOriginalOwner() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            Permanent source = addPermanent(player1Id, sourceCard);

            // Mark target as stolen from player2
            gd.stolenCreatures.put(target.getId(), player2Id);

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            assertThat(gd.exileReturnOnPermanentLeave.get(source.getId()))
                    .isNotNull()
                    .satisfies(per -> {
                        assertThat(per.card().getName()).isEqualTo("Spellbook");
                        assertThat(per.controllerId()).isEqualTo(player2Id);
                    });
        }

        @Test
        @DisplayName("Logs exile message with source name")
        void logsExile() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            addPermanent(player1Id, sourceCard);

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Spellbook is exiled by Leonin Relic-Warder."));
        }
    }

    // =========================================================================
    // ImprintDyingCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveImprintDyingCreature")
    class ResolveImprintDyingCreature {

        @Test
        @DisplayName("Imprints a dying creature onto source permanent")
        void imprintsDyingCreature() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2Id).add(dyingCard);

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            // Dying card moved from graveyard to exile
            assertThat(gd.playerGraveyards.get(player2Id))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            verify(exileService).exileCard(gd, player2Id, dyingCard);
            // Card imprinted on source
            assertThat(vatPerm.getCard().getImprintedCard()).isSameAs(dyingCard);
        }

        @Test
        @DisplayName("Replaces previously imprinted card when a new creature dies")
        void replacesPreviousImprint() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            // Set up previously imprinted card
            Card previousCard = createCreatureCard("Giant Spider");
            vatPerm.getCard().setImprintedCard(previousCard);
            gd.playerExiledCards.get(player1Id).add(previousCard);

            // New dying creature
            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2Id).add(dyingCard);

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            // New card should be imprinted
            assertThat(vatPerm.getCard().getImprintedCard()).isSameAs(dyingCard);
            // Previous card returned to owner's graveyard
            verify(graveyardService).addCardToGraveyard(gd, player1Id, previousCard);
            // Previous card removed from exile
            assertThat(gd.playerExiledCards.get(player1Id))
                    .noneMatch(c -> c.getName().equals("Giant Spider"));
        }

        @Test
        @DisplayName("Does nothing when source permanent is gone")
        void fizzlesWhenSourceGone() {
            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2Id).add(dyingCard);

            UUID vatId = UUID.randomUUID();
            Card vatCard = createCard("Mimic Vat");

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                    List.of(effect), vatId, (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatId)).thenReturn(null);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            // Dying card should still be in graveyard
            assertThat(gd.playerGraveyards.get(player2Id))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Does nothing when dying card is no longer in graveyard")
        void fizzlesWhenDyingCardGone() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            UUID dyingCardId = UUID.randomUUID();
            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCardId);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            assertThat(vatPerm.getCard().getImprintedCard()).isNull();
        }

        @Test
        @DisplayName("Logs imprint message")
        void logsImprint() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2Id).add(dyingCard);

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1Id, "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled and imprinted on Mimic Vat."));
        }
    }

    // =========================================================================
    // ExileFromHandToImprintEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileFromHandToImprint")
    class ResolveExileFromHandToImprint {

        private final CardPredicate filter = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

        @Test
        @DisplayName("Prompts player to choose a card from hand to imprint")
        void promptsCardChoice() {
            Card anvilCard = createCard("Semblance Anvil");
            Permanent anvilPerm = new Permanent(anvilCard);

            gd.playerHands.get(player1Id).clear();
            Card handCard = createCreatureCard("Grizzly Bears");
            gd.playerHands.get(player1Id).add(handCard);

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                    List.of(effect), anvilPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);
            when(gameQueryService.matchesCardPredicate(eq(handCard), any(), any())).thenReturn(true);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService).beginImprintFromHandChoice(
                    eq(gd), eq(player1Id), eq(List.of(0)),
                    eq("Choose a nonland card from your hand to exile and imprint."),
                    eq(anvilPerm.getId()));
        }

        @Test
        @DisplayName("Skips when controller has no matching cards in hand")
        void skipsWhenNoMatchingCards() {
            Card anvilCard = createCard("Semblance Anvil");
            Permanent anvilPerm = new Permanent(anvilCard);

            gd.playerHands.get(player1Id).clear();

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                    List.of(effect), anvilPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Skips when no cards match the filter")
        void skipsWhenNoCardsMatchFilter() {
            Card anvilCard = createCard("Semblance Anvil");
            Permanent anvilPerm = new Permanent(anvilCard);

            gd.playerHands.get(player1Id).clear();
            Card handCard = createCreatureCard("Grizzly Bears");
            gd.playerHands.get(player1Id).add(handCard);

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                    List.of(effect), anvilPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);
            when(gameQueryService.matchesCardPredicate(eq(handCard), any(), any())).thenReturn(false);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when source permanent is gone")
        void fizzlesWhenSourceGone() {
            UUID anvilId = UUID.randomUUID();
            Card anvilCard = createCard("Semblance Anvil");

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1Id, "Semblance Anvil trigger",
                    List.of(effect), anvilId, (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilId)).thenReturn(null);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
        }
    }

    // =========================================================================
    // EachPlayerExilesTopCardsToSourceEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEachPlayerExilesTopCardsToSource")
    class ResolveEachPlayerExilesTopCardsToSource {

        @Test
        @DisplayName("Exiles top cards from each player's library to source pool")
        void exilesTopCardsFromEachPlayer() {
            Card sourceCard = createCard("Knowledge Pool");
            Permanent source = addPermanent(player1Id, sourceCard);

            Card p1Card1 = createSorceryCard("Lightning Bolt");
            Card p1Card2 = createCreatureCard("Grizzly Bears");
            Card p1Card3 = createCard("Sol Ring");
            gd.playerDecks.get(player1Id).addAll(List.of(p1Card1, p1Card2, p1Card3));

            Card p2Card1 = createSorceryCard("Doom Blade");
            Card p2Card2 = createCreatureCard("Llanowar Elves");
            gd.playerDecks.get(player2Id).addAll(List.of(p2Card1, p2Card2));

            EachPlayerExilesTopCardsToSourceEffect effect = new EachPlayerExilesTopCardsToSourceEffect(3);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(effect), (UUID) null, source.getId()
            );

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            exileResolutionService.resolveEachPlayerExilesTopCardsToSource(gd, entry, effect);

            // Player1 exiles 3, player2 exiles 2 (only has 2)
            assertThat(gd.permanentExiledCards.get(source.getId())).hasSize(5);
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            assertThat(gd.playerDecks.get(player2Id)).isEmpty();
            verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), anyString());
        }

        @Test
        @DisplayName("Fizzles when source permanent is no longer on battlefield")
        void fizzlesWhenSourceGone() {
            UUID sourceId = UUID.randomUUID();
            Card sourceCard = createCard("Knowledge Pool");

            EachPlayerExilesTopCardsToSourceEffect effect = new EachPlayerExilesTopCardsToSourceEffect(3);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(effect), (UUID) null, sourceId
            );

            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

            exileResolutionService.resolveEachPlayerExilesTopCardsToSource(gd, entry, effect);

            assertThat(gd.permanentExiledCards).isEmpty();
        }

        @Test
        @DisplayName("Skips players with empty libraries")
        void skipsEmptyLibraries() {
            Card sourceCard = createCard("Knowledge Pool");
            Permanent source = addPermanent(player1Id, sourceCard);

            Card p1Card = createSorceryCard("Lightning Bolt");
            gd.playerDecks.get(player1Id).add(p1Card);
            // player2 deck is empty

            EachPlayerExilesTopCardsToSourceEffect effect = new EachPlayerExilesTopCardsToSourceEffect(3);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(effect), (UUID) null, source.getId()
            );

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            exileResolutionService.resolveEachPlayerExilesTopCardsToSource(gd, entry, effect);

            assertThat(gd.permanentExiledCards.get(source.getId())).hasSize(1);
            // Only one log message (player1 only)
            verify(gameBroadcastService, times(1)).logAndBroadcast(eq(gd), anyString());
        }
    }

    // =========================================================================
    // KnowledgePoolExileAndCastEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveKnowledgePoolExileAndCast")
    class ResolveKnowledgePoolExileAndCast {

        @Test
        @DisplayName("Fizzles when Knowledge Pool is no longer on battlefield")
        void fizzlesWhenKPGone() {
            UUID kpId = UUID.randomUUID();
            Card sourceCard = createCard("Knowledge Pool");

            KnowledgePoolExileAndCastEffect effect =
                    new KnowledgePoolExileAndCastEffect(UUID.randomUUID(), kpId, player1Id);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(effect)
            );

            when(gameQueryService.findPermanentById(gd, kpId)).thenReturn(null);

            exileResolutionService.resolveKnowledgePoolExileAndCast(gd, entry, effect);

            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Logs and returns when original spell is no longer on the stack")
        void fizzlesWhenOriginalSpellGone() {
            Card kpCard = createCard("Knowledge Pool");
            Permanent kp = new Permanent(kpCard);

            KnowledgePoolExileAndCastEffect effect =
                    new KnowledgePoolExileAndCastEffect(UUID.randomUUID(), kp.getId(), player1Id);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, kpCard, player1Id, kpCard.getName(),
                    List.of(effect)
            );

            when(gameQueryService.findPermanentById(gd, kp.getId())).thenReturn(kp);

            exileResolutionService.resolveKnowledgePoolExileAndCast(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Knowledge Pool's ability — original spell is no longer on the stack."));
        }

        @Test
        @DisplayName("Exiles original spell and logs no-choice when pool has no eligible cards")
        void noEligibleCardsInPool() {
            Card kpCard = createCard("Knowledge Pool");
            Permanent kp = new Permanent(kpCard);

            Card originalCard = createSorceryCard("Lightning Bolt");
            StackEntry originalSpell = new StackEntry(
                    StackEntryType.SORCERY_SPELL, originalCard, player1Id, originalCard.getName(),
                    List.of()
            );
            gd.stack.add(originalSpell);

            KnowledgePoolExileAndCastEffect effect =
                    new KnowledgePoolExileAndCastEffect(originalCard.getId(), kp.getId(), player1Id);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, kpCard, player1Id, kpCard.getName(),
                    List.of(effect)
            );

            when(gameQueryService.findPermanentById(gd, kp.getId())).thenReturn(kp);

            exileResolutionService.resolveKnowledgePoolExileAndCast(gd, entry, effect);

            // Original spell removed from stack
            assertThat(gd.stack).doesNotContain(originalSpell);
            // Original card added to pool and exile
            assertThat(gd.permanentExiledCards.get(kp.getId())).contains(originalCard);
            verify(exileService).exileCard(gd, player1Id, originalCard);
            // No eligible cards → log message
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Knowledge Pool — no other nonland cards exiled. Player1 cannot cast a spell."));
        }

        @Test
        @DisplayName("Presents choice when pool has eligible nonland cards")
        void presentsChoiceWhenEligibleCardsExist() {
            Card kpCard = createCard("Knowledge Pool");
            Permanent kp = new Permanent(kpCard);

            // Pre-seed pool with an eligible card
            Card poolCard = createSorceryCard("Doom Blade");
            gd.permanentExiledCards.put(kp.getId(),
                    Collections.synchronizedList(new ArrayList<>(List.of(poolCard))));

            Card originalCard = createSorceryCard("Lightning Bolt");
            StackEntry originalSpell = new StackEntry(
                    StackEntryType.SORCERY_SPELL, originalCard, player1Id, originalCard.getName(),
                    List.of()
            );
            gd.stack.add(originalSpell);

            KnowledgePoolExileAndCastEffect effect =
                    new KnowledgePoolExileAndCastEffect(originalCard.getId(), kp.getId(), player1Id);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, kpCard, player1Id, kpCard.getName(),
                    List.of(effect)
            );

            CardView mockCardView = mock(CardView.class);
            when(gameQueryService.findPermanentById(gd, kp.getId())).thenReturn(kp);
            when(cardViewFactory.create(poolCard)).thenReturn(mockCardView);

            exileResolutionService.resolveKnowledgePoolExileAndCast(gd, entry, effect);

            assertThat(gd.knowledgePoolSourcePermanentId).isEqualTo(kp.getId());
            verify(playerInputService).sendKnowledgePoolCastChoice(
                    eq(gd), eq(player1Id), eq(List.of(poolCard.getId())), anyList());
        }
    }

    // =========================================================================
    // handleKnowledgePoolCastChoice
    // =========================================================================

    @Nested
    @DisplayName("handleKnowledgePoolCastChoice")
    class HandleKnowledgePoolCastChoice {

        @Test
        @DisplayName("Player declines — logs and broadcasts")
        void playerDeclines() {
            Player player = new Player(player1Id, "Player1");
            UUID kpId = UUID.randomUUID();
            gd.knowledgePoolSourcePermanentId = kpId;
            gd.interaction.beginKnowledgePoolCastChoice(player1Id, java.util.Set.of(), 1);

            exileResolutionService.handleKnowledgePoolCastChoice(gd, player, List.of());

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1 declines to cast a spell from Knowledge Pool."));
            verify(gameBroadcastService).broadcastGameState(gd);
            assertThat(gd.knowledgePoolSourcePermanentId).isNull();
        }

        @Test
        @DisplayName("Non-targeted spell is put directly on the stack")
        void nonTargetedSpellOnStack() {
            Player player = new Player(player1Id, "Player1");
            Card chosenCard = createSorceryCard("Wrath of God");
            UUID kpId = UUID.randomUUID();

            gd.knowledgePoolSourcePermanentId = kpId;
            gd.permanentExiledCards.put(kpId,
                    Collections.synchronizedList(new ArrayList<>(List.of(chosenCard))));
            gd.playerExiledCards.get(player1Id).add(chosenCard);
            gd.interaction.beginKnowledgePoolCastChoice(player1Id,
                    java.util.Set.of(chosenCard.getId()), 1);

            exileResolutionService.handleKnowledgePoolCastChoice(gd, player, List.of(chosenCard.getId()));

            // Card removed from pool and exile
            assertThat(gd.permanentExiledCards.get(kpId)).doesNotContain(chosenCard);
            assertThat(gd.playerExiledCards.get(player1Id)).doesNotContain(chosenCard);
            // Spell on stack
            assertThat(gd.stack).anyMatch(se -> se.getCard() == chosenCard);
            assertThat(gd.spellsCastThisTurn.get(player1Id)).isEqualTo(1);
            verify(triggerCollectionService).checkSpellCastTriggers(gd, chosenCard, player1Id, false);
            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Player-only targeting spell only offers players as targets, not creatures")
        void playerOnlySpellDoesNotOfferCreatureTargets() {
            Player player = new Player(player1Id, "Player1");
            Card chosenCard = createSorceryCard("Traumatize");
            chosenCard.addEffect(EffectSlot.SPELL, new MillHalfLibraryEffect());
            UUID kpId = UUID.randomUUID();

            gd.knowledgePoolSourcePermanentId = kpId;
            gd.permanentExiledCards.put(kpId,
                    Collections.synchronizedList(new ArrayList<>(List.of(chosenCard))));
            gd.playerExiledCards.get(player1Id).add(chosenCard);
            gd.interaction.beginKnowledgePoolCastChoice(player1Id,
                    java.util.Set.of(chosenCard.getId()), 1);

            // Put a creature on the battlefield — it must NOT appear as a valid target
            Card creatureCard = createCreatureCard("Baneslayer Angel");
            addPermanent(player2Id, creatureCard);

            exileResolutionService.handleKnowledgePoolCastChoice(gd, player, List.of(chosenCard.getId()));

            // Should begin permanent choice with only the two player IDs
            @SuppressWarnings("unchecked")
            org.mockito.ArgumentCaptor<List<UUID>> targetsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), targetsCaptor.capture(), anyString());
            List<UUID> validTargets = targetsCaptor.getValue();
            assertThat(validTargets).containsExactlyInAnyOrder(player1Id, player2Id);
        }

        @Test
        @DisplayName("Broadcasts and returns when pool is null")
        void broadcastsWhenPoolNull() {
            Player player = new Player(player1Id, "Player1");
            UUID kpId = UUID.randomUUID();
            gd.knowledgePoolSourcePermanentId = kpId;
            gd.interaction.beginKnowledgePoolCastChoice(player1Id, java.util.Set.of(), 1);

            exileResolutionService.handleKnowledgePoolCastChoice(gd, player, List.of(UUID.randomUUID()));

            verify(gameBroadcastService).broadcastGameState(gd);
        }
    }

    // =========================================================================
    // OmenMachineDrawStepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveOmenMachineDrawStep")
    class ResolveOmenMachineDrawStep {

        @Test
        @DisplayName("Logs and returns when library is empty")
        void logsWhenLibraryEmpty() {
            Card sourceCard = createCard("Omen Machine");

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
            );

            exileResolutionService.resolveOmenMachineDrawStep(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1's library is empty (Omen Machine)."));
        }

        @Test
        @DisplayName("Puts land directly onto the battlefield")
        void putsLandOntoBattlefield() {
            Card sourceCard = createCard("Omen Machine");
            Card landCard = createLandCard("Forest");
            gd.playerDecks.get(player1Id).add(landCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
            );

            exileResolutionService.resolveOmenMachineDrawStep(gd, entry);

            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1 puts Forest onto the battlefield."));
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Puts non-targeted non-land spell on the stack")
        void putsNonTargetedSpellOnStack() {
            Card sourceCard = createCard("Omen Machine");
            Card sorceryCard = createSorceryCard("Wrath of God");
            gd.playerDecks.get(player1Id).add(sorceryCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
            );

            exileResolutionService.resolveOmenMachineDrawStep(gd, entry);

            assertThat(gd.stack).anyMatch(se -> se.getCard() == sorceryCard);
            assertThat(gd.spellsCastThisTurn.get(player1Id)).isEqualTo(1);
            verify(triggerCollectionService).checkSpellCastTriggers(gd, sorceryCard, player1Id, false);
        }

        @Test
        @DisplayName("Targeted spell with no valid targets stays in exile")
        void targetedSpellNoTargetsStaysInExile() {
            Card sourceCard = createCard("Omen Machine");
            Card targetedCard = createSorceryCard("Doom Blade");
            targetedCard.addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
            gd.playerDecks.get(player1Id).add(targetedCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
            );

            exileResolutionService.resolveOmenMachineDrawStep(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Doom Blade has no valid targets and remains in exile."));
        }
    }

    // =========================================================================
    // MirrorOfFateEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveMirrorOfFate")
    class ResolveMirrorOfFate {

        @Test
        @DisplayName("Exiles library immediately when no exiled cards to choose")
        void exilesLibraryWhenNoExiledCards() {
            Card sourceCard = createCard("Mirror of Fate");

            Card libraryCard = createSorceryCard("Lightning Bolt");
            gd.playerDecks.get(player1Id).add(libraryCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new MirrorOfFateEffect())
            );

            exileResolutionService.resolveMirrorOfFate(gd, entry);

            // Library exiled
            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            assertThat(gd.playerExiledCards.get(player1Id)).contains(libraryCard);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1 exiles 1 card from their library (Mirror of Fate)."));
        }

        @Test
        @DisplayName("Presents choice when player has exiled cards")
        void presentsChoiceWhenExiledCardsExist() {
            Card sourceCard = createCard("Mirror of Fate");

            Card exiledCard = createSorceryCard("Lightning Bolt");
            gd.playerExiledCards.get(player1Id).add(exiledCard);

            StackEntry entry = new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                    List.of(new MirrorOfFateEffect())
            );

            CardView mockView = mock(CardView.class);
            when(cardViewFactory.create(exiledCard)).thenReturn(mockView);

            exileResolutionService.resolveMirrorOfFate(gd, entry);

            verify(playerInputService).sendMirrorOfFateChoice(
                    eq(gd), eq(player1Id), eq(List.of(exiledCard.getId())),
                    eq(List.of(mockView)), eq(1));
        }
    }

    // =========================================================================
    // handleMirrorOfFateChoice
    // =========================================================================

    @Nested
    @DisplayName("handleMirrorOfFateChoice")
    class HandleMirrorOfFateChoice {

        @Test
        @DisplayName("Puts single chosen card on top of library")
        void putsSingleCardOnTop() {
            Player player = new Player(player1Id, "Player1");
            Card exiledCard = createSorceryCard("Lightning Bolt");
            gd.playerExiledCards.get(player1Id).add(exiledCard);

            Card libraryCard = createSorceryCard("Doom Blade");
            gd.playerDecks.get(player1Id).add(libraryCard);

            gd.interaction.beginMirrorOfFateChoice(player1Id,
                    java.util.Set.of(exiledCard.getId()), 7);

            exileResolutionService.handleMirrorOfFateChoice(gd, player, List.of(exiledCard.getId()));

            // Library card exiled, chosen card on top
            assertThat(gd.playerDecks.get(player1Id)).containsExactly(exiledCard);
            assertThat(gd.playerExiledCards.get(player1Id)).contains(libraryCard);
            assertThat(gd.playerExiledCards.get(player1Id)).doesNotContain(exiledCard);
            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Throws when not awaiting Mirror of Fate choice")
        void throwsWhenNotAwaiting() {
            Player player = new Player(player1Id, "Player1");

            assertThatThrownBy(() ->
                    exileResolutionService.handleMirrorOfFateChoice(gd, player, List.of()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Throws when too many cards selected")
        void throwsWhenTooManyCards() {
            Player player = new Player(player1Id, "Player1");
            Card card1 = createSorceryCard("Card A");
            Card card2 = createSorceryCard("Card B");
            gd.playerExiledCards.get(player1Id).addAll(List.of(card1, card2));

            gd.interaction.beginMirrorOfFateChoice(player1Id,
                    java.util.Set.of(card1.getId(), card2.getId()), 1);

            assertThatThrownBy(() ->
                    exileResolutionService.handleMirrorOfFateChoice(gd, player,
                            List.of(card1.getId(), card2.getId())))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Too many");
        }

        @Test
        @DisplayName("Begins library reorder when multiple cards chosen")
        void beginsReorderForMultipleCards() {
            Player player = new Player(player1Id, "Player1");
            Card card1 = createSorceryCard("Card A");
            Card card2 = createSorceryCard("Card B");
            gd.playerExiledCards.get(player1Id).addAll(List.of(card1, card2));

            gd.interaction.beginMirrorOfFateChoice(player1Id,
                    java.util.Set.of(card1.getId(), card2.getId()), 7);

            exileResolutionService.handleMirrorOfFateChoice(gd, player,
                    List.of(card1.getId(), card2.getId()));

            verify(playerInputService).beginLibraryReorderFromExile(eq(gd), eq(player1Id), anyList());
        }
    }
}
