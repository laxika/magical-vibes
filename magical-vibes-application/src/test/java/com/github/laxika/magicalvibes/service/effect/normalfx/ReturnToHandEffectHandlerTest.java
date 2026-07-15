package com.github.laxika.magicalvibes.service.effect.normalfx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Consolidated handler-unit test for {@link ReturnToHandEffectHandler}, nested by
 * {@link com.github.laxika.magicalvibes.model.effect.BounceScope}.
 */
@ExtendWith(MockitoExtension.class)
class ReturnToHandEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PredicateEvaluationService predicateEvaluationService;

    private BounceSupport bounceSupport;
    private ReturnToHandEffectHandler handler;
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

        bounceSupport = new BounceSupport(gameQueryService, gameBroadcastService, permanentRemovalService);
        handler = new ReturnToHandEffectHandler(gameQueryService, gameBroadcastService, gameOutcomeService,
                permanentRemovalService, predicateEvaluationService, bounceSupport);
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

    private Permanent createLand(String name) {
        Card card = createCard(name);
        card.setType(CardType.LAND);
        return new Permanent(card);
    }

    private StackEntry entryWithSource(Card card, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName() + " trigger", effects, (UUID) null, sourcePermanentId);
    }

    private StackEntry entryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName(), effects, 0, targetId, null);
    }

    @Nested
    @DisplayName("TARGET scope")
    class Target {

        @Test
        @DisplayName("Returns target permanent to its owner's hand")
        void returnsTargetPermanentToHand() {
            Card card = createCard("Boomerang");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            ReturnToHandEffect effect = ReturnToHandEffect.target();
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            handler.resolve(gd, entry, effect);

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

            ReturnToHandEffect effect = ReturnToHandEffect.target();
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Controller loses life when lifeLoss is set (Vapor Snag)")
        void controllerLosesLifeWhenLifeLossSet() {
            Card card = createCard("Vapor Snag");
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);
            gd.playerLifeTotals.put(player2Id, 20);

            ReturnToHandEffect effect = ReturnToHandEffect.targetAndControllerLosesLife(1);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            handler.resolve(gd, entry, effect);

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

            ReturnToHandEffect effect = ReturnToHandEffect.targetAndControllerLosesLife(1);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2Id);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);
            when(permanentRemovalService.removePermanentToHand(gd, target)).thenReturn(true);

            handler.resolve(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player2's life total can't change."));
        }
    }

    @Nested
    @DisplayName("SELF scope")
    class Self {

        @Test
        @DisplayName("Returns the source permanent to its owner's hand")
        void returnsPermanentToOwnersHand() {
            Card card = createCard("Viashino Sandscout");
            Permanent permanent = createCreature("Viashino Sandscout");
            gd.playerBattlefields.get(player1Id).add(permanent);

            ReturnToHandEffect effect = ReturnToHandEffect.self();
            StackEntry entry = entryWithSource(card, player1Id, List.of(effect), permanent.getId());

            when(gameQueryService.findPermanentById(gd, permanent.getId())).thenReturn(permanent);

            handler.resolve(gd, entry, effect);

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

            ReturnToHandEffect effect = ReturnToHandEffect.self();
            StackEntry entry = entryWithSource(card, player1Id, List.of(effect), sourcePermanentId);

            when(gameQueryService.findPermanentById(gd, sourcePermanentId)).thenReturn(null);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Viashino Sandscout is no longer on the battlefield."));
        }
    }

    @Nested
    @DisplayName("ALL_MATCHING scope")
    class AllMatching {

        @Test
        @DisplayName("Returns every permanent the filter matches, across all battlefields")
        void returnsFilterMatchingPermanents() {
            Card card = createCard("Evacuation");
            Permanent match1 = createCreature("Grizzly Bears");
            Permanent match2 = createCreature("Serra Angel");
            Permanent nonMatch = createEnchantment("Glorious Anthem");
            gd.playerBattlefields.get(player1Id).add(match1);
            gd.playerBattlefields.get(player1Id).add(nonMatch);
            gd.playerBattlefields.get(player2Id).add(match2);

            ReturnToHandEffect effect = ReturnToHandEffect.allPermanentsMatching(new PermanentIsCreaturePredicate());
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

            when(predicateEvaluationService.matchesPermanentPredicate(eq(match1), any(), any())).thenReturn(true);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(match2), any(), any())).thenReturn(true);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(nonMatch), any(), any())).thenReturn(false);
            when(permanentRemovalService.removePermanentToHand(eq(gd), any())).thenReturn(true);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, match1);
            verify(permanentRemovalService).removePermanentToHand(gd, match2);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, nonMatch);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Null filter returns every permanent without evaluating a predicate")
        void nullFilterReturnsEveryPermanent() {
            Card card = createCard("Cataclysmic Bounce");
            Permanent creature = createCreature("Grizzly Bears");
            Permanent land = createLand("Island");
            Permanent artifact = createArtifact("Icy Manipulator");
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.playerBattlefields.get(player1Id).add(land);
            gd.playerBattlefields.get(player2Id).add(artifact);

            ReturnToHandEffect effect = ReturnToHandEffect.allPermanentsMatching(null);
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

            when(permanentRemovalService.removePermanentToHand(eq(gd), any())).thenReturn(true);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, creature);
            verify(permanentRemovalService).removePermanentToHand(gd, land);
            verify(permanentRemovalService).removePermanentToHand(gd, artifact);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Works with empty battlefields without crashing")
        void worksWithEmptyBattlefields() {
            Card card = createCard("Evacuation");
            ReturnToHandEffect effect = ReturnToHandEffect.allPermanentsMatching(new PermanentIsCreaturePredicate());
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), null);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
            verify(permanentRemovalService, never()).removeOrphanedAuras(any());
        }
    }

    @Nested
    @DisplayName("TARGET_PLAYERS_PERMANENTS scope (River's Rebuke)")
    class TargetPlayersPermanents {

        @Test
        @DisplayName("Returns the matching permanents the target player controls")
        void returnsMatchingControlledPermanents() {
            Card card = createCard("River's Rebuke");
            Permanent nonland = createCreature("Grizzly Bears");
            Permanent land = createLand("Island");
            gd.playerBattlefields.get(player2Id).add(nonland);
            gd.playerBattlefields.get(player2Id).add(land);

            ReturnToHandEffect effect = ReturnToHandEffect.permanentsTargetPlayerControls(
                    new PermanentNotPredicate(new PermanentIsLandPredicate()));
            assertThat(effect.targetSpec().category().includesPlayers()).isTrue();

            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), player2Id);

            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(nonland), any())).thenReturn(true);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(land), any())).thenReturn(false);
            when(permanentRemovalService.removePermanentToHand(gd, nonland)).thenReturn(true);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, nonland);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, land);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("No-op when target player is unknown")
        void noOpWhenTargetUnknown() {
            Card card = createCard("River's Rebuke");
            ReturnToHandEffect effect = ReturnToHandEffect.permanentsTargetPlayerControls(
                    new PermanentNotPredicate(new PermanentIsLandPredicate()));
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), UUID.randomUUID());

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
        }
    }

    @Nested
    @DisplayName("TARGET_PLAYERS_OWNED scope (Hurkyl's Recall)")
    class TargetPlayersOwned {

        @Test
        @DisplayName("Returns the matching permanents the target player owns")
        void returnsOwnedArtifacts() {
            Card card = createCard("Hurkyl's Recall");
            Permanent artifact1 = createArtifact("Angel's Feather");
            Permanent artifact2 = createArtifact("Icy Manipulator");
            Permanent creature = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(artifact1);
            gd.playerBattlefields.get(player2Id).add(artifact2);
            gd.playerBattlefields.get(player2Id).add(creature);

            ReturnToHandEffect effect = ReturnToHandEffect.permanentsTargetPlayerOwns(new PermanentIsArtifactPredicate());
            assertThat(effect.targetSpec().category().includesPlayers()).isTrue();

            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), player2Id);

            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(artifact1), any())).thenReturn(true);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(artifact2), any())).thenReturn(true);
            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(creature), any())).thenReturn(false);
            when(permanentRemovalService.removePermanentToHand(eq(gd), any())).thenReturn(true);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, artifact1);
            verify(permanentRemovalService).removePermanentToHand(gd, artifact2);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, creature);
        }

        @Test
        @DisplayName("Owner-based: a stolen artifact on the target's battlefield (owned by someone else) is not returned")
        void stolenArtifactNotReturned() {
            Card card = createCard("Hurkyl's Recall");
            // Icy Manipulator on player1's battlefield but owned by player2 (stolen).
            Permanent stolen = createArtifact("Icy Manipulator");
            gd.playerBattlefields.get(player1Id).add(stolen);
            gd.stolenCreatures.put(stolen.getId(), player2Id);
            // An artifact player1 both controls and owns.
            Permanent ownArtifact = createArtifact("Angel's Feather");
            gd.playerBattlefields.get(player1Id).add(ownArtifact);

            ReturnToHandEffect effect = ReturnToHandEffect.permanentsTargetPlayerOwns(new PermanentIsArtifactPredicate());
            // Target player1 — should return artifacts player1 owns.
            StackEntry entry = entryWithTarget(card, player1Id, List.of(effect), player1Id);

            when(predicateEvaluationService.matchesPermanentPredicate(eq(gd), eq(ownArtifact), any())).thenReturn(true);
            when(permanentRemovalService.removePermanentToHand(gd, ownArtifact)).thenReturn(true);

            handler.resolve(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToHand(gd, ownArtifact);
            verify(permanentRemovalService, never()).removePermanentToHand(gd, stolen);
        }
    }
}
