package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BounceCreatureOnUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandOnCoinFlipLossEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BounceResolutionServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private PermanentRemovalService permanentRemovalService;

    @InjectMocks private BounceResolutionService service;

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
    }

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Permanent createCreature(String name) {
        Card card = createCard(name);
        card.setType(CardType.CREATURE);
        return new Permanent(card);
    }

    private Permanent createArtifact(String name) {
        Card card = createCard(name);
        card.setType(CardType.ARTIFACT);
        return new Permanent(card);
    }

    private Permanent createEnchantment(String name) {
        Card card = createCard(name);
        card.setType(CardType.ENCHANTMENT);
        return new Permanent(card);
    }

    private StackEntry entryWithSource(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + " trigger", effects, (UUID) null, sourcePermanentId);
    }

    private StackEntry entryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetPermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName(), effects, 0, targetPermanentId, null);
    }

    private StackEntry entryWithTargetAndSource(Card card, UUID controllerId, List<CardEffect> effects,
                                                UUID targetPermanentId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + " trigger", effects, targetPermanentId, sourcePermanentId);
    }

    // =========================================================================
    // ReturnSelfToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnSelfToHand")
    class ResolveReturnSelfToHand {

        @Test
        @DisplayName("Returns the permanent to its owner's hand")
        void returnsPermanentToOwnersHand() {
            Card card = createCard("Viashino Sandscout");
            Permanent permanent = createCreature("Viashino Sandscout");
            gd.playerBattlefields.get(player1Id).add(permanent);

            StackEntry entry = entryWithSource(card, player1Id,
                    List.of(new ReturnSelfToHandEffect()), permanent.getId());

            when(gameQueryService.findPermanentById(gd, permanent.getId())).thenReturn(permanent);

            service.resolveReturnSelfToHand(gd, entry);

            verify(permanentRemovalService).removePermanentToHand(gd, permanent);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Viashino Sandscout is returned to its owner's hand."));
        }

        @Test
        @DisplayName("Does nothing when the permanent is already removed from the battlefield")
        void doesNothingWhenAlreadyRemoved() {
            Card card = createCard("Viashino Sandscout");
            UUID sourcePermanentId = UUID.randomUUID();

            StackEntry entry = entryWithSource(card, player1Id,
                    List.of(new ReturnSelfToHandEffect()), sourcePermanentId);

            when(gameQueryService.findPermanentById(gd, sourcePermanentId)).thenReturn(null);

            service.resolveReturnSelfToHand(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Viashino Sandscout is no longer on the battlefield."));
        }
    }

    // =========================================================================
    // ReturnTargetPermanentToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnTargetPermanentToHand")
    class ResolveReturnTargetPermanentToHand {

        @Test
        @DisplayName("Returns target permanent to its owner's hand")
        void returnsTargetPermanentToHand() {
            Card card = createCard("Boomerang");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnTargetPermanentToHandEffect()), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            service.resolveReturnTargetPermanentToHand(gd, entry, new ReturnTargetPermanentToHandEffect());

            verify(permanentRemovalService).removePermanentToHand(gd, target);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is returned to its owner's hand."));
        }

        @Test
        @DisplayName("Skips when target is removed before resolution")
        void skipsWhenTargetRemoved() {
            Card card = createCard("Boomerang");
            UUID targetId = UUID.randomUUID();

            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnTargetPermanentToHandEffect()), targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            service.resolveReturnTargetPermanentToHand(gd, entry, new ReturnTargetPermanentToHandEffect());

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Controller loses life when lifeLoss is set")
        void controllerLosesLifeWhenLifeLossSet() {
            Card card = createCard("Vapor Snag");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);
            gd.playerLifeTotals.put(player2Id, 20);

            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnTargetPermanentToHandEffect(1)), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            service.resolveReturnTargetPermanentToHand(gd, entry, new ReturnTargetPermanentToHandEffect(1));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(19);
            verify(gameOutcomeService).checkWinCondition(gd);
        }

        @Test
        @DisplayName("Life loss is skipped when player's life total can't change")
        void lifeLossSkippedWhenLifeCantChange() {
            Card card = createCard("Vapor Snag");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);
            gd.playerLifeTotals.put(player2Id, 20);

            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnTargetPermanentToHandEffect(1)), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            service.resolveReturnTargetPermanentToHand(gd, entry, new ReturnTargetPermanentToHandEffect(1));

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player2's life total can't change."));
        }

        @Test
        @DisplayName("Can bounce own permanent")
        void canBounceOwnPermanent() {
            Card card = createCard("Boomerang");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(target);

            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnTargetPermanentToHandEffect()), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            service.resolveReturnTargetPermanentToHand(gd, entry, new ReturnTargetPermanentToHandEffect());

            verify(permanentRemovalService).removePermanentToHand(gd, target);
        }
    }

    // =========================================================================
    // ReturnCreaturesToOwnersHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnCreaturesToOwnersHand")
    class ResolveReturnCreaturesToOwnersHand {

        @Test
        @DisplayName("Returns all creatures on both sides")
        void returnsAllCreatures() {
            Card card = createCard("Evacuation");
            Permanent creature1 = createCreature("Grizzly Bears");
            Permanent creature2 = createCreature("Serra Angel");
            Permanent creature3 = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature1);
            gd.playerBattlefields.get(player1Id).add(creature2);
            gd.playerBattlefields.get(player2Id).add(creature3);

            ReturnCreaturesToOwnersHandEffect effect = new ReturnCreaturesToOwnersHandEffect(Set.of());
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

            when(gameQueryService.isCreature(eq(gd), any())).thenReturn(true);
            when(gameQueryService.matchesFilters(any(), eq(Set.of()), any())).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(eq(gd), any())).thenReturn(true);

            service.resolveReturnCreaturesToOwnersHand(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, creature1);
            verify(permanentRemovalService).removePermanentToHand(gd, creature2);
            verify(permanentRemovalService).removePermanentToHand(gd, creature3);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Does not return non-creature permanents")
        void doesNotReturnNonCreatures() {
            Card card = createCard("Evacuation");
            Permanent creature = createCreature("Grizzly Bears");
            Permanent enchantment = createEnchantment("Glorious Anthem");
            gd.playerBattlefields.get(player1Id).add(enchantment);
            gd.playerBattlefields.get(player1Id).add(creature);

            ReturnCreaturesToOwnersHandEffect effect = new ReturnCreaturesToOwnersHandEffect(Set.of());
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isCreature(gd, enchantment)).thenReturn(false);
            when(gameQueryService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, creature)).thenReturn(true);

            service.resolveReturnCreaturesToOwnersHand(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, creature);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, enchantment);
        }

        @Test
        @DisplayName("Works with empty battlefields without crashing")
        void worksWithEmptyBattlefields() {
            Card card = createCard("Evacuation");
            ReturnCreaturesToOwnersHandEffect effect = new ReturnCreaturesToOwnersHandEffect(Set.of());
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

            service.resolveReturnCreaturesToOwnersHand(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            verify(permanentRemovalService, never()).removeOrphanedAuras(any());
        }
    }

    // =========================================================================
    // ReturnArtifactsTargetPlayerOwnsToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnArtifactsTargetPlayerOwnsToHand")
    class ResolveReturnArtifactsTargetPlayerOwnsToHand {

        @Test
        @DisplayName("Returns all artifacts target player owns to their hand")
        void returnsAllArtifactsToTargetPlayersHand() {
            Permanent artifact1 = createArtifact("Angel's Feather");
            Permanent artifact2 = createArtifact("Icy Manipulator");
            gd.playerBattlefields.get(player2Id).add(artifact1);
            gd.playerBattlefields.get(player2Id).add(artifact2);

            Card card = createCard("Hurkyl's Recall");
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player2Id);

            when(gameQueryService.isArtifact(artifact1)).thenReturn(true);
            when(gameQueryService.isArtifact(artifact2)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(eq(gd), any())).thenReturn(true);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            verify(permanentRemovalService).removePermanentToHand(gd, artifact1);
            verify(permanentRemovalService).removePermanentToHand(gd, artifact2);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Does not return non-artifact permanents")
        void doesNotReturnNonArtifacts() {
            Permanent artifact = createArtifact("Angel's Feather");
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(artifact);
            gd.playerBattlefields.get(player2Id).add(creature);

            Card card = createCard("Hurkyl's Recall");
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player2Id);

            when(gameQueryService.isArtifact(artifact)).thenReturn(true);
            when(gameQueryService.isArtifact(creature)).thenReturn(false);
            when(permanentRemovalService.removePermanentToHand(gd, artifact)).thenReturn(true);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            verify(permanentRemovalService).removePermanentToHand(gd, artifact);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, creature);
        }

        @Test
        @DisplayName("Does not affect other player's artifacts")
        void doesNotAffectOtherPlayersArtifacts() {
            Permanent player1Artifact = createArtifact("Angel's Feather");
            Permanent player2Artifact = createArtifact("Icy Manipulator");
            gd.playerBattlefields.get(player1Id).add(player1Artifact);
            gd.playerBattlefields.get(player2Id).add(player2Artifact);

            Card card = createCard("Hurkyl's Recall");
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player2Id);

            when(gameQueryService.isArtifact(player1Artifact)).thenReturn(true);
            when(gameQueryService.isArtifact(player2Artifact)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, player2Artifact)).thenReturn(true);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            verify(permanentRemovalService).removePermanentToHand(gd, player2Artifact);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, player1Artifact);
        }

        @Test
        @DisplayName("No-op when target player has no artifacts")
        void noOpWhenNoArtifacts() {
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(creature);

            Card card = createCard("Hurkyl's Recall");
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player2Id);

            when(gameQueryService.isArtifact(creature)).thenReturn(false);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
        }

        @Test
        @DisplayName("Stolen artifact on target's battlefield is NOT returned (target controls but doesn't own it)")
        void stolenArtifactOnTargetBattlefieldNotReturned() {
            // Angel's Feather owned by player2 but on player1's battlefield (stolen)
            Permanent stolen = createArtifact("Angel's Feather");
            gd.playerBattlefields.get(player1Id).add(stolen);
            gd.stolenCreatures.put(stolen.getId(), player2Id);
            gd.permanentControlStolenCreatures.add(stolen.getId());

            // Player1's own artifact
            Permanent ownArtifact = createArtifact("Icy Manipulator");
            gd.playerBattlefields.get(player1Id).add(ownArtifact);

            Card card = createCard("Hurkyl's Recall");
            // Target player1 — should return artifacts player1 OWNS
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player1Id);

            when(gameQueryService.isArtifact(stolen)).thenReturn(true);
            when(gameQueryService.isArtifact(ownArtifact)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, ownArtifact)).thenReturn(true);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            // Stolen artifact (owned by player2) should NOT be returned when targeting player1
            verify(permanentRemovalService, never()).removePermanentToHand(gd, stolen);
            // Player1's own artifact SHOULD be returned
            verify(permanentRemovalService).removePermanentToHand(gd, ownArtifact);
        }

        @Test
        @DisplayName("Artifact owned by target but controlled by opponent IS returned")
        void artifactOwnedByTargetButControlledByOpponentIsReturned() {
            // Angel's Feather owned by player2 but on player1's battlefield (stolen)
            Permanent stolen = createArtifact("Angel's Feather");
            gd.playerBattlefields.get(player1Id).add(stolen);
            gd.stolenCreatures.put(stolen.getId(), player2Id);
            gd.permanentControlStolenCreatures.add(stolen.getId());

            Card card = createCard("Hurkyl's Recall");
            // Target player2 — should return artifacts player2 OWNS, even from player1's battlefield
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player2Id);

            when(gameQueryService.isArtifact(stolen)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, stolen)).thenReturn(true);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            verify(permanentRemovalService).removePermanentToHand(gd, stolen);
        }

        @Test
        @DisplayName("Can target self to return own artifacts")
        void canTargetSelf() {
            Permanent artifact = createArtifact("Angel's Feather");
            gd.playerBattlefields.get(player1Id).add(artifact);

            Card card = createCard("Hurkyl's Recall");
            StackEntry entry = entryWithTarget(card, player1Id,
                    List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player1Id);

            when(gameQueryService.isArtifact(artifact)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, artifact)).thenReturn(true);

            service.resolveReturnArtifactsTargetPlayerOwnsToHand(gd, entry);

            verify(permanentRemovalService).removePermanentToHand(gd, artifact);
        }
    }

    // =========================================================================
    // BounceCreatureOnUpkeepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveBounceCreatureOnUpkeep")
    class ResolveBounceCreatureOnUpkeep {

        @Test
        @DisplayName("TRIGGER_TARGET_PLAYER scope prompts the target player to choose")
        void triggerTargetPlayerScopePromptsTargetPlayer() {
            Card card = createCard("Sunken Hope");
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(creature);

            BounceCreatureOnUpkeepEffect effect = new BounceCreatureOnUpkeepEffect(
                    BounceCreatureOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER, Set.of(),
                    "Choose a creature to return to its owner's hand.");
            StackEntry entry = entryWithTargetAndSource(card, player1Id,
                    List.of(effect), player2Id, UUID.randomUUID());

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);

            service.resolveBounceCreatureOnUpkeep(gd, entry, effect);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player2Id),
                    eq(List.of(creature.getId())), anyString());
        }

        @Test
        @DisplayName("SOURCE_CONTROLLER scope prompts the source controller to choose")
        void sourceControllerScopePromptsController() {
            Card card = createCard("Stampeding Wildebeests");
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);

            BounceCreatureOnUpkeepEffect effect = new BounceCreatureOnUpkeepEffect(
                    BounceCreatureOnUpkeepEffect.Scope.SOURCE_CONTROLLER, Set.of(),
                    "Choose a creature to return.");
            StackEntry entry = entryWithSource(card, player1Id,
                    List.of(effect), UUID.randomUUID());

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);

            service.resolveBounceCreatureOnUpkeep(gd, entry, effect);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    eq(List.of(creature.getId())), anyString());
        }

        @Test
        @DisplayName("Sets BounceCreature permanent choice context")
        void setsBounceCreatureContext() {
            Card card = createCard("Sunken Hope");
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player1Id).add(creature);

            BounceCreatureOnUpkeepEffect effect = new BounceCreatureOnUpkeepEffect(
                    BounceCreatureOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER, Set.of(),
                    "Choose a creature.");
            StackEntry entry = entryWithTargetAndSource(card, player1Id,
                    List.of(effect), player1Id, UUID.randomUUID());

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.matchesFilters(eq(creature), eq(Set.of()), any())).thenReturn(true);

            service.resolveBounceCreatureOnUpkeep(gd, entry, effect);

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
        }

        @Test
        @DisplayName("Logs and skips when no valid creatures exist")
        void logsWhenNoValidCreatures() {
            Card card = createCard("Sunken Hope");
            // No creatures on player1's battlefield

            BounceCreatureOnUpkeepEffect effect = new BounceCreatureOnUpkeepEffect(
                    BounceCreatureOnUpkeepEffect.Scope.TRIGGER_TARGET_PLAYER, Set.of(),
                    "Choose a creature.");
            StackEntry entry = entryWithTargetAndSource(card, player1Id,
                    List.of(effect), player1Id, UUID.randomUUID());

            service.resolveBounceCreatureOnUpkeep(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("controls no valid creatures")));
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Only offers creatures matching filters as valid choices")
        void filtersRestrictValidChoices() {
            Card card = createCard("Stampeding Wildebeests");
            Permanent matchingCreature = createCreature("Grizzly Bears");
            Permanent nonMatchingCreature = createCreature("Serra Angel");
            gd.playerBattlefields.get(player1Id).add(matchingCreature);
            gd.playerBattlefields.get(player1Id).add(nonMatchingCreature);

            BounceCreatureOnUpkeepEffect effect = new BounceCreatureOnUpkeepEffect(
                    BounceCreatureOnUpkeepEffect.Scope.SOURCE_CONTROLLER, Set.of(),
                    "Choose a green creature.");
            StackEntry entry = entryWithSource(card, player1Id,
                    List.of(effect), UUID.randomUUID());

            when(gameQueryService.isCreature(gd, matchingCreature)).thenReturn(true);
            when(gameQueryService.isCreature(gd, nonMatchingCreature)).thenReturn(true);
            when(gameQueryService.matchesFilters(eq(matchingCreature), any(), any())).thenReturn(true);
            when(gameQueryService.matchesFilters(eq(nonMatchingCreature), any(), any())).thenReturn(false);

            service.resolveBounceCreatureOnUpkeep(gd, entry, effect);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            assertThat(idsCaptor.getValue())
                    .contains(matchingCreature.getId())
                    .doesNotContain(nonMatchingCreature.getId());
        }
    }

    // =========================================================================
    // ReturnSelfToHandOnCoinFlipLossEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnSelfToHandOnCoinFlipLoss")
    class ResolveReturnSelfToHandOnCoinFlipLoss {

        @Test
        @DisplayName("Coin flip is logged and bounce or stay occurs")
        void coinFlipIsLoggedAndBounceOrStay() {
            Card card = createCard("Scoria Wurm");
            Permanent permanent = createCreature("Scoria Wurm");
            gd.playerBattlefields.get(player1Id).add(permanent);

            StackEntry entry = entryWithSource(card, player1Id,
                    List.of(new ReturnSelfToHandOnCoinFlipLossEffect()), permanent.getId());

            // These stubs may or may not be used depending on coin flip outcome
            lenient().when(gameQueryService.findPermanentById(gd, permanent.getId())).thenReturn(permanent);
            lenient().when(permanentRemovalService.removePermanentToHand(gd, permanent)).thenReturn(true);

            service.resolveReturnSelfToHandOnCoinFlipLoss(gd, entry);

            // Verify coin flip was logged
            ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
            verify(gameBroadcastService, atLeastOnce()).logAndBroadcast(eq(gd), logCaptor.capture());
            List<String> allLogs = logCaptor.getAllValues();
            assertThat(allLogs).anyMatch(log -> log.contains("coin flip for Scoria Wurm"));

            // Verify exactly one outcome: won (no bounce) or lost (bounced)
            boolean won = allLogs.stream().anyMatch(log -> log.contains("wins the coin flip"));
            if (won) {
                verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            } else {
                verify(permanentRemovalService).removePermanentToHand(gd, permanent);
            }
        }
    }
}
