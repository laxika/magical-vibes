package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ReturnArtifactsTargetPlayerOwnsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.normalfx.BounceSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnArtifactsTargetPlayerOwnsToHandEffectHandler;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnArtifactsTargetPlayerOwnsToHandEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PlayerInputService playerInputService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @InjectMocks
    private BounceSupport bounceSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private ReturnArtifactsTargetPlayerOwnsToHandEffectHandler returnArtifactsTargetPlayerOwnsToHandHandler;

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
        returnArtifactsTargetPlayerOwnsToHandHandler = new ReturnArtifactsTargetPlayerOwnsToHandEffectHandler(
                gameQueryService, gameBroadcastService, permanentRemovalService);

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

        private StackEntry entryWithTarget(Card card, UUID controllerId, List<CardEffect> effects, UUID targetId) {
            return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName(), effects, 0, targetId, null);
        }

        private StackEntry entryWithTargetAndSource(Card card, UUID controllerId, List<CardEffect> effects,
                                                    UUID targetId, UUID sourcePermanentId) {
            return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName() + " trigger", effects, targetId, sourcePermanentId);
        }

        // =========================================================================
        // ReturnSelfToHandEffect
        // =========================================================================

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

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

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

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

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

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

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

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

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
                // Target player1 â€” should return artifacts player1 OWNS
                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player1Id);

                when(gameQueryService.isArtifact(stolen)).thenReturn(true);
                when(gameQueryService.isArtifact(ownArtifact)).thenReturn(true);
                when(permanentRemovalService.removePermanentToHand(gd, ownArtifact)).thenReturn(true);

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

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
                // Target player2 â€” should return artifacts player2 OWNS, even from player1's battlefield
                StackEntry entry = entryWithTarget(card, player1Id,
                        List.of(new ReturnArtifactsTargetPlayerOwnsToHandEffect()), player2Id);

                when(gameQueryService.isArtifact(stolen)).thenReturn(true);
                when(permanentRemovalService.removePermanentToHand(gd, stolen)).thenReturn(true);

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

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

                returnArtifactsTargetPlayerOwnsToHandHandler.resolve(gd, entry, new ReturnArtifactsTargetPlayerOwnsToHandEffect());

                verify(permanentRemovalService).removePermanentToHand(gd, artifact);
            }
}
